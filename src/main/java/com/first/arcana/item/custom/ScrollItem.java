package com.first.arcana.item.custom;

import com.first.arcana.component.ModDataComponents;
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

/** 주문 한 개를 담은 일회용 두루마리. 우클릭하면 시전되고 소모된다. */
public class ScrollItem extends Item {
    public ScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        SpellSlot slot = getSlot(stack);
        if (slot == null) {
            return InteractionResultHolder.fail(stack);
        }

        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.success(stack);
        }

        AbstractSpell spell = slot.spell();
        if (spell == null || !spell.tryCast(serverPlayer, slot.level())) {
            return InteractionResultHolder.fail(stack);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        SpellSlot slot = getSlot(stack);
        AbstractSpell spell = (slot == null) ? null : slot.spell();
        if (spell == null) {
            return super.getName(stack);
        }
        return Component.translatable("item.arcana.scroll.named", spell.getDisplayName());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // 아이템 종류를 알려주는 부제. 이름 바로 아래에 온다.
        tooltip.add(Component.translatable("tooltip.arcana.scroll_subtitle").withStyle(ChatFormatting.AQUA));

        SpellSlot slot = getSlot(stack);
        AbstractSpell spell = (slot == null) ? null : slot.spell();
        if (spell == null) {
            tooltip.add(Component.translatable("tooltip.arcana.blank_scroll").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        tooltip.addAll(SpellTooltip.full(spell, slot.level()));
    }

    // ---------------- 공개 헬퍼 ----------------

    @Nullable
    public static SpellSlot getSlot(ItemStack stack) {
        return stack.get(ModDataComponents.SPELL_SLOT.get());
    }

    /** 특정 주문이 담긴 두루마리 스택을 만든다. */
    public static ItemStack of(Item scrollItem, SpellSlot slot) {
        ItemStack stack = new ItemStack(scrollItem);
        stack.set(ModDataComponents.SPELL_SLOT.get(), slot);
        return stack;
    }
}
