package com.first.arcana.client.screen;

import com.first.arcana.Arcana;
import com.first.arcana.component.SpellContainer;
import com.first.arcana.item.ModItems;
import com.first.arcana.item.custom.ScrollItem;
import com.first.arcana.item.custom.SpellBookItem;
import com.first.arcana.menu.InscriptionTableMenu;
import com.first.arcana.spell.AbstractSpell;
import com.first.arcana.spell.CastType;
import com.first.arcana.spell.SpellSlot;
import com.first.arcana.spell.SpellTooltip;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 각인 테이블 화면 — Iron's Spells 방식.
 *
 * 책이 없으면 중앙은 빈 어둠뿐이다. 책을 놓으면 책 용량만큼 칸이 나타나고,
 * 클릭으로 주문을 선택하면 오른쪽 양피지에 상세가 표시된다.
 * Shift+클릭은 그 주문을 배출 칸으로 꺼낸다.
 *
 * 그리드는 아이템 슬롯이 아니라 그림이다 — 클릭은 바닐라 메뉴 버튼 패킷으로
 * 서버에 전달되고, 결과(책 컴포넌트 변경)는 슬롯 동기화로 돌아온다.
 */
public class InscriptionTableScreen extends AbstractContainerScreen<InscriptionTableMenu> {
    /** 256x256 텍스처의 좌상단에 254x166 패널 (tools/GuiGen.java 로 생성). */
    private static final ResourceLocation TEXTURE = Arcana.id("textures/gui/inscription_table.png");

    // 그리드 배치 — GuiGen 의 중앙 어두운 영역(60,18 ~ 168,82)과 짝이다.
    private static final int GRID_AREA_X = 60;
    private static final int GRID_AREA_Y = 18;
    private static final int GRID_AREA_W = 108;
    private static final int GRID_AREA_H = 64;
    private static final int CELL = 18;
    private static final int COLS = 4;

    // 양피지 패널 — GuiGen 의 (176,0 ~ 254,166)과 짝이다.
    private static final int PANEL_X = 176;
    private static final int PANEL_W = 78;

    private static final int COLOR_CELL_BG = 0xFF4A4436;
    private static final int COLOR_CELL_BORDER = 0xFF2A261E;
    private static final int COLOR_SELECTED = 0xFFF8C542;
    private static final int COLOR_HEADER = 0xFF3A2C1E;
    private static final int COLOR_TEXT_GRAY = 0xFF6B5B47;
    private static final int COLOR_MANA = 0xFF2A4BD7;
    private static final int COLOR_CAST = 0xFF1F7A8C;
    private static final int COLOR_INFO = 0xFF2E7D32;

    /** 렌더링은 매 프레임 돌므로, 컨테이너가 바뀔 때만 아이콘 스택을 다시 만든다. */
    private SpellContainer cachedContainer;
    private List<ItemStack> cachedStacks = List.of();

    public InscriptionTableScreen(InscriptionTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 254;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if (!this.menu.hasBook()) {
            return;
        }
        SpellContainer container = SpellBookItem.getContainer(this.menu.getBook());
        refreshCache(container);

        int cells = Math.min(container.maxSlots(), InscriptionTableMenu.MAX_DISPLAY_SLOTS);
        int selected = SpellBookItem.getSelectedIndex(this.menu.getBook());

        for (int i = 0; i < cells; i++) {
            int x = this.leftPos + cellX(i, cells);
            int y = this.topPos + cellY(i);
            boolean isSelected = i == selected && i < container.spells().size();

            guiGraphics.fill(x, y, x + CELL, y + CELL, isSelected ? COLOR_SELECTED : COLOR_CELL_BORDER);
            guiGraphics.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, COLOR_CELL_BG);
            if (i < cachedStacks.size()) {
                guiGraphics.renderFakeItem(cachedStacks.get(i), x + 1, y + 1);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderSelectionPanel(guiGraphics);
        renderGridTooltip(guiGraphics, mouseX, mouseY);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    // ---------------- 양피지 상세 패널 ----------------

    private void renderSelectionPanel(GuiGraphics guiGraphics) {
        int panelLeft = this.leftPos + PANEL_X;
        int centerX = panelLeft + PANEL_W / 2;

        drawCentered(guiGraphics, Component.translatable("screen.arcana.spell_selection"),
                centerX, this.topPos + 8, COLOR_HEADER);

        SpellSlot slot = selectedSlot();
        AbstractSpell spell = (slot == null) ? null : slot.spell();
        if (spell == null) {
            return;
        }

        int y = this.topPos + 26;
        drawCentered(guiGraphics, spell.getDisplayName(), centerX, y, 0xFFFFFFFF);
        y += 11;
        drawCentered(guiGraphics, Component.translatable("screen.arcana.level", slot.level()),
                centerX, y, COLOR_TEXT_GRAY);
        y += 16;

        int textX = panelLeft + 7;
        y = drawLine(guiGraphics, Component.translatable("tooltip.arcana.mana_cost",
                spell.getManaCost(slot.level())), textX, y, COLOR_MANA);
        Component castLine = (spell.getCastType() == CastType.INSTANT || spell.getCastTimeTicks() <= 0)
                ? Component.translatable("tooltip.arcana.instant_cast")
                : Component.translatable("tooltip.arcana.cast_time",
                        SpellTooltip.format(spell.getCastTimeTicks() / 20.0));
        y = drawLine(guiGraphics, castLine, textX, y, COLOR_CAST);
        y = drawLine(guiGraphics, Component.translatable("tooltip.arcana.cooldown",
                SpellTooltip.format(spell.getCooldownTicks() / 20.0)), textX, y, COLOR_MANA);
        y += 4;
        for (Component info : spell.getUniqueInfo(slot.level())) {
            y = drawLine(guiGraphics, info, textX, y, COLOR_INFO);
        }

        drawCentered(guiGraphics, Component.translatable("screen.arcana.extract_hint"),
                centerX, this.topPos + 126, COLOR_TEXT_GRAY);
    }

    /** 그리드 칸에 마우스를 올리면 주문 이름을 툴팁으로 띄운다. */
    private void renderGridTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int index = hoveredCell(mouseX, mouseY);
        if (index < 0 || index >= cachedStacks.size()) {
            return;
        }
        SpellSlot slot = SpellBookItem.getContainer(this.menu.getBook()).get(index);
        AbstractSpell spell = (slot == null) ? null : slot.spell();
        if (spell != null) {
            guiGraphics.renderTooltip(this.font, SpellTooltip.summary(spell, slot.level()), mouseX, mouseY);
        }
    }

    // ---------------- 그리드 클릭 ----------------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.menu.hasBook()) {
            int index = hoveredCell((int) mouseX, (int) mouseY);
            if (index >= 0) {
                SpellContainer container = SpellBookItem.getContainer(this.menu.getBook());
                if (index < container.spells().size()) {
                    int buttonId = Screen.hasShiftDown()
                            ? InscriptionTableMenu.EXTRACT_BUTTON_BASE + index
                            : index;
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // ---------------- private ----------------

    private SpellSlot selectedSlot() {
        if (!this.menu.hasBook()) {
            return null;
        }
        ItemStack book = this.menu.getBook();
        return SpellBookItem.getContainer(book).get(SpellBookItem.getSelectedIndex(book));
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

    /** i 번째 칸의 패널 기준 x. 마지막 줄은 남은 칸 수만큼 가운데 정렬한다. */
    private int cellX(int i, int cells) {
        int row = i / COLS;
        int col = i % COLS;
        int lastRow = (cells - 1) / COLS;
        int colsInRow = (row == lastRow) ? cells - row * COLS : COLS;
        int rowWidth = colsInRow * CELL;
        return GRID_AREA_X + (GRID_AREA_W - rowWidth) / 2 + col * CELL;
    }

    private int cellY(int i) {
        int cells = this.menu.hasBook()
                ? Math.min(SpellBookItem.getContainer(this.menu.getBook()).maxSlots(),
                        InscriptionTableMenu.MAX_DISPLAY_SLOTS)
                : 0;
        int rows = (cells + COLS - 1) / COLS;
        int gridHeight = rows * CELL;
        return GRID_AREA_Y + (GRID_AREA_H - gridHeight) / 2 + (i / COLS) * CELL;
    }

    /** 화면 좌표가 어느 그리드 칸 위인지. 아니면 -1. */
    private int hoveredCell(int mouseX, int mouseY) {
        if (!this.menu.hasBook()) {
            return -1;
        }
        SpellContainer container = SpellBookItem.getContainer(this.menu.getBook());
        int cells = Math.min(container.maxSlots(), InscriptionTableMenu.MAX_DISPLAY_SLOTS);
        for (int i = 0; i < cells; i++) {
            int x = this.leftPos + cellX(i, cells);
            int y = this.topPos + cellY(i);
            if (mouseX >= x && mouseX < x + CELL && mouseY >= y && mouseY < y + CELL) {
                return i;
            }
        }
        return -1;
    }

    private void drawCentered(GuiGraphics guiGraphics, Component text, int centerX, int y, int color) {
        int width = this.font.width(text);
        guiGraphics.drawString(this.font, text, centerX - width / 2, y, color, false);
    }

    private int drawLine(GuiGraphics guiGraphics, Component text, int x, int y, int color) {
        guiGraphics.drawString(this.font, text, x, y, color, false);
        return y + 10;
    }
}
