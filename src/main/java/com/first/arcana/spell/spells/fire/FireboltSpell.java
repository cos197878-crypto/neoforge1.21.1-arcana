package com.first.arcana.spell.spells.fire;

import com.first.arcana.entity.custom.FireballProjectile;
import com.first.arcana.spell.AbstractSpell;
import com.first.arcana.spell.CastType;
import com.first.arcana.spell.SchoolType;
import com.first.arcana.spell.SpellTooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** 시선 방향으로 화염구를 날린다. 맞은 대상에 피해 + 점화. */
public class FireboltSpell extends AbstractSpell {
    private static final float BASE_DAMAGE = 4.0F;
    private static final float DAMAGE_PER_LEVEL = 2.0F;
    /** 발사 정확도. 0 이면 정확히 조준한 방향으로 나간다. */
    private static final float INACCURACY = 0.0F;
    /** 시전자 눈에서 얼마나 앞에 생길지 (블록). 너무 가까우면 화면을 가린다. */
    private static final double SPAWN_FORWARD = 1.8;
    /** 벽에 걸렸을 때 벽면에서 살짝 떼는 거리. 블록 안에 박혀서 스폰되는 걸 막는다. */
    private static final double WALL_MARGIN = 0.2;

    public FireboltSpell() {
        super(SchoolType.FIRE, CastType.INSTANT, 20, 5, 40, 0, 5);
    }

    @Override
    public List<Component> getUniqueInfo(int spellLevel) {
        return List.of(
                Component.translatable("tooltip.arcana.damage", SpellTooltip.format(getDamage(spellLevel))),
                Component.translatable("tooltip.arcana.burn",
                        SpellTooltip.format(FireballProjectile.BURN_TICKS / 20.0))
        );
    }

    public float getDamage(int spellLevel) {
        return BASE_DAMAGE + DAMAGE_PER_LEVEL * (spellLevel - 1);
    }

    @Override
    protected void onCast(Level level, Player player, int spellLevel) {
        FireballProjectile fireball = new FireballProjectile(level, player, getDamage(spellLevel));

        // ThrowableProjectile 의 생성자는 시전자 눈높이에 그대로 놓아서 화면을 가린다.
        // moveTo 는 이전 위치(xo/yo/zo)까지 같이 맞춰줘서 클라이언트가 원래 자리에서
        // 끌려오는 것처럼 보이지 않는다.
        Vec3 spawnPos = findSpawnPos(level, player);
        fireball.moveTo(spawnPos.x, spawnPos.y, spawnPos.z);

        fireball.shootFromRotation(player, player.getXRot(), player.getYRot(),
                0.0F, FireballProjectile.SPEED, INACCURACY);
        level.addFreshEntity(fireball);

        level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * 시선 방향으로 SPAWN_FORWARD 만큼 앞. 단 그 사이에 벽이 있으면 벽 앞까지만 밀어낸다.
     * 이걸 안 하면 벽에 바짝 붙어서 쏠 때 화염구가 벽 너머에 생겨 관통해버린다.
     */
    private Vec3 findSpawnPos(Level level, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 target = eye.add(look.scale(SPAWN_FORWARD));

        BlockHitResult hit = level.clip(new ClipContext(
                eye, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        if (hit.getType() == HitResult.Type.MISS) {
            return target;
        }
        return hit.getLocation().subtract(look.scale(WALL_MARGIN));
    }
}
