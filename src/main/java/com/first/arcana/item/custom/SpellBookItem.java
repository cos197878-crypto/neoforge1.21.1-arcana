package com.first.arcana.item.custom;

import com.first.arcana.component.ModDataComponents;
import com.first.arcana.component.SpellContainer;
import com.first.arcana.spell.AbstractSpell;
import com.first.arcana.spell.SpellSlot;
import com.first.arcana.spell.SpellTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 주문 여러 개를 담아두고 골라 쓰는 책.
 * 우클릭 -> 선택된 주문 시전
 * 웅크리기 + 우클릭 -> 다음 주문으로 전환
 */
public class SpellBookItem extends Item {
    private static final String SELECTED_MARKER = " > ";
    private static final String UNSELECTED_MARKER = "   ";

    public SpellBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            cycleSelected(stack);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        SpellSlot slot = getSelectedSlot(stack);
        if (slot == null) {
            return InteractionResultHolder.fail(stack);
        }

        // 실제 효과는 서버에서만. 클라이언트는 손 흔들기만 한다.
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.success(stack);
        }

        AbstractSpell spell = slot.spell();
        if (spell == null || !spell.tryCast(serverPlayer, slot.level())) {
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.success(stack);
    }

    /** 목록을 먼저 보여주고, 그 아래에 선택된 주문의 상세를 두루마리와 같은 배치로 붙인다. */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.arcana.spell_book_subtitle").withStyle(ChatFormatting.AQUA));

        SpellContainer container = getContainer(stack);
        if (container.spells().isEmpty()) {
            tooltip.add(Component.translatable("tooltip.arcana.empty_spell_book").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        int selected = getSelectedIndex(stack);
        appendSpellList(tooltip, container, selected);

        SpellSlot slot = container.get(selected);
        AbstractSpell spell = (slot == null) ? null : slot.spell();
        if (spell != null) {
            tooltip.add(Component.empty());
            tooltip.addAll(SpellTooltip.full(spell, slot.level()));
        }
    }

    // ---------------- 공개 헬퍼 ----------------

    public static SpellContainer getContainer(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.SPELL_CONTAINER.get(), SpellContainer.EMPTY);
    }

    public static void setContainer(ItemStack stack, SpellContainer container) {
        stack.set(ModDataComponents.SPELL_CONTAINER.get(), container);
    }

    public static int getSelectedIndex(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.SELECTED_SLOT.get(), 0);
    }

    @Nullable
    public static SpellSlot getSelectedSlot(ItemStack stack) {
        return getContainer(stack).get(getSelectedIndex(stack));
    }

    // ---------------- private ----------------

    private static void appendSpellList(List<Component> tooltip, SpellContainer container, int selected) {
        for (int i = 0; i < container.spells().size(); i++) {
            SpellSlot slot = container.spells().get(i);
            AbstractSpell spell = slot.spell();
            if (spell == null) {
                continue;
            }
            Component marker = Component.literal(i == selected ? SELECTED_MARKER : UNSELECTED_MARKER)
                    .withStyle(ChatFormatting.YELLOW);
            tooltip.add(marker.copy().append(SpellTooltip.summary(spell, slot.level())));
        }
    }

    private static void cycleSelected(ItemStack stack) {
        SpellContainer container = getContainer(stack);
        if (container.spells().isEmpty()) {
            return;
        }
        int next = (getSelectedIndex(stack) + 1) % container.spells().size();
        stack.set(ModDataComponents.SELECTED_SLOT.get(), next);
    }
}
