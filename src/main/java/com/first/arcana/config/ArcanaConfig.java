package com.first.arcana.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** COMMON 설정. 값은 반드시 런타임에 읽는다 — 정적 초기화 시점에 읽으면 아직 로드 전이라 터진다. */
public class ArcanaConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_MANA = BUILDER
            .comment("플레이어 최대 마나")
            .defineInRange("maxMana", 100, 1, 100000);

    public static final ModConfigSpec.IntValue MANA_REGEN_INTERVAL = BUILDER
            .comment("마나가 회복되는 간격 (틱). 20틱 = 1초")
            .defineInRange("manaRegenInterval", 20, 1, 1200);

    public static final ModConfigSpec.IntValue MANA_REGEN_AMOUNT = BUILDER
            .comment("한 번에 회복되는 마나량")
            .defineInRange("manaRegenAmount", 2, 0, 10000);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
