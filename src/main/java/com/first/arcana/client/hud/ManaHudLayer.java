package com.first.arcana.client.hud;

import com.first.arcana.attachment.MagicData;
import com.first.arcana.attachment.ModAttachments;
import com.first.arcana.client.ModKeyMappings;
import com.first.arcana.item.custom.SpellBookItem;
import com.first.arcana.spell.AbstractSpell;
import com.first.arcana.spell.SpellSlot;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/**
 * 경험치 바 위에 마나 바를 그린다.
 * 텍스처 없이 사각형만으로 그리므로 png 가 없어도 바로 뜬다 — 나중에 텍스처로 교체하면 된다.
 */
public class ManaHudLayer implements LayeredDraw.Layer {
    private static final int BAR_WIDTH = 81;
    private static final int BAR_HEIGHT = 5;
    private static final int OFFSET_Y = 32;

    private static final int COLOR_BORDER = 0xFF000000;
    private static final int COLOR_EMPTY = 0xFF1B1B2F;
    private static final int COLOR_FILL = 0xFF4A6CF7;
    private static final int COLOR_TEXT = 0xFF9FB4FF;

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || player.isSpectator() || minecraft.options.hideGui) {
            return;
        }

        MagicData data = player.getData(ModAttachments.MAGIC_DATA);
        int max = Math.max(1, MagicData.getMaxMana());
        float ratio = Math.min(1.0F, data.getMana() / (float) max);

        int left = guiGraphics.guiWidth() / 2 - BAR_WIDTH / 2;
        int top = guiGraphics.guiHeight() - OFFSET_Y;

        drawBar(guiGraphics, left, top, ratio);
        drawManaText(guiGraphics, minecraft, left, top, data.getMana(), max);
        drawSelectedSpell(guiGraphics, minecraft, player, left, top);
    }

    private void drawBar(GuiGraphics guiGraphics, int left, int top, float ratio) {
        guiGraphics.fill(left - 1, top - 1, left + BAR_WIDTH + 1, top + BAR_HEIGHT + 1, COLOR_BORDER);
        guiGraphics.fill(left, top, left + BAR_WIDTH, top + BAR_HEIGHT, COLOR_EMPTY);
        guiGraphics.fill(left, top, left + (int) (BAR_WIDTH * ratio), top + BAR_HEIGHT, COLOR_FILL);
    }

    private void drawManaText(GuiGraphics guiGraphics, Minecraft minecraft, int left, int top, int mana, int max) {
        Component text = Component.literal(mana + " / " + max);
        int width = minecraft.font.width(text);
        guiGraphics.drawString(minecraft.font, text,
                left + BAR_WIDTH / 2 - width / 2, top - 10, COLOR_TEXT, true);
    }

    /** 손에 든 스펠북에서 현재 선택된 스펠 이름과 남은 쿨다운을 바 아래에 띄운다. */
    private void drawSelectedSpell(GuiGraphics guiGraphics, Minecraft minecraft, LocalPlayer player,
                                   int left, int top) {
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(stack.getItem() instanceof SpellBookItem)) {
            return;
        }

        SpellSlot slot = SpellBookItem.getSelectedSlot(stack);
        if (slot == null) {
            return;
        }
        AbstractSpell spell = slot.spell();
        if (spell == null) {
            return;
        }

        MagicData data = player.getData(ModAttachments.MAGIC_DATA);
        int cooldown = data.getCooldown(slot.spellId());

        Component text = (cooldown > 0)
                ? Component.translatable("hud.arcana.cooldown", String.format("%.1f", cooldown / 20.0F))
                : spell.getDisplayName();

        int width = minecraft.font.width(text);
        guiGraphics.drawString(minecraft.font, text,
                left + BAR_WIDTH / 2 - width / 2, top + BAR_HEIGHT + 3, 0xFFFFFFFF, true);
    }
}
