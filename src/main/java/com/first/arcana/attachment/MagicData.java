package com.first.arcana.attachment;

import com.first.arcana.config.ArcanaConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 플레이어 한 명의 마법 상태 — 마나와 스펠별 쿨다운.
 * 서버가 원본이고, 클라이언트에는 HUD 표시용으로 패킷으로 밀어준다.
 */
public class MagicData {
    public static final Codec<MagicData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.optionalFieldOf("mana", 0).forGetter(MagicData::getMana),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT)
                    .optionalFieldOf("cooldowns", Map.of()).forGetter(MagicData::getCooldowns)
    ).apply(inst, MagicData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MagicData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MagicData::getMana,
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_INT),
            MagicData::getCooldowns,
            MagicData::new);

    private int mana;
    private final Map<ResourceLocation, Integer> cooldowns;
    private int regenTimer;

    public MagicData() {
        this(ArcanaConfig.MAX_MANA.getAsInt(), Map.of());
    }

    public MagicData(int mana, Map<ResourceLocation, Integer> cooldowns) {
        this.mana = mana;
        this.cooldowns = new HashMap<>(cooldowns);
    }

    // ---------------- 마나 ----------------

    public int getMana() {
        return mana;
    }

    public void setMana(int mana) {
        this.mana = Math.max(0, Math.min(mana, getMaxMana()));
    }

    public static int getMaxMana() {
        return ArcanaConfig.MAX_MANA.getAsInt();
    }

    /** 마나가 충분하면 소모하고 true. 부족하면 아무것도 안 하고 false. */
    public boolean spendMana(int amount) {
        if (mana < amount) {
            return false;
        }
        mana -= amount;
        return true;
    }

    /**
     * 매 틱 호출. 설정된 간격마다 마나를 회복한다.
     * @return 값이 실제로 바뀌었으면 true (그럴 때만 동기화 패킷을 보내면 된다)
     */
    public boolean tickRegen() {
        if (mana >= getMaxMana()) {
            regenTimer = 0;
            return false;
        }
        if (++regenTimer < ArcanaConfig.MANA_REGEN_INTERVAL.getAsInt()) {
            return false;
        }
        regenTimer = 0;
        setMana(mana + ArcanaConfig.MANA_REGEN_AMOUNT.getAsInt());
        return true;
    }

    // ---------------- 쿨다운 ----------------

    public Map<ResourceLocation, Integer> getCooldowns() {
        return cooldowns;
    }

    public boolean isOnCooldown(ResourceLocation spellId) {
        return cooldowns.getOrDefault(spellId, 0) > 0;
    }

    public int getCooldown(ResourceLocation spellId) {
        return cooldowns.getOrDefault(spellId, 0);
    }

    public void setCooldown(ResourceLocation spellId, int ticks) {
        if (ticks <= 0) {
            cooldowns.remove(spellId);
        } else {
            cooldowns.put(spellId, ticks);
        }
    }

    /**
     * 매 틱 호출. 모든 쿨다운을 1씩 줄인다.
     * @return 하나라도 0이 되어 사라졌으면 true
     */
    public boolean tickCooldowns() {
        if (cooldowns.isEmpty()) {
            return false;
        }
        boolean expired = false;
        Iterator<Map.Entry<ResourceLocation, Integer>> it = cooldowns.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ResourceLocation, Integer> entry = it.next();
            int remaining = entry.getValue() - 1;
            if (remaining <= 0) {
                it.remove();
                expired = true;
            } else {
                entry.setValue(remaining);
            }
        }
        return expired;
    }

    // ---------------- 동기화 ----------------

    /** 서버에서 받은 값으로 통째로 덮어쓴다. 클라이언트에서만 쓴다. */
    public void copyFrom(MagicData other) {
        this.mana = other.mana;
        this.cooldowns.clear();
        this.cooldowns.putAll(other.cooldowns);
    }
}
