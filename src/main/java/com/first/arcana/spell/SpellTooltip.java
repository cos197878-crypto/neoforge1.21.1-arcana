package com.first.arcana.spell;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * 주문 툴팁을 조립한다. 두루마리와 주문서가 같은 배치를 쓰도록 여기 한 곳에만 둔다.
 *
 * 배치:
 *   레벨 3 (고급)          <- 회색 + 희귀도 색
 *   피해 6.5               <- 주문 고유 수치 (초록)
 *   반경 6 블록
 *   1.5s 시전 시간         <- 하늘색
 *
 *   주문서 내:
 *   마나 20                <- 파랑
 *   쿨타임 2s
 *
 *   화염                   <- 계열 색
 */
public class SpellTooltip {
    private static final ChatFormatting COLOR_LEVEL = ChatFormatting.GRAY;
    private static final ChatFormatting COLOR_INFO = ChatFormatting.GREEN;
    private static final ChatFormatting COLOR_CAST_TIME = ChatFormatting.AQUA;
    private static final ChatFormatting COLOR_HEADER = ChatFormatting.GRAY;
    private static final ChatFormatting COLOR_COST = ChatFormatting.BLUE;

    private static final String[] ROMAN = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    /** 두루마리처럼 주문 하나만 보여줄 때의 전체 툴팁. 아이템 이름 줄은 포함하지 않는다. */
    public static List<Component> full(AbstractSpell spell, int spellLevel) {
        List<Component> lines = new ArrayList<>();

        lines.add(Component.translatable("tooltip.arcana.level",
                spellLevel, SpellRarity.forLevel(spellLevel).getDisplayName()).withStyle(COLOR_LEVEL));

        for (Component info : spell.getUniqueInfo(spellLevel)) {
            lines.add(info.copy().withStyle(COLOR_INFO));
        }

        lines.add(castTimeLine(spell).withStyle(COLOR_CAST_TIME));

        lines.add(Component.empty());
        lines.add(Component.translatable("tooltip.arcana.in_spell_book").withStyle(COLOR_HEADER));
        lines.add(manaLine(spell, spellLevel).withStyle(COLOR_COST));
        lines.add(Component.translatable("tooltip.arcana.cooldown",
                format(spell.getCooldownTicks() / 20.0)).withStyle(COLOR_COST));

        lines.add(Component.empty());
        lines.add(Component.translatable(spell.getSchool().getTranslationKey())
                .withStyle(spell.getSchool().getColor()));

        return lines;
    }

    /** 주문서 목록에 한 줄로 넣을 때. "화염탄 III" 형태. */
    public static Component summary(AbstractSpell spell, int spellLevel) {
        return spell.getDisplayName().copy()
                .append(Component.literal(" " + toRoman(spellLevel)).withStyle(ChatFormatting.GRAY));
    }

    /** 소수점이 필요 없으면 정수로 보여준다. 6.0 -> "6", 6.5 -> "6.5" */
    public static String format(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.format("%.1f", value);
    }

    public static String toRoman(int n) {
        return (n >= 1 && n <= ROMAN.length) ? ROMAN[n - 1] : String.valueOf(n);
    }

    // ---------------- private ----------------

    private static MutableComponent castTimeLine(AbstractSpell spell) {
        if (spell.getCastTimeTicks() <= 0) {
            return Component.translatable("tooltip.arcana.instant_cast");
        }
        return Component.translatable("tooltip.arcana.cast_time", format(spell.getCastTimeTicks() / 20.0));
    }

    /** 지속 시전(CONTINUOUS)은 초당 소모라 문구가 다르다. */
    private static MutableComponent manaLine(AbstractSpell spell, int spellLevel) {
        String key = (spell.getCastType() == CastType.CONTINUOUS)
                ? "tooltip.arcana.mana_per_second"
                : "tooltip.arcana.mana_cost";
        return Component.translatable(key, spell.getManaCost(spellLevel));
    }
}
