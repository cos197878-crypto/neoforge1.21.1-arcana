package com.first.arcana.entity;

import com.first.arcana.Arcana;
import com.first.arcana.entity.custom.FireballProjectile;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Arcana.MOD_ID);

    /** 파이어볼 주문이 발사하는 화염구 */
    public static final DeferredHolder<EntityType<?>, EntityType<FireballProjectile>> FIREBALL =
            ENTITY_TYPES.register("fireball",
                    () -> EntityType.Builder.<FireballProjectile>of(FireballProjectile::new, MobCategory.MISC)
                            .sized(0.6F, 0.6F)       // 판정 크기
                            .clientTrackingRange(8)  // 청크 단위 표시 거리
                            .updateInterval(1)       // 빠르게 날아가므로 매 틱 동기화
                            .build("fireball"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
