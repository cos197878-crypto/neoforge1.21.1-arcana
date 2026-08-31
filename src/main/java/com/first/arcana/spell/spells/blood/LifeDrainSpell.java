package com.first.arcana.spell.spells.blood;

import com.first.arcana.spell.AbstractSpell;
import com.first.arcana.spell.CastType;
import com.first.arcana.spell.SchoolType;
import com.first.arcana.spell.SpellTooltip;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** 조준한 대상의 생명력을 빼앗아 절반만큼 회복한다. */
public class LifeDrainSpell extends AbstractSpell {
    private static final double RANGE = 8.0;
    private static final float BASE_DAMAGE = 4.0F;
    private static final float DAMAGE_PER_LEVEL = 1.5F;
    /** 흡수한 피해 중 회복으로 돌아오는 비율 */
    private static final float DRAIN_RATIO = 0.5F;
    /** 조준 판정을 살짝 넉넉하게 잡는 범위 */
    private static final double AIM_INFLATE = 0.5;

    public LifeDrainSpell() {
        super(SchoolType.BLOOD, CastType.INSTANT, 25, 6, 120, 10, 5);
    }

    @Override
    public List<Component> getUniqueInfo(int spellLevel) {
        return List.of(
                Component.translatable("tooltip.arcana.damage", SpellTooltip.format(getDamage(spellLevel))),
                Component.translatable("tooltip.arcana.drain",
                        SpellTooltip.format(getDamage(spellLevel) * DRAIN_RATIO)),
                Component.translatable("tooltip.arcana.range", SpellTooltip.format(RANGE))
        );
    }

    public float getDamage(int spellLevel) {
        return BASE_DAMAGE + DAMAGE_PER_LEVEL * (spellLevel - 1);
    }

    @Override
    protected void onCast(Level level, Player player, int spellLevel) {
        LivingEntity target = findTarget(level, player);
        if (target == null) {
            return;
        }

        // hurt 는 무적시간(i-frame) 중이면 false 를 돌려주고 피해를 주지 않는다.
        // 그때 회복까지 해버리면 공짜 힐이 되므로 성공했을 때만 흡수한다.
        float damage = getDamage(spellLevel);
        if (!target.hurt(player.damageSources().indirectMagic(player, player), damage)) {
            return;
        }
        player.heal(damage * DRAIN_RATIO);

        spawnDrainLine(level, player, target);
        level.playSound(null, target.blockPosition(),
                SoundEvents.WITCH_DRINK, SoundSource.PLAYERS, 0.8F, 0.7F);
    }

    private LivingEntity findTarget(Level level, Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(RANGE));
        AABB searchBox = player.getBoundingBox().expandTowards(end.subtract(start)).inflate(AIM_INFLATE);

        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, start, end, searchBox,
                entity -> entity instanceof LivingEntity && entity != player && entity.isAlive()
                        && entity.isPickable(),
                start.distanceToSqr(end));

        // findTarget 의 필터가 LivingEntity 만 통과시키므로 이 캐스트는 안전하다.
        return (hit == null) ? null : (LivingEntity) hit.getEntity();
    }

    /** 대상에서 시전자 쪽으로 빨려오는 파티클 선 */
    private void spawnDrainLine(Level level, Player player, LivingEntity target) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 from = target.position().add(0, target.getBbHeight() / 2.0, 0);
        Vec3 to = player.getEyePosition();
        int points = 10;
        for (int i = 0; i < points; i++) {
            Vec3 p = from.lerp(to, i / (double) points);
            serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.0);
        }
    }
}
