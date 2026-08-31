package com.first.arcana.component;

import com.first.arcana.Arcana;
import com.first.arcana.spell.SpellSlot;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** 아이템에 붙는 커스텀 데이터(구 NBT). */
public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Arcana.MOD_ID);

    /** 스펠북이 담고 있는 스펠 목록 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SpellContainer>> SPELL_CONTAINER =
            COMPONENTS.register("spell_container", () -> DataComponentType.<SpellContainer>builder()
                    .persistent(SpellContainer.CODEC)
                    .networkSynchronized(SpellContainer.STREAM_CODEC)
                    .build());

    /** 스크롤 한 장이 담고 있는 스펠 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SpellSlot>> SPELL_SLOT =
            COMPONENTS.register("spell_slot", () -> DataComponentType.<SpellSlot>builder()
                    .persistent(SpellSlot.CODEC)
                    .networkSynchronized(SpellSlot.STREAM_CODEC)
                    .build());

    /** 스펠북에서 현재 선택된 칸 번호 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SELECTED_SLOT =
            COMPONENTS.register("selected_slot", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static void register(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }
}
