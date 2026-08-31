package com.first.arcana.entity.custom;

import com.first.arcana.entity.ModEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 파이어볼 주문이 발사하는 화염구.
 * 중력을 받지 않고 직진하다가 처음 맞은 것에서 터진다.
 *
 * 피해량은 주문 레벨마다 달라지므로 스폰할 때 넣어준다.
 * 서버에서만 쓰는 값이라 동기화하지 않는다.
 */
public class FireballProjectile extends ThrowableProjectile {
    /** 발사 속도 (블록/틱) */
    public static final float SPEED = 1.5F;
    /** 최대 생존 시간 (틱). 20틱 = 1초 */
    private static final int MAX_LIFE = 20;
    /** 명중 시 점화 시간 (틱) */
    public static final int BURN_TICKS = 80;

    private static final float DEFAULT_DAMAGE = 4.0F;

    /** 한 틱에 잔상을 몇 지점에 뿌릴지. 늘리면 꼬리가 촘촘해지지만 그만큼 오래 남는다. */
    private static final int TRAIL_SAMPLES = 2;
    /** 연기를 몇 틱마다 한 번 낼지 */
    private static final int SMOKE_INTERVAL = 3;

    private float damage = DEFAULT_DAMAGE;
    private int life;

    /** 레지스트리/클라이언트 스폰용 생성자 */
    public FireballProjectile(EntityType<? extends FireballProjectile> type, Level level) {
        super(type, level);
    }

    /** 주문이 발사할 때 쓰는 생성자 */
    public FireballProjectile(Level level, LivingEntity owner, float damage) {
        super(ModEntityTypes.FIREBALL.get(), owner, level);
        this.damage = damage;
    }

    /** 화염구는 포물선을 그리지 않는다. */
    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    /** 따로 동기화할 데이터가 없다. */
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();

        // super.tick() 안의 명중 처리로 이미 사라졌을 수 있다.
        if (this.isRemoved()) {
            return;
        }

        if (this.level().isClientSide()) {
            this.spawnTrail();
            return;
        }

        if (++this.life >= MAX_LIFE) {
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && target != this.getOwner();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide()) {
            return;
        }

        Entity target = result.getEntity();
        LivingEntity owner = (this.getOwner() instanceof LivingEntity living) ? living : null;
        DamageSource source = this.damageSources().indirectMagic(this, owner);

        target.hurt(source, this.damage);
        target.setRemainingFireTicks(BURN_TICKS);

        this.burst();
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level().isClientSide()) {
            return;
        }
        this.burst();
        this.discard();
    }

    // ---------------- 연출 ----------------

    /** 클라이언트에서 날아온 자리에 불꽃 잔상을 남긴다. */
    private void spawnTrail() {
        Vec3 motion = this.getDeltaMovement();
        for (int i = 0; i < TRAIL_SAMPLES; i++) {
            double back = i / (double) TRAIL_SAMPLES;
            double x = this.getX() - motion.x * back;
            double y = this.getY() - motion.y * back;
            double z = this.getZ() - motion.z * back;

            // SMALL_FLAME 은 FLAME 보다 작고 수명도 짧아서 꼬리가 금방 사라진다.
            this.level().addParticle(ParticleTypes.SMALL_FLAME, x, y, z, 0.0, 0.0, 0.0);
        }

        // 연기는 오래 남으니 매 틱 내지 않는다.
        if (this.tickCount % SMOKE_INTERVAL == 0) {
            this.level().addParticle(ParticleTypes.SMOKE,
                    this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        }
    }

    /** 터질 때의 연출. 서버에서만 호출한다. */
    private void burst() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                    this.getX(), this.getY(), this.getZ(), 8, 0.2, 0.2, 0.2, 0.02);
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    this.getX(), this.getY(), this.getZ(), 20, 0.2, 0.2, 0.2, 0.15);
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.7F, 1.6F);
    }
}
