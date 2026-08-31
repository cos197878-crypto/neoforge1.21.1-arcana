package com.first.arcana.menu;

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
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 주문서에 두루마리를 넣고 빼는 메뉴.
 *
 * 데이터 모델: 책의 진짜 상태는 SPELL_CONTAINER 데이터 컴포넌트다.
 * 메뉴를 열 때 컴포넌트를 두루마리 스택으로 "펼쳐서" 가상 컨테이너에 담고,
 * 슬롯이 바뀔 때마다 다시 컴포넌트로 "접어서" 저장한다.
 * SpellSlot 과 두루마리 스택의 왕복이 무손실이라 복제도 소실도 없다.
 *
 * 슬롯 배치:
 *   0 .. 4   책의 주문 칸 (주문이 담긴 두루마리만)
 *   5 .. 31  플레이어 인벤토리
 *  32 .. 40  핫바
 */
public class SpellBookMenu extends AbstractContainerMenu {
    public static final int SPELL_SLOTS = SpellContainer.DEFAULT_SLOTS;
    private static final int INV_SLOT_START = SPELL_SLOTS;
    private static final int HOTBAR_START = SPELL_SLOTS + 27;

    private final SimpleContainer bookInventory = new SimpleContainer(SPELL_SLOTS);
    private final Inventory playerInventory;
    /** 열려 있는 책이 든 인벤토리 칸 (0-8 핫바, 40 오프핸드) */
    private final int lockedInvIndex;

    public SpellBookMenu(int containerId, Inventory playerInventory, int lockedInvIndex) {
        super(ModMenuTypes.SPELL_BOOK.get(), containerId);
        this.playerInventory = playerInventory;
        this.lockedInvIndex = lockedInvIndex;

        loadFromBook();
        // 리스너는 초기 적재가 끝난 뒤에 붙인다. 적재 중에 저장이 돌면 안 된다.
        this.bookInventory.addListener(container -> saveToBook());

        // 책의 주문 칸 5개
        for (int i = 0; i < SPELL_SLOTS; i++) {
            this.addSlot(new ScrollOnlySlot(bookInventory, i, 44 + i * 18, 26));
        }
        // 플레이어 인벤토리 (9..35)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, 9 + row * 9 + col, 8 + col * 18, 84 + row * 18));
            }
        }
        // 핫바 (0..8). 열려 있는 책이 든 칸은 잠근다.
        for (int col = 0; col < 9; col++) {
            if (col == lockedInvIndex) {
                this.addSlot(new LockedSlot(playerInventory, col, 8 + col * 18, 142));
            } else {
                this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
            }
        }
    }

    // ---------------- 컴포넌트 <-> 슬롯 왕복 ----------------

    private void loadFromBook() {
        SpellContainer container = SpellBookItem.getContainer(getBook());
        List<SpellSlot> spells = container.spells();
        for (int i = 0; i < Math.min(SPELL_SLOTS, spells.size()); i++) {
            bookInventory.setItem(i, ScrollItem.of(ModItems.SCROLL.get(), spells.get(i)));
        }
    }

    /** 판정은 서버가 하므로 저장도 서버에서만. 클라이언트 쪽 스택은 어차피 서버가 덮어쓴다. */
    private void saveToBook() {
        if (playerInventory.player.level().isClientSide()) {
            return;
        }
        ItemStack book = getBook();
        if (!(book.getItem() instanceof SpellBookItem)) {
            return;
        }

        SpellContainer original = SpellBookItem.getContainer(book);
        List<SpellSlot> spells = new ArrayList<>();
        for (int i = 0; i < SPELL_SLOTS; i++) {
            SpellSlot slot = ScrollItem.getSlot(bookInventory.getItem(i));
            if (slot != null) {
                spells.add(slot);
            }
        }
        // 이 메뉴는 5칸만 보여준다. 커맨드/업그레이드로 만든 더 큰 책의 6번째 이후 주문과
        // 원래 maxSlots 를 여기서 날려먹으면 안 되므로 그대로 이어붙여 보존한다.
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

    private ItemStack getBook() {
        return playerInventory.getItem(lockedInvIndex);
    }

    // ---------------- 메뉴 규칙 ----------------

    @Override
    public boolean stillValid(Player player) {
        return getBook().getItem() instanceof SpellBookItem;
    }

    /** 숫자키/F키 스왑으로 열려 있는 책 자체를 빼돌리는 것을 막는다. */
    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (clickType == ClickType.SWAP && button == lockedInvIndex) {
            return;
        }
        if (slotId >= 0 && slotId < this.slots.size() && this.slots.get(slotId) instanceof LockedSlot) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem() || slot instanceof LockedSlot) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < INV_SLOT_START) {
            // 책 -> 인벤토리
            if (!this.moveItemStackTo(stack, INV_SLOT_START, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof ScrollItem && ScrollItem.getSlot(stack) != null
                && this.moveItemStackTo(stack, 0, SPELL_SLOTS, false)) {
            // 두루마리 -> 책. 성공했으면 그대로 진행.
        } else if (index < HOTBAR_START) {
            // 인벤토리 -> 핫바
            if (!this.moveItemStackTo(stack, HOTBAR_START, this.slots.size(), false)) {
                return ItemStack.EMPTY;
            }
        } else {
            // 핫바 -> 인벤토리
            if (!this.moveItemStackTo(stack, INV_SLOT_START, HOTBAR_START, false)) {
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

    /** 주문이 담긴 두루마리만 한 장씩 받는 칸 */
    private static class ScrollOnlySlot extends Slot {
        ScrollOnlySlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof ScrollItem && ScrollItem.getSlot(stack) != null;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    /** 열려 있는 책이 든 칸. 넣지도 빼지도 못한다. */
    private static class LockedSlot extends Slot {
        LockedSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}
