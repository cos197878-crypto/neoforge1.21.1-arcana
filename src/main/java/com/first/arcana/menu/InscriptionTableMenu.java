package com.first.arcana.menu;

import com.first.arcana.block.ModBlocks;
import com.first.arcana.component.ModDataComponents;
import com.first.arcana.component.SpellContainer;
import com.first.arcana.item.ModItems;
import com.first.arcana.item.custom.ScrollItem;
import com.first.arcana.item.custom.SpellBookItem;
import com.first.arcana.spell.SpellSlot;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 각인 테이블 메뉴 — Iron's Spells 방식.
 *
 * 실제 아이템 슬롯은 셋뿐이다: 책 / 두루마리 투입 / 배출.
 * 책 안의 주문들은 화면(Screen)이 클릭형 그리드로 그리고, 클릭은
 * 바닐라 메뉴 버튼 패킷(clickMenuButton)으로 서버에 전달된다 —
 * 마법부여대·석재절단기가 쓰는 것과 같은 경로라 별도 패킷이 필요 없다.
 *
 * 흐름:
 *   투입 칸에 주문 두루마리를 넣으면 즉시 책에 각인되고 두루마리는 소모된다.
 *   그리드 클릭(버튼 id 0..N-1) = 그 주문 선택 (책의 SELECTED_SLOT 변경)
 *   그리드 Shift+클릭(버튼 id 100+i) = 그 주문을 배출 칸으로 꺼내기
 *
 * 슬롯 배치:
 *   0        책 칸
 *   1        두루마리 투입 칸
 *   2        배출 칸 (꺼낸 두루마리가 나오는 곳)
 *   3 .. 29  플레이어 인벤토리
 *  30 .. 38  핫바
 */
public class InscriptionTableMenu extends AbstractContainerMenu {
    public static final int BOOK_SLOT = 0;
    public static final int INPUT_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    private static final int INV_START = 3;
    private static final int HOTBAR_START = INV_START + 27;

    /** Shift+클릭 꺼내기 버튼 id 의 시작점. id = EXTRACT_BUTTON_BASE + 칸번호 */
    public static final int EXTRACT_BUTTON_BASE = 100;
    /** 그리드가 표시할 수 있는 최대 칸 수 (화면 공간 한계) */
    public static final int MAX_DISPLAY_SLOTS = 12;

    private final SimpleContainer tableContainer = new SimpleContainer(3);
    private final ContainerLevelAccess access;
    private final Player player;
    /** 각인 처리 중 리스너 재진입 방지 */
    private boolean inscribing;

    public InscriptionTableMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenuTypes.INSCRIPTION_TABLE.get(), containerId);
        this.access = access;
        this.player = playerInventory.player;

        this.tableContainer.addListener(container -> onTableChanged());

        this.addSlot(new BookSlot(tableContainer, BOOK_SLOT, 18, 22));
        this.addSlot(new ScrollInputSlot(tableContainer, INPUT_SLOT, 18, 58));
        this.addSlot(new OutputSlot(tableContainer, OUTPUT_SLOT, 213, 138));

        // 플레이어 인벤토리 (9..35)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, 9 + row * 9 + col, 8 + col * 18, 84 + row * 18));
            }
        }
        // 핫바 (0..8)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    // ---------------- 화면이 읽는 상태 ----------------

    public ItemStack getBook() {
        return tableContainer.getItem(BOOK_SLOT);
    }

    public boolean hasBook() {
        return getBook().getItem() instanceof SpellBookItem;
    }

    // ---------------- 자동 각인 ----------------

    /** 투입 칸에 두루마리가 들어오면 즉시 책에 새기고 소모한다. */
    private void onTableChanged() {
        if (inscribing || player.level().isClientSide()) {
            return;
        }
        ItemStack input = tableContainer.getItem(INPUT_SLOT);
        SpellSlot spell = ScrollItem.getSlot(input);
        if (spell == null || !hasBook()) {
            return;
        }

        ItemStack book = getBook();
        SpellContainer container = SpellBookItem.getContainer(book);
        if (container.isFull()) {
            return; // 책이 꽉 찼으면 두루마리를 투입 칸에 그대로 둔다.
        }

        inscribing = true;
        try {
            SpellBookItem.setContainer(book, container.withAdded(spell));
            input.shrink(1);
            tableContainer.setItem(INPUT_SLOT, input.isEmpty() ? ItemStack.EMPTY : input);
        } finally {
            inscribing = false;
        }
        access.execute((level, pos) -> level.playSound(null, pos,
                SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.1F));
    }

    // ---------------- 그리드 버튼 (선택 / 꺼내기) ----------------

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!hasBook()) {
            return false;
        }
        ItemStack book = getBook();
        SpellContainer container = SpellBookItem.getContainer(book);

        if (id >= 0 && id < container.spells().size()) {
            // 선택 — HUD 스펠 바와 우클릭 시전이 이 값을 그대로 쓴다.
            book.set(ModDataComponents.SELECTED_SLOT.get(), id);
            return true;
        }

        int extractIndex = id - EXTRACT_BUTTON_BASE;
        if (extractIndex >= 0 && extractIndex < container.spells().size()) {
            return extract(book, container, extractIndex);
        }
        return false;
    }

    /** 주문 하나를 책에서 빼서 배출 칸에 두루마리로 내놓는다. */
    private boolean extract(ItemStack book, SpellContainer container, int index) {
        if (!tableContainer.getItem(OUTPUT_SLOT).isEmpty()) {
            return false; // 배출 칸을 먼저 비워야 한다.
        }
        SpellSlot spell = container.get(index);
        if (spell == null) {
            return false;
        }

        SpellBookItem.setContainer(book, container.withRemoved(index));
        tableContainer.setItem(OUTPUT_SLOT, ScrollItem.of(ModItems.SCROLL.get(), spell));

        // 선택 칸이 빠진 자리 뒤를 가리키고 있었으면 당겨온다.
        int remaining = container.spells().size() - 1;
        int selected = SpellBookItem.getSelectedIndex(book);
        if (selected >= remaining) {
            book.set(ModDataComponents.SELECTED_SLOT.get(), Math.max(0, remaining - 1));
        }
        return true;
    }

    // ---------------- 메뉴 규칙 ----------------

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.INSCRIPTION_TABLE.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // 세 칸 전부 진짜 아이템이므로 닫을 때 모두 돌려준다.
        this.access.execute((level, pos) -> this.clearContainer(player, tableContainer));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < INV_START) {
            // 테이블 칸 -> 인벤토리
            if (!this.moveItemStackTo(stack, INV_START, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof SpellBookItem
                && this.moveItemStackTo(stack, BOOK_SLOT, BOOK_SLOT + 1, false)) {
            // 주문서 -> 책 칸. 성공했으면 그대로 진행.
        } else if (stack.getItem() instanceof ScrollItem && ScrollItem.getSlot(stack) != null
                && this.moveItemStackTo(stack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
            // 두루마리 -> 투입 칸 (책이 없으면 mayPlace 가 막는다). 성공 시 그대로 진행.
        } else if (index < HOTBAR_START) {
            if (!this.moveItemStackTo(stack, HOTBAR_START, this.slots.size(), false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.moveItemStackTo(stack, INV_START, HOTBAR_START, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    // ---------------- 슬롯 종류 ----------------

    /** 주문서 한 권만 받는 칸 */
    private static class BookSlot extends Slot {
        BookSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof SpellBookItem;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    /** 주문이 담긴 두루마리만, 책이 놓여 있을 때만 받는 투입 칸 */
    private class ScrollInputSlot extends Slot {
        ScrollInputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return hasBook()
                    && stack.getItem() instanceof ScrollItem
                    && ScrollItem.getSlot(stack) != null;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    /** 꺼낸 두루마리가 나오는 칸. 손으로 넣을 수는 없다. */
    private static class OutputSlot extends Slot {
        OutputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
