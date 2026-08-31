package com.first.arcana.item;

import com.first.arcana.Arcana;
import com.first.arcana.block.ModBlocks;
import com.first.arcana.item.custom.ScrollItem;
import com.first.arcana.item.custom.SpellBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Arcana.MOD_ID);

    public static final DeferredItem<Item> SPELL_BOOK =
            ITEMS.register("spell_book", () -> new SpellBookItem(
                    new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<Item> SCROLL =
            ITEMS.register("scroll", () -> new ScrollItem(
                    new Item.Properties().stacksTo(16)));

    public static final DeferredItem<Item> MANA_CRYSTAL =
            ITEMS.registerSimpleItem("mana_crystal", new Item.Properties());

    /** 각인 테이블 블록 아이템. 번역 키가 block.arcana.inscription_table 로 잡힌다. */
    public static final DeferredItem<net.minecraft.world.item.BlockItem> INSCRIPTION_TABLE =
            ITEMS.registerSimpleBlockItem("inscription_table", ModBlocks.INSCRIPTION_TABLE);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
