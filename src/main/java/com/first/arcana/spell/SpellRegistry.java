package com.first.arcana.spell;

import com.first.arcana.Arcana;
import com.first.arcana.spell.spells.fire.FireboltSpell;
import com.first.arcana.spell.spells.ice.FrostNovaSpell;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import javax.annotation.Nullable;

/**
 * 스펠 전용 커스텀 레지스트리.
 *
 * 스펠을 하나 추가하는 절차는 이 파일에서 상수 한 줄 늘리는 것으로 끝난다.
 * 상수 이름(FIREBOLT)과 등록 id("firebolt")는 항상 같은 단어로 맞춘다.
 */
public class SpellRegistry {
    // 1) 레지스트리 자체
    public static final ResourceKey<Registry<AbstractSpell>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(Arcana.id("spell"));

    public static final Registry<AbstractSpell> SPELLS =
            new RegistryBuilder<>(REGISTRY_KEY).sync(true).create();

    public static final DeferredRegister<AbstractSpell> SPELL_REGISTER =
            DeferredRegister.create(REGISTRY_KEY, Arcana.MOD_ID);

    // 2) 스펠 상수들
    public static final DeferredHolder<AbstractSpell, FireboltSpell> FIREBOLT =
            SPELL_REGISTER.register("firebolt", FireboltSpell::new);

    public static final DeferredHolder<AbstractSpell, FrostNovaSpell> FROST_NOVA =
            SPELL_REGISTER.register("frost_nova", FrostNovaSpell::new);

    // 3) 헬퍼
    @Nullable
    public static AbstractSpell get(ResourceLocation id) {
        return SPELLS.get(id);
    }

    // 4) 진입점에서 부르는 훅
    public static void onNewRegistry(NewRegistryEvent event) {
        event.register(SPELLS);
    }

    public static void register(IEventBus eventBus) {
        SPELL_REGISTER.register(eventBus);
    }
}
