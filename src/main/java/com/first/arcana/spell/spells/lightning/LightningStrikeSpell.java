package com.first.arcana.spell.spells.lightning;

import com.first.arcana.spell.AbstractSpell;
import com.first.arcana.spell.CastType;
import com.first.arcana.spell.SchoolType;
import com.first.arcana.spell.SpellTooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** 조준한 지점에 번개를 떨어뜨린다. */
public class LightningStrikeSpell extends AbstractSpell {
    private static final double RANGE = 24.0;
    private static final float BASE_DAMAGE = 6.0F;
    private static final float DAMAGE_PER_LEVEL = 2.0F;

    public LightningStrikeSpell() {
        super(SchoolType.LIGHTNING, CastType.INSTANT, 45, 10, 200, 10, 5);
    }

    @Override
    public List<Component> getUniqueInfo(int spellLevel) {
        return List.of(
                Component.translatable("tooltip.arcana.damage", SpellTooltip.format(getDamage(spellLevel))),
                Component.translatable("tooltip.arcana.range", SpellTooltip.format(RANGE))
        );
    }

    public float getDamage(int spellLevel) {
        return BASE_DAMAGE + DAMAGE_PER_LEVEL * (spellLevel - 1);
    }

    @Override
    protected void onCast(Level level, Player player, int spellLevel) {
        Vec3 eye = player.getEyePosition();
        Vec3 target = eye.add(player.getLookAngle().scale(RANGE));

        // 시선이 블록에 막히면 그 지점에, 아니면 최대 사거리 지점에 떨어뜨린다.
        BlockHitResult hit = level.clip(new ClipContext(
                eye, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 strikePos = hit.getLocation();

        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) {
            return;
        }
        bolt.moveTo(strikePos.x, strikePos.y, strikePos.z);
        bolt.setDamage(getDamage(spellLevel));
        if (player instanceof ServerPlayer serverPlayer) {
            // 주의: cause 는 피해 귀속이 아니다 (소스 확인: thunderHit 의 데미지 소스에 공격자가 없다).
            // 발전과제(CHANNELED_LIGHTNING) 판정에만 쓰인다. 이 주문의 킬은 플레이어 킬로 집계되지 않는다.
            bolt.setCause(serverPlayer);
        }
        level.addFreshEntity(bolt);
    }
}
