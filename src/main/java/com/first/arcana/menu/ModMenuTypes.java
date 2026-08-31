package com.first.arcana.menu;

import com.first.arcana.Arcana;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, Arcana.MOD_ID);

    /** 클라이언트는 블록 위치 검증을 할 수 없으므로 NULL 접근을 쓴다 (바닐라 제작대와 같은 패턴). */
    public static final DeferredHolder<MenuType<?>, MenuType<InscriptionTableMenu>> INSCRIPTION_TABLE =
            MENU_TYPES.register("inscription_table", () -> IMenuTypeExtension.create(
                    (windowId, inv, data) -> new InscriptionTableMenu(windowId, inv, ContainerLevelAccess.NULL)));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
