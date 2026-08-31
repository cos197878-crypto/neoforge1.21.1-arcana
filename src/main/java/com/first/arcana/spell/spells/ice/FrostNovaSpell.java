package com.first.arcana.spell.spells.ice;

import com.first.arcana.spell.AbstractSpell;
import com.first.arcana.spell.CastType;
import com.first.arcana.spell.SchoolType;
import com.first.arcana.spell.SpellTooltip;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/** 자기 주위를 얼려 피해를 주고 둔화시키는 광역기. */
public class FrostNovaSpell extends AbstractSpell {
    private static final double BASE_RADIUS = 4.0;
    private static final double RADIUS_PER_LEVEL = 0.75;
    private static final float BASE_DAMAGE = 3.0F;
    private static final float DAMAGE_PER_LEVEL = 1.5F;
    private static final int SLOW_TICKS = 100;
    private static final int RING_POINTS = 40;

    public FrostNovaSpell() {
        super(SchoolType.ICE, CastType.INSTANT, 35, 8, 120, 20, 5);
    }

    @Override
    public List<Component> getUniqueInfo(int spellLevel) {
        return List.of(
                Component.translatable("tooltip.arcana.damage", SpellTooltip.format(getDamage(spellLevel))),
                Component.translatable("tooltip.arcana.radius", SpellTooltip.format(getRadius(spellLevel))),
                Component.translatable("tooltip.arcana.slow", SpellTooltip.format(SLOW_TICKS / 20.0), spellLevel)
        );
    }

    public float getDamage(int spellLevel) {
        return BASE_DAMAGE + DAMAGE_PER_LEVEL * (spellLevel - 1);
    }

    public double getRadius(int spellLevel) {
        return BASE_RADIUS + RADIUS_PER_LEVEL * (spellLevel - 1);
    }

    @Override
    protected void onCast(Level level, Player player, int spellLevel) {
        double radius = getRadius(spellLevel);

        AABB box = player.getBoundingBox().inflate(radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
                entity -> entity != player && entity.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().indirectMagic(player, player), getDamage(spellLevel));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOW_TICKS, spellLevel - 1));
        }

        spawnRing(level, player, radius);
        level.playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.4F);
    }

    private void spawnRing(Level level, Player player, double radius) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        for (int i = 0; i < RING_POINTS; i++) {
            double angle = (Math.PI * 2 / RING_POINTS) * i;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, x, player.getY() + 0.2, z, 3, 0.1, 0.1, 0.1, 0.0);
        }
    }
}
