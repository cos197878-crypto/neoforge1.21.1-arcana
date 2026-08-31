package com.first.arcana.client.screen;

import com.first.arcana.Arcana;
import com.first.arcana.menu.InscriptionTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** 각인 테이블 화면. 슬롯 로직은 전부 메뉴에 있고 여기는 그리기만 한다. */
public class InscriptionTableScreen extends AbstractContainerScreen<InscriptionTableMenu> {
    /** 256x256 텍스처의 좌상단에 176x166 패널 (tools/GuiGen.java 로 생성). */
    private static final ResourceLocation TEXTURE = Arcana.id("textures/gui/inscription_table.png");

    public InscriptionTableScreen(InscriptionTableMenu menu, Inventory playerInventory, Component title) {
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
