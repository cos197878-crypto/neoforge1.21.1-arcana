package com.first.arcana.spell;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/** 스펠북 한 칸 / 스크롤 한 장에 담긴 "어떤 스펠을 몇 레벨로". */
public record SpellSlot(ResourceLocation spellId, int level) {
    public static final Codec<SpellSlot> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("spell").forGetter(SpellSlot::spellId),
            Codec.INT.optionalFieldOf("level", 1).forGetter(SpellSlot::level)
    ).apply(inst, SpellSlot::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpellSlot> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, SpellSlot::spellId,
            ByteBufCodecs.VAR_INT, SpellSlot::level,
            SpellSlot::new);

    /** 등록되지 않은 id가 저장돼 있을 수 있으므로 null 검사는 호출부의 책임이다. */
    @Nullable
    public AbstractSpell spell() {
        return SpellRegistry.get(spellId);
    }
}
