package com.Icedreammoon.TouhouHisoutensoku.entity.Marisa;

import com.Icedreammoon.TouhouHisoutensoku.init.ModEntities;
import com.Icedreammoon.TouhouHisoutensoku.utils.TTZDamageTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
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
        this.acceleration = 0.1F;
    }

    public Star(Level world, LivingEntity caster) {
        this(ModEntities.STAR.get(), world);
        this.setOwner(caster);
    }

    public Star(EntityType<? extends Star> caster,double x, double y, double z, Vec3 vec3,Level world) {
        this(caster, world);
        this.moveTo(x, y, z,this.getYRot(), this.getXRot());
        this.reapplyPosition();
        this.assignDirectionalMovement(vec3,this.acceleration);
    }

    public Star(LivingEntity caster,LivingEntity target,Level world,Vec3 vec3, float damage,int duration) {
        this(ModEntities.STAR.get(),);
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

    protected void onHitEntity(EntityHitResult  entityHitResult) {
        super.onHitEntity(target);
        if(!this.level().isClientSide && !(entityHitResult.getEntity() instanceof Marisa)) {
            Entity target = entityHitResult.getEntity();
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


}
