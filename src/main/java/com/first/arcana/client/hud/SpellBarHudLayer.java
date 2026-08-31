package com.first.arcana.client.hud;

import com.first.arcana.attachment.MagicData;
import com.first.arcana.attachment.ModAttachments;
import com.first.arcana.component.SpellContainer;
import com.first.arcana.item.ModItems;
import com.first.arcana.item.custom.ScrollItem;
import com.first.arcana.item.custom.SpellBookItem;
import com.first.arcana.spell.AbstractSpell;
import com.first.arcana.spell.SpellSlot;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 주문서를 들고 있을 때 화면 왼쪽 아래에 새겨진 주문들을 아이콘으로 보여주는 바.
 * 선택된 주문은 금색 테두리, 쿨다운 중이면 아래에서 차오르는 반투명 덮개.
 *
 * 아이콘은 계열별 두루마리 아이템을 그대로 그린다 — 계열 색이 자동으로 따라온다.
 */
public class SpellBarHudLayer implements LayeredDraw.Layer {
    private static final int FRAME = 22;
    private static final int GAP = 2;
    private static final int MARGIN_LEFT = 8;
    private static final int MARGIN_BOTTOM = 8;

    private static final int COLOR_FRAME = 0xFF2B2B3B;
    private static final int COLOR_FRAME_SELECTED = 0xFFF8C542;
    private static final int COLOR_BG = 0xB0101018;
    private static final int COLOR_COOLDOWN = 0x90FFFFFF;

    /** 렌더링은 매 프레임 돌므로, 컨테이너가 바뀔 때만 아이콘 스택을 다시 만든다. */
    private SpellContainer cachedContainer;
    private List<ItemStack> cachedStacks = List.of();

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || player.isSpectator() || minecraft.options.hideGui) {
            return;
        }

        ItemStack book = findHeldBook(player);
        if (book.isEmpty()) {
            return;
        }
        SpellContainer container = SpellBookItem.getContainer(book);
        if (container.spells().isEmpty()) {
            return;
        }

        refreshCache(container);
        int selected = SpellBookItem.getSelectedIndex(book);
        MagicData data = player.getData(ModAttachments.MAGIC_DATA);

        int x = MARGIN_LEFT;
        int y = guiGraphics.guiHeight() - MARGIN_BOTTOM - FRAME;

        for (int i = 0; i < cachedStacks.size(); i++) {
            drawFrame(guiGraphics, x, y, i == selected);
            guiGraphics.renderFakeItem(cachedStacks.get(i), x + 3, y + 3);
            drawCooldownOverlay(guiGraphics, x, y, container.spells().get(i), data);
            x += FRAME + GAP;
        }
    }

    // ---------------- private ----------------

    private static ItemStack findHeldBook(LocalPlayer player) {
        ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (main.getItem() instanceof SpellBookItem) {
            return main;
        }
        ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
        if (off.getItem() instanceof SpellBookItem) {
            return off;
        }
        return ItemStack.EMPTY;
    }

    private void refreshCache(SpellContainer container) {
        if (container.equals(cachedContainer)) {
            return;
        }
        List<ItemStack> stacks = new ArrayList<>(container.spells().size());
        for (SpellSlot slot : container.spells()) {
            stacks.add(ScrollItem.of(ModItems.SCROLL.get(), slot));
        }
        cachedContainer = container;
        cachedStacks = List.copyOf(stacks);
    }

    private static void drawFrame(GuiGraphics guiGraphics, int x, int y, boolean selected) {
        guiGraphics.fill(x, y, x + FRAME, y + FRAME, selected ? COLOR_FRAME_SELECTED : COLOR_FRAME);
        guiGraphics.fill(x + 1, y + 1, x + FRAME - 1, y + FRAME - 1, COLOR_BG);
    }

    private static void drawCooldownOverlay(GuiGraphics guiGraphics, int x, int y,
                                            SpellSlot slot, MagicData data) {
        AbstractSpell spell = slot.spell();
        if (spell == null || spell.getCooldownTicks() <= 0) {
            return;
        }
        int remaining = data.getCooldown(slot.spellId());
        if (remaining <= 0) {
            return;
        }
        float fraction = Math.min(1.0F, remaining / (float) spell.getCooldownTicks());
        int inner = FRAME - 2;
        int height = Math.max(1, (int) (inner * fraction));
        guiGraphics.fill(x + 1, y + 1 + (inner - height), x + FRAME - 1, y + FRAME - 1, COLOR_COOLDOWN);
    }
}
