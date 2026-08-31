package com.first.arcana.block.custom;

import com.first.arcana.menu.InscriptionTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 각인 테이블. 우클릭하면 주문서에 두루마리를 새기는 메뉴가 열린다.
 * 상태를 전부 메뉴가 (책 아이템의 컴포넌트로) 관리하므로 블록 엔티티가 필요 없다.
 */
public class InscriptionTableBlock extends Block {
    private static final Component TITLE = Component.translatable("container.arcana.inscription_table");

    public InscriptionTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new InscriptionTableMenu(id, inv, ContainerLevelAccess.create(level, pos)),
                    TITLE), buf -> { });
        }
        return InteractionResult.CONSUME;
    }
}
