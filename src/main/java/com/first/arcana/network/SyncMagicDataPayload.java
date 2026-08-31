package com.first.arcana.network;

import com.first.arcana.Arcana;
import com.first.arcana.attachment.MagicData;
import com.first.arcana.attachment.ModAttachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 서버 -> 클라이언트. 마나/쿨다운을 HUD에 그리기 위해 밀어준다.
 * 클라이언트는 이 값을 표시에만 쓰고, 판정은 언제나 서버가 한다.
 */
public record SyncMagicDataPayload(MagicData data) implements CustomPacketPayload {
    public static final Type<SyncMagicDataPayload> TYPE = new Type<>(Arcana.id("sync_magic_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncMagicDataPayload> STREAM_CODEC =
            StreamCodec.composite(
                    MagicData.STREAM_CODEC, SyncMagicDataPayload::data,
                    SyncMagicDataPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 클라이언트에서 실행된다. context.player() 가 로컬 플레이어라
     * net.minecraft.client 클래스를 전혀 건드리지 않아도 된다.
     */
    public static void handle(SyncMagicDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            player.getData(ModAttachments.MAGIC_DATA).copyFrom(payload.data());
        });
    }
}
