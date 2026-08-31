package com.first.arcana.menu;

import com.first.arcana.block.ModBlocks;
import com.first.arcana.component.ModDataComponents;
import com.first.arcana.component.SpellContainer;
import com.first.arcana.item.ModItems;
import com.first.arcana.item.custom.ScrollItem;
import com.first.arcana.item.custom.SpellBookItem;
import com.first.arcana.spell.SpellSlot;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 각인 테이블 메뉴. 책 슬롯에 주문서를 놓으면 그 안의 주문들이
 * 두루마리 스택으로 "펼쳐져" 오른쪽 5칸에 나타난다. 칸이 바뀔 때마다
 * 즉시 책의 SPELL_CONTAINER 컴포넌트로 "접어서" 저장한다.
 *
 * 두루마리 칸은 파생된 표시일 뿐이라 (진짜 저장소는 책 컴포넌트),
 * 메뉴를 닫을 때 책만 돌려주고 두루마리 칸은 돌려주지 않는다 — 복제 방지의 핵심.
 *
 * 슬롯 배치:
 *   0        책 칸
 *   1 .. 5   책의 주문 칸 (주문이 담긴 두루마리만, 책이 있을 때만)
 *   6 .. 32  플레이어 인벤토리
 *  33 .. 41  핫바
 */
public class InscriptionTableMenu extends AbstractContainerMenu {
    public static final int SPELL_SLOTS = SpellContainer.DEFAULT_SLOTS;
    private static final int BOOK_SLOT = 0;
    private static final int SPELL_START = 1;
    private static final int INV_START = SPELL_START + SPELL_SLOTS;
    private static final int HOTBAR_START = INV_START + 27;

    private final SimpleContainer bookContainer = new SimpleContainer(1);
    private final SimpleContainer scrollContainer = new SimpleContainer(SPELL_SLOTS);
    private final ContainerLevelAccess access;
    private final Player player;
    /** 책에서 칸을 다시 채우는 동안 저장 리스너가 되받아치지 않게 막는 플래그 */
    private boolean refreshing;

    public InscriptionTableMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenuTypes.INSCRIPTION_TABLE.get(), containerId);
        this.access = access;
        this.player = playerInventory.player;

        this.bookContainer.addListener(container -> onBookChanged());
        this.scrollContainer.addListener(container -> onScrollsChanged());

        // 0: 책 칸
        this.addSlot(new BookSlot(bookContainer, 0, 17, 26));
        // 1..5: 주문 칸
        for (int i = 0; i < SPELL_SLOTS; i++) {
            this.addSlot(new ScrollOnlySlot(scrollContainer, i, 71 + i * 18, 26));
        }
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

    // ---------------- 책 <-> 칸 왕복 (서버 전용) ----------------

    /** 책이 놓이거나 빠지면 주문 칸을 그 책 내용으로 다시 채운다. */
    private void onBookChanged() {
        if (refreshing || player.level().isClientSide()) {
            return;
        }
        refreshing = true;
        try {
            ItemStack book = bookContainer.getItem(0);
            SpellContainer container = (book.getItem() instanceof SpellBookItem)
                    ? SpellBookItem.getContainer(book)
                    : SpellContainer.EMPTY;
            for (int i = 0; i < SPELL_SLOTS; i++) {
                SpellSlot slot = container.get(i);
                scrollContainer.setItem(i, slot == null
                        ? ItemStack.EMPTY
                        : ScrollItem.of(ModItems.SCROLL.get(), slot));
            }
        } finally {
            refreshing = false;
        }
    }

    /** 주문 칸이 바뀔 때마다 즉시 책 컴포넌트에 저장한다. */
    private void onScrollsChanged() {
        if (refreshing || player.level().isClientSide()) {
            return;
        }
        ItemStack book = bookContainer.getItem(0);
        if (!(book.getItem() instanceof SpellBookItem)) {
            return;
        }

        SpellContainer original = SpellBookItem.getContainer(book);
        List<SpellSlot> spells = new ArrayList<>();
        for (int i = 0; i < SPELL_SLOTS; i++) {
            SpellSlot slot = ScrollItem.getSlot(scrollContainer.getItem(i));
            if (slot != null) {
                spells.add(slot);
            }
        }
        // 이 메뉴는 5칸만 보여준다. 더 큰 책의 6번째 이후 주문과 maxSlots 는 보존한다.
        for (int i = SPELL_SLOTS; i < original.spells().size(); i++) {
            spells.add(original.spells().get(i));
        }
        int maxSlots = Math.max(original.maxSlots(), SPELL_SLOTS);
        SpellBookItem.setContainer(book, new SpellContainer(maxSlots, List.copyOf(spells)));

        // 주문이 빠져서 선택 칸이 범위를 벗어났으면 당겨온다.
        int selected = SpellBookItem.getSelectedIndex(book);
        if (selected >= spells.size()) {
            book.set(ModDataComponents.SELECTED_SLOT.get(), Math.max(0, spells.size() - 1));
        }
    }

    // ---------------- 메뉴 규칙 ----------------

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.INSCRIPTION_TABLE.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // 주문 칸은 파생 표시일 뿐이라 (내용이 책 안에 저장돼 있다) 돌려주면 복제다. 책만 돌려준다.
        this.access.execute((level, pos) -> this.clearContainer(player, bookContainer));
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
            // 책/주문 칸 -> 인벤토리
            if (!this.moveItemStackTo(stack, INV_START, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof SpellBookItem
                && this.moveItemStackTo(stack, BOOK_SLOT, BOOK_SLOT + 1, false)) {
            // 주문서 -> 책 칸. 성공했으면 그대로 진행.
        } else if (stack.getItem() instanceof ScrollItem && ScrollItem.getSlot(stack) != null
                && this.moveItemStackTo(stack, SPELL_START, INV_START, false)) {
            // 두루마리 -> 주문 칸 (책이 없으면 mayPlace 가 막아서 실패한다). 성공 시 그대로 진행.
        } else if (index < HOTBAR_START) {
            // 인벤토리 -> 핫바
            if (!this.moveItemStackTo(stack, HOTBAR_START, this.slots.size(), false)) {
                return ItemStack.EMPTY;
            }
        } else {
            // 핫바 -> 인벤토리
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

    /** 주문이 담긴 두루마리만, 책이 놓여 있을 때만 받는 칸 */
    private class ScrollOnlySlot extends Slot {
        ScrollOnlySlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return bookContainer.getItem(0).getItem() instanceof SpellBookItem
                    && stack.getItem() instanceof ScrollItem
                    && ScrollItem.getSlot(stack) != null;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
