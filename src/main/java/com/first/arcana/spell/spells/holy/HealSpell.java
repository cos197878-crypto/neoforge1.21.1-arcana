package com.first.arcana.spell.spells.holy;

import com.first.arcana.spell.AbstractSpell;
import com.first.arcana.spell.CastType;
import com.first.arcana.spell.SchoolType;
import com.first.arcana.spell.SpellTooltip;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

/** 자기 자신을 회복한다. */
public class HealSpell extends AbstractSpell {
    private static final float BASE_HEAL = 4.0F;
    private static final float HEAL_PER_LEVEL = 2.0F;

    public HealSpell() {
        super(SchoolType.HOLY, CastType.INSTANT, 30, 8, 160, 20, 5);
    }

    @Override
    public List<Component> getUniqueInfo(int spellLevel) {
        return List.of(
                Component.translatable("tooltip.arcana.heal", SpellTooltip.format(getHealAmount(spellLevel)))
        );
    }

    public float getHealAmount(int spellLevel) {
        return BASE_HEAL + HEAL_PER_LEVEL * (spellLevel - 1);
    }

    @Override
    protected void onCast(Level level, Player player, int spellLevel) {
        player.heal(getHealAmount(spellLevel));

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HEART,
                    player.getX(), player.getY() + 1.0, player.getZ(), 6, 0.4, 0.5, 0.4, 0.0);
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 0.5, player.getZ(), 12, 0.3, 0.6, 0.3, 0.02);
        }
        level.playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.6F);
    }
}
