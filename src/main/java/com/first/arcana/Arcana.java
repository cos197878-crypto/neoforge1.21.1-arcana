package com.first.arcana;

import com.first.arcana.attachment.ModAttachments;
import com.first.arcana.component.ModDataComponents;
import com.first.arcana.config.ArcanaConfig;
import com.first.arcana.entity.ModEntityTypes;
import com.first.arcana.item.ModItems;
import com.first.arcana.spell.SpellRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(Arcana.MOD_ID)
public class Arcana {
    public static final String MOD_ID = "arcana";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public Arcana(IEventBus modEventBus, ModContainer modContainer) {
        // 커스텀 레지스트리(스펠)는 다른 등록보다 먼저 만들어져야 한다.
        modEventBus.addListener(SpellRegistry::onNewRegistry);

        SpellRegistry.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModItems.register(modEventBus);
        ModEntityTypes.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModAttachments.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, ArcanaConfig.SPEC);
    }
}
