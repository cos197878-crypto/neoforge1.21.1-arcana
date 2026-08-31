package com.first.arcana.network;

import com.first.arcana.Arcana;
import com.first.arcana.component.ModDataComponents;
import com.first.arcana.component.SpellContainer;
import com.first.arcana.item.custom.SpellBookItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 클라이언트 -> 서버. V 키로 다음 주문을 선택한다.
 *
 * 선택 칸(SELECTED_SLOT)의 원본은 서버 스택이다. 클라이언트에서만 바꾸면
 * 우클릭 시전(서버가 선택 칸을 읽는다)과 어긋나므로 반드시 서버에 알린다.
 * 서버가 스택을 바꾸면 그 변경은 클라이언트로 자동 동기화된다.
 */
public record CycleSpellPayload() implements CustomPacketPayload {
    public static final Type<CycleSpellPayload> TYPE = new Type<>(Arcana.id("cycle_spell"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CycleSpellPayload> STREAM_CODEC =
            StreamCodec.unit(new CycleSpellPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CycleSpellPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!(stack.getItem() instanceof SpellBookItem)) {
                return;
            }
            SpellContainer container = SpellBookItem.getContainer(stack);
            if (container.spells().isEmpty()) {
                return;
            }
            int next = (SpellBookItem.getSelectedIndex(stack) + 1) % container.spells().size();
            stack.set(ModDataComponents.SELECTED_SLOT.get(), next);
        });
    }
}
