package com.first.arcana.network;

import com.first.arcana.Arcana;
import com.first.arcana.item.custom.SpellBookItem;
import com.first.arcana.spell.AbstractSpell;
import com.first.arcana.spell.SpellRegistry;
import com.first.arcana.spell.SpellSlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 클라이언트 -> 서버. 키를 눌러 시전을 요청한다.
 *
 * 클라이언트가 보내는 값은 전부 거짓일 수 있다고 가정한다.
 * 그래서 "이 플레이어가 정말 그 스펠을 들고 있는가"를 서버에서 다시 확인한다.
 */
public record CastSpellPayload(ResourceLocation spellId, int spellLevel) implements CustomPacketPayload {
    public static final Type<CastSpellPayload> TYPE = new Type<>(Arcana.id("cast_spell"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CastSpellPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, CastSpellPayload::spellId,
                    ByteBufCodecs.VAR_INT, CastSpellPayload::spellLevel,
                    CastSpellPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CastSpellPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            SpellSlot held = findHeldSlot(player, payload.spellId());
            if (held == null) {
                return;
            }
            AbstractSpell spell = SpellRegistry.get(held.spellId());
            if (spell != null) {
                spell.tryCast(player, held.level());
            }
        });
    }

    /** 양손 중 하나에 든 스펠북에 해당 스펠이 실제로 들어 있는지 확인한다. */
    private static SpellSlot findHeldSlot(Player player, ResourceLocation spellId) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof SpellBookItem)) {
                continue;
            }
            for (SpellSlot slot : SpellBookItem.getContainer(stack).spells()) {
                if (slot.spellId().equals(spellId)) {
                    return slot;
                }
            }
        }
        return null;
    }
}
