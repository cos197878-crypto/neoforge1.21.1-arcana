package com.first.arcana.spell;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * 주문 레벨에서 유도되는 희귀도. 아이템 자체의 Rarity 와는 별개로 툴팁 표시용이다.
 * 레벨 구간은 여기 한 곳에서만 정한다.
 */
public enum SpellRarity {
    COMMON("common", ChatFormatting.GRAY),
    UNCOMMON("uncommon", ChatFormatting.GREEN),
    RARE("rare", ChatFormatting.AQUA),
    EPIC("epic", ChatFormatting.LIGHT_PURPLE),
    LEGENDARY("legendary", ChatFormatting.GOLD);

    private final String name;
    private final ChatFormatting color;

    SpellRarity(String name, ChatFormatting color) {
        this.name = name;
        this.color = color;
    }

    public ChatFormatting getColor() {
        return color;
    }

    /** lang 키: rarity.arcana.rare */
    public String getTranslationKey() {
        return "rarity.arcana." + name;
    }

    public Component getDisplayName() {
        return Component.translatable(getTranslationKey()).withStyle(color);
    }

    /** 레벨 -> 희귀도. 2레벨마다 한 단계씩 올라간다. */
    public static SpellRarity forLevel(int spellLevel) {
        if (spellLevel <= 2) {
            return COMMON;
        }
        if (spellLevel <= 4) {
            return UNCOMMON;
        }
        if (spellLevel <= 6) {
            return RARE;
        }
        if (spellLevel <= 8) {
            return EPIC;
        }
        return LEGENDARY;
    }
}
