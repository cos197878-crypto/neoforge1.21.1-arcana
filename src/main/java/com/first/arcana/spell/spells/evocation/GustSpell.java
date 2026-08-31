package com.first.arcana.spell.spells.evocation;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** 전방의 적들을 강하게 밀쳐낸다. */
public class GustSpell extends AbstractSpell {
    private static final double RANGE = 5.0;
    private static final double WIDTH = 2.0;
    private static final float BASE_DAMAGE = 2.0F;
    private static final float DAMAGE_PER_LEVEL = 1.0F;
    private static final double BASE_KNOCKBACK = 1.2;
    private static final double KNOCKBACK_PER_LEVEL = 0.3;
    private static final double LIFT = 0.4;

    public GustSpell() {
        super(SchoolType.EVOCATION, CastType.INSTANT, 15, 4, 60, 0, 5);
    }

    @Override
    public List<Component> getUniqueInfo(int spellLevel) {
        return List.of(
                Component.translatable("tooltip.arcana.damage", SpellTooltip.format(getDamage(spellLevel))),
                Component.translatable("tooltip.arcana.knockback",
                        SpellTooltip.format(getKnockback(spellLevel)))
        );
    }

    public float getDamage(int spellLevel) {
        return BASE_DAMAGE + DAMAGE_PER_LEVEL * (spellLevel - 1);
    }

    public double getKnockback(int spellLevel) {
        return BASE_KNOCKBACK + KNOCKBACK_PER_LEVEL * (spellLevel - 1);
    }

    @Override
    protected void onCast(Level level, Player player, int spellLevel) {
        Vec3 look = player.getLookAngle();
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(look.scale(RANGE));

        AABB box = new AABB(start, end).inflate(WIDTH);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
                entity -> entity != player && entity.isAlive());

        double knockback = getKnockback(spellLevel);
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().indirectMagic(player, player), getDamage(spellLevel));
            target.push(look.x * knockback, LIFT, look.z * knockback);
            // push 만으로는 속도 변화가 클라이언트로 안 간다. 이 플래그가 동기화를 강제한다.
            target.hurtMarked = true;
        }

        spawnWave(level, start, look);
        level.playSound(null, player.blockPosition(),
                SoundEvents.BREEZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.2F);
    }

    private void spawnWave(Level level, Vec3 start, Vec3 look) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        for (double d = 1.0; d < RANGE; d += 1.0) {
            Vec3 p = start.add(look.scale(d));
            serverLevel.sendParticles(ParticleTypes.GUST, p.x, p.y, p.z, 1, 0.3, 0.3, 0.3, 0.0);
        }
    }
}
