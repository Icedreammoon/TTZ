package com.Icedreammoon.TouhouHisoutensoku.entity.Marisa;

import com.Icedreammoon.TouhouHisoutensoku.init.ModEntities;
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

    public Star(Level world, LivingEntity entity) {
        this(ModEntities.STAR.get(), world);
        this.setOwner(entity);
    }

    public Star(EntityType<? extends Star> type,double x, double y, double z, Vec3 vec3,Level world) {
        this(type, world);
        this.moveTo(x, y, z,this.getYRot(), this.getXRot());
        this.reapplyPosition();
        this.
    }

    public Star(EntityType<? extends Star> type,LivingEntity caster double x, double y, double z, Level world){
        this(type, world);


    }

    protected void defineSynchedData() {
        this.entityData.define(DURATION, 80);
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

}
