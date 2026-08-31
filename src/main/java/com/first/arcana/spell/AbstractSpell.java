package com.first.arcana.spell;

import com.first.arcana.attachment.MagicData;
import com.first.arcana.attachment.ModAttachments;
import com.first.arcana.network.SyncMagicDataPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * 모든 주문의 부모. 주문 하나 = 이 클래스의 서브클래스 하나 + SpellRegistry 등록 한 줄.
 *
 * 서브클래스가 반드시 구현할 것은 {@link #onCast} 뿐이고,
 * 마나/쿨다운/사이드 검사는 전부 {@link #tryCast} 가 처리한다.
 * 툴팁에 피해량 같은 고유 수치를 띄우려면 {@link #getUniqueInfo} 를 덮어쓴다.
 */
public abstract class AbstractSpell {
    private final SchoolType school;
    private final CastType castType;
    private final int baseManaCost;
    private final int manaCostPerLevel;
    private final int cooldownTicks;
    private final int castTimeTicks;
    private final int maxLevel;

    protected AbstractSpell(SchoolType school, CastType castType,
                            int baseManaCost, int manaCostPerLevel,
                            int cooldownTicks, int castTimeTicks, int maxLevel) {
        this.school = school;
        this.castType = castType;
        this.baseManaCost = baseManaCost;
        this.manaCostPerLevel = manaCostPerLevel;
        this.cooldownTicks = cooldownTicks;
        this.castTimeTicks = castTimeTicks;
        this.maxLevel = maxLevel;
    }

    // ---------------- 서브클래스가 구현하는 부분 ----------------

    /** 실제 효과. 항상 서버에서만 불린다. */
    protected abstract void onCast(Level level, Player player, int spellLevel);

    /**
     * 툴팁에 초록색으로 들어갈 주문 고유 수치 (피해량, 반경, 지속시간 등).
     * 색은 툴팁 조립부가 입히므로 여기서는 문구만 만든다.
     */
    public List<Component> getUniqueInfo(int spellLevel) {
        return List.of();
    }

    // ---------------- 공통 시전 흐름 ----------------

    /**
     * 마나·쿨다운을 검사하고 통과하면 시전한다.
     * @return 실제로 시전됐으면 true
     */
    public boolean tryCast(ServerPlayer player, int spellLevel) {
        MagicData data = player.getData(ModAttachments.MAGIC_DATA);
        ResourceLocation spellId = getSpellId();

        if (data.isOnCooldown(spellId)) {
            return false;
        }

        int cost = getManaCost(spellLevel);
        if (!data.spendMana(cost)) {
            player.displayClientMessage(Component.translatable("message.arcana.not_enough_mana")
                    .withStyle(ChatFormatting.RED), true);
            return false;
        }

        data.setCooldown(spellId, cooldownTicks);
        onCast(player.level(), player, spellLevel);
        PacketDistributor.sendToPlayer(player, new SyncMagicDataPayload(data));
        return true;
    }

    // ---------------- 값 조회 ----------------

    public int getManaCost(int spellLevel) {
        return baseManaCost + manaCostPerLevel * (spellLevel - 1);
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public int getCastTimeTicks() {
        return castTimeTicks;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public SchoolType getSchool() {
        return school;
    }

    public CastType getCastType() {
        return castType;
    }

    /** 이 주문이 레지스트리에 등록된 이름. 등록 전에 부르면 null 이 나온다. */
    public ResourceLocation getSpellId() {
        return SpellRegistry.SPELLS.getKey(this);
    }

    // ---------------- 표시용 ----------------

    /** lang 키: spell.arcana.firebolt */
    public String getTranslationKey() {
        ResourceLocation id = getSpellId();
        return "spell." + id.getNamespace() + "." + id.getPath();
    }

    public Component getDisplayName() {
        return Component.translatable(getTranslationKey()).withStyle(school.getColor());
    }
}
