package com.first.arcana.client;

import com.first.arcana.Arcana;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/**
 * 클라이언트 전용 진입점.
 * 전용 서버에는 net.minecraft.client 가 아예 없으므로, 렌더링/키/화면 관련 코드는
 * 전부 이 client 패키지 안에서만 참조한다.
 */
@Mod(value = Arcana.MOD_ID, dist = Dist.CLIENT)
public class ArcanaClient {
    public ArcanaClient(ModContainer container, IEventBus modEventBus) {
        // 클라이언트 전용 배선이 필요해지면 여기에 추가한다.
    }
}
