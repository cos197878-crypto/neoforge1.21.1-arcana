package com.first.arcana.event;

import com.first.arcana.Arcana;
import com.first.arcana.attachment.MagicData;
import com.first.arcana.attachment.ModAttachments;
import com.first.arcana.network.SyncMagicDataPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** 게임 버스 핸들러 — 마나 회복과 쿨다운 감소를 매 틱 돌린다. */
@EventBusSubscriber(modid = Arcana.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        MagicData data = serverPlayer.getData(ModAttachments.MAGIC_DATA);
        boolean manaChanged = data.tickRegen();
        boolean cooldownExpired = data.tickCooldowns();

        // 매 틱 보내면 낭비다. 값이 실제로 바뀐 틱에만 동기화한다.
        if (manaChanged || cooldownExpired) {
            sync(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            sync(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            sync(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            sync(serverPlayer);
        }
    }

    private static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new SyncMagicDataPayload(player.getData(ModAttachments.MAGIC_DATA)));
    }
}
