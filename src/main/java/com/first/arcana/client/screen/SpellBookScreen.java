package com.first.arcana.client.screen;

import com.first.arcana.Arcana;
import com.first.arcana.menu.SpellBookMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** 주문서 화면. 슬롯 로직은 전부 메뉴에 있고 여기는 그리기만 한다. */
public class SpellBookScreen extends AbstractContainerScreen<SpellBookMenu> {
    /** 256x256 텍스처의 좌상단에 176x166 패널이 그려져 있다 (tools/GuiGen.java 로 생성). */
    private static final ResourceLocation TEXTURE = Arcana.id("textures/gui/spell_book.png");

    public SpellBookScreen(SpellBookMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
