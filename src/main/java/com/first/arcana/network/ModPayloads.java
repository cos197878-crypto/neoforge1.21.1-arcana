package com.first.arcana.network;

import com.first.arcana.Arcana;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** 패킷 등록. 모드 버스 이벤트라 bus = MOD 를 반드시 명시한다 (NeoForge 21.1 기준). */
@EventBusSubscriber(modid = Arcana.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModPayloads {
    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToServer(
                CastSpellPayload.TYPE,
                CastSpellPayload.STREAM_CODEC,
                CastSpellPayload::handle);

        registrar.playToClient(
                SyncMagicDataPayload.TYPE,
                SyncMagicDataPayload.STREAM_CODEC,
                SyncMagicDataPayload::handle);
    }
}
