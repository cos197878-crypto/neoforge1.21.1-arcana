package com.first.arcana.menu;

import com.first.arcana.Arcana;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, Arcana.MOD_ID);

    /** 서버가 보내는 추가 데이터(책이 든 칸 번호)를 받으므로 IMenuTypeExtension 으로 만든다. */
    public static final DeferredHolder<MenuType<?>, MenuType<SpellBookMenu>> SPELL_BOOK =
            MENU_TYPES.register("spell_book", () -> IMenuTypeExtension.create(
                    (windowId, inv, data) -> new SpellBookMenu(windowId, inv, data.readVarInt())));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
