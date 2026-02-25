package com.Icedreammoon.TouhouHisoutensoku.entity.Marisa;

import com.Icedreammoon.TouhouHisoutensoku.init.ModEntities;
import com.Icedreammoon.TouhouHisoutensoku.utils.TTZDamageTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.UUID;

public class Star extends Projectile {
    private boolean stopHoming = false;
    private static double Max_Speed = 2.0D;
    public double acceleration = 0.5F;
    @Nullable
    private Entity target;
    @Nullable
    private UUID targetID;

    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(Star.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(Star.class, EntityDataSerializers.FLOAT);


    @OnlyIn(Dist.CLIENT)
    private Vec3[] attractorPos;

    public Star(EntityType<? extends Star> type, Level world) {
        super(type, world);
        this.setDeltaMovement(new Vec3(0, 0, 0.5));
        this.hasImpulse = true;
        this.acceleration = 0.1F;
    }

    public Star(Level world, LivingEntity caster) {
        this(ModEntities.STAR.get(), world);
        this.setOwner(caster);
        Vec3 vec3 = caster.getForward().normalize().scale(0.5);
        this.setDeltaMovement(vec3);
    }

    public Star(EntityType<? extends Star> caster,double x, double y, double z, Vec3 vec3,Level world) {
        this(caster, world);
        this.moveTo(x, y, z,this.getYRot(), this.getXRot());
        this.reapplyPosition();
        this.assignDirectionalMovement(vec3,this.acceleration);
    }

    public Star(LivingEntity caster,LivingEntity target,Level world,Vec3 vec3, float damage,int duration) {
        this(ModEntities.STAR.get(),world);
        this.setOwner(caster);
        this.target = target;
        this.setDamage(damage);
        this.setDuration(duration);
        this.setRot(caster.getYRot(),caster.getXRot());
    }

    private void assignDirectionalMovement(Vec3 movement,double acceleration){
        this.setDeltaMovement(movement.normalize().scale(acceleration));
        this.hasImpulse = true;
    }
    protected void defineSynchedData() {
        this.entityData.define(DURATION, 200);
        this.entityData.define(DAMAGE, 0F);
    }

    public void setDamage(float damage) {
        entityData.set(DAMAGE, damage);
    }

    public float getDamage() {
        return entityData.get(DAMAGE);
    }

    public void setDuration(int duration) {
        entityData.set(DURATION, duration);
    }

    public int getDuration() {
        return entityData.get(DURATION);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if(this.target != null) {
            tag.putUUID("Target", this.target.getUUID());
        }
        tag.putInt("duration", this.getDuration());
        tag.putDouble("acceleration", this.acceleration);
    }
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if(tag.hasUUID("Target")) {
            this.targetID = tag.getUUID("Target");
        }
        if(tag.contains("duration",3)) {
            this.setDuration(tag.getInt("duration"));
        }
        if(tag.contains("acceleration",6)) {
            this.acceleration = tag.getDouble("acceleration");
        }
    }

    protected void onHitEntity(EntityHitResult  ptarget) {
        super.onHitEntity(ptarget);
        if(!this.level().isClientSide && !(ptarget.getEntity() instanceof Marisa)) {
            Entity target = ptarget.getEntity();
            Entity caster = this.getOwner();
            boolean flag ;
            if(caster instanceof LivingEntity) {
                LivingEntity livingcaster = (LivingEntity) caster;
                flag = target.hurt(TTZDamageTypes.causeMagicDamage(this,livingcaster),this.getDamage());
            }
            else {
                flag = target.hurt(TTZDamageTypes.causeMagicDamage(this,null),this.getDamage());
            }
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if(!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        HitResult.Type type = hitResult.getType();
        if(type == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult)hitResult);
        }
        else if(type == HitResult.Type.BLOCK) {
            this.onHitBlock((BlockHitResult)hitResult);
        }
    }

    @Override
    protected boolean canHitEntity(Entity pTarget) {
        return super.canHitEntity(pTarget) && !pTarget.noPhysics;
    }

    public boolean isPickable() {
        return false;
    }

    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        Vec3 vec3 = new Vec3(packet.getXa(), packet.getYa(), packet.getZa());
        this.setDeltaMovement(vec3);
        this.xRotO = packet.getXRot();
        this.yRotO = packet.getYRot();
    }

    protected float getInertia() {
        return 1.0F;
    }

    public void tick(){
        Entity caster = this.getOwner();
        if(this.level().isClientSide || this.level().hasChunkAt(this.blockPosition())) {
            super.tick();
            HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this,this::canHitEntity);
            if(hitResult.getType() != HitResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this,hitResult)) {
                this.onHit(hitResult);
            }
            this.checkInsideBlocks();
            Vec3 vec3 = this.getDeltaMovement();
            double newx =this.getX() + vec3.x;
            double newy = this.getY() + vec3.y;
            double newz = this.getZ() + vec3.z;
            ProjectileUtil.rotateTowardsMovement(this,0.8F);
            float f = this.getInertia();
            double currentSpeed = vec3.length();

            if(this.isInWater()){
                for(int i = 0;i<4; i++){
                    float d = 0.2F;
                    this.level().addParticle(ParticleTypes.BUBBLE,
                            newx - vec3.x * d,
                            newy - vec3.y * d,
                            newz - vec3.z * d,
                            vec3.x, vec3.y, vec3.z
                            );
                }
                f = 0.9F;
            }

            if(currentSpeed < Max_Speed * f) {
                Vec3 newvec = vec3.add(vec3.normalize().scale(this.acceleration));
                double newspeed = newvec.length();
                if (newspeed > Max_Speed * f) {
                    newvec = newvec.normalize().scale(Max_Speed).scale((double) f);
                }
                this.setDeltaMovement(newvec);
            }
            else {
                this.setDeltaMovement(vec3.normalize().scale(Max_Speed).scale((double) f));
                this.stopHoming = true;
            }
            this.setPos(newx,newy,newz);
        }
        if(!this.stopHoming && !this.level().isClientSide) {
            if(this.target == null && this.targetID != null) {
                this.target = ((ServerLevel)this.level()).getEntity(this.targetID);
                if(this.target == null) {
                    this.targetID = null;
                }
            }
            if(this.target != null && !this.target.isAlive() && !(this.target instanceof Marisa)) {
                Vec3 targetPos = new Vec3(
                        this.target.getX(),
                        this.target.getY(0.5),
                        this.target.getZ()
                );


            }
        }

        if(tickCount > getDuration()){
            this.discard();
        }

    }

}
