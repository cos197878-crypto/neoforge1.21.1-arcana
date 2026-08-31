package com.first.arcana.component;

import com.first.arcana.spell.SpellSlot;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * 스펠북 한 권이 담고 있는 스펠 목록. 아이템의 데이터 컴포넌트로 붙는다.
 * record 라서 불변이다 — 바꿀 때는 새 인스턴스를 만들어 스택에 다시 set 한다.
 */
public record SpellContainer(int maxSlots, List<SpellSlot> spells) {
    public static final int DEFAULT_SLOTS = 5;
    public static final SpellContainer EMPTY = new SpellContainer(DEFAULT_SLOTS, List.of());

    public static final Codec<SpellContainer> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.optionalFieldOf("max_slots", DEFAULT_SLOTS).forGetter(SpellContainer::maxSlots),
            SpellSlot.CODEC.listOf().optionalFieldOf("spells", List.of()).forGetter(SpellContainer::spells)
    ).apply(inst, SpellContainer::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpellContainer> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SpellContainer::maxSlots,
            SpellSlot.STREAM_CODEC.apply(ByteBufCodecs.list()), SpellContainer::spells,
            SpellContainer::new);

    public boolean isFull() {
        return spells.size() >= maxSlots;
    }

    @Nullable
    public SpellSlot get(int index) {
        return (index >= 0 && index < spells.size()) ? spells.get(index) : null;
    }

    /** 스펠을 추가한 새 컨테이너. 꽉 찼으면 자기 자신을 그대로 돌려준다. */
    public SpellContainer withAdded(SpellSlot slot) {
        if (isFull()) {
            return this;
        }
        List<SpellSlot> next = new ArrayList<>(spells);
        next.add(slot);
        return new SpellContainer(maxSlots, List.copyOf(next));
    }

    public SpellContainer withRemoved(int index) {
        if (index < 0 || index >= spells.size()) {
            return this;
        }
        List<SpellSlot> next = new ArrayList<>(spells);
        next.remove(index);
        return new SpellContainer(maxSlots, List.copyOf(next));
    }
}
