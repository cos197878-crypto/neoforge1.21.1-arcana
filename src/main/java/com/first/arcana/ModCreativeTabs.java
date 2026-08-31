package com.first.arcana;

import com.first.arcana.item.ModItems;
import com.first.arcana.item.custom.ScrollItem;
import com.first.arcana.spell.AbstractSpell;
import com.first.arcana.spell.SpellRegistry;
import com.first.arcana.spell.SpellSlot;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Arcana.MOD_ID);

    public static final Supplier<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + Arcana.MOD_ID + ".main"))
                    .icon(() -> new ItemStack(ModItems.SPELL_BOOK.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.INSCRIPTION_TABLE.get());
                        output.accept(ModItems.SPELL_BOOK.get());
                        output.accept(ModItems.MANA_CRYSTAL.get());
                        output.accept(ModItems.SCROLL.get());

                        // 등록된 모든 스펠에 대해 스크롤을 한 장씩 뿌린다.
                        // 스펠을 추가하면 여기는 손댈 필요가 없다.
                        for (AbstractSpell spell : SpellRegistry.SPELLS) {
                            ResourceLocation id = SpellRegistry.SPELLS.getKey(spell);
                            if (id == null) {
                                continue;
                            }
                            output.accept(ScrollItem.of(ModItems.SCROLL.get(), new SpellSlot(id, 1)));
                        }
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
