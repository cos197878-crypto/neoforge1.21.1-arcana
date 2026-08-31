package com.first.arcana.spell;

import net.minecraft.ChatFormatting;

/**
 * 스펠의 계열. 툴팁 색과 두루마리 텍스처를 정한다.
 *
 * modelIndex 는 assets/arcana/models/item/scroll.json 의 "arcana:school" predicate 값과
 * 반드시 일치해야 한다. 계열을 추가할 때는 여기와 scroll.json 을 같이 고친다.
 */
public enum SchoolType {
    FIRE("fire", 1, ChatFormatting.RED),
    ICE("ice", 2, ChatFormatting.AQUA),
    LIGHTNING("lightning", 3, ChatFormatting.YELLOW),
    HOLY("holy", 4, ChatFormatting.GOLD),
    BLOOD("blood", 5, ChatFormatting.DARK_RED),
    EVOCATION("evocation", 6, ChatFormatting.LIGHT_PURPLE);

    private final String name;
    private final int modelIndex;
    private final ChatFormatting color;

    SchoolType(String name, int modelIndex, ChatFormatting color) {
        this.name = name;
        this.modelIndex = modelIndex;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    /** scroll.json 의 predicate 값. 0 은 계열 없음(빈 두루마리)이라 1부터 시작한다. */
    public int getModelIndex() {
        return modelIndex;
    }

    public ChatFormatting getColor() {
        return color;
    }

    /** lang 키: school.arcana.fire */
    public String getTranslationKey() {
        return "school.arcana." + name;
    }
}
