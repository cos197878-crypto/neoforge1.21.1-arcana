package com.first.arcana.block;

import com.first.arcana.Arcana;
import com.first.arcana.block.custom.InscriptionTableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Arcana.MOD_ID);

    /** 두루마리를 주문서에 새기는 각인 테이블. BlockItem 은 ModItems 쪽에서 등록한다. */
    public static final DeferredBlock<Block> INSCRIPTION_TABLE =
            BLOCKS.register("inscription_table", () -> new InscriptionTableBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(2.5F)
                            .sound(SoundType.WOOD)));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
