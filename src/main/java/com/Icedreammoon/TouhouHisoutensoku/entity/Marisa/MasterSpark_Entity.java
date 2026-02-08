package com.Icedreammoon.TouhouHisoutensoku.entity.Marisa;

import com.Icedreammoon.TouhouHisoutensoku.client.tool.ControlledAnimation;
import com.Icedreammoon.TouhouHisoutensoku.utils.TTZDamageTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MasterSpark_Entity extends Entity {
    public static final double MAX_DISTANCE = 64;
    public LivingEntity caster; // 施法者（生物实体，BOSS/怪物）
    public double endPosX, endPosY, endPosZ; // 激光「理论终点」（最大射程处，无遮挡时的位置）
    public double collidePosX, collidePosY, collidePosZ; // 激光「实际碰撞点」（被方块/实体遮挡的位置）
    public double prevCollidePosX, prevCollidePosY, prevCollidePosZ; // 上一帧碰撞点（平滑渲染插值用）
    public float renderYaw, renderPitch; // 渲染用旋转角度（弧度制，适配客户端绘制）
    public float prevYaw, prevPitch; // 上一帧渲染角度（平滑渲染）
    public boolean on = true; // 激光开启状态（核心开关，控制逻辑执行）
    public Direction blockSide = null; // 碰撞到的方块朝向（用于粒子生成/方块交互的精准定位）
    public ControlledAnimation appear = new ControlledAnimation(20);

    // 旋转角度（服务端计算，同步给客户端用于渲染/终点计算）
    private static final EntityDataAccessor<Float> YAW = SynchedEntityData.defineId(MasterSpark_Entity.class, net.minecraft.network.syncher.EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PITCH = SynchedEntityData.defineId(MasterSpark_Entity.class, net.minecraft.network.syncher.EntityDataSerializers.FLOAT);
    // 激光持续时间（帧数，控制激光存活时长）
    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(MasterSpark_Entity.class, net.minecraft.network.syncher.EntityDataSerializers.INT);
    // 施法者ID（客户端无法直接持有实体引用，通过ID获取施法者）
    private static final EntityDataAccessor<Integer> CASTER = SynchedEntityData.defineId(MasterSpark_Entity.class, net.minecraft.network.syncher.EntityDataSerializers.INT);
    // 基础伤害（固定值）
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(MasterSpark_Entity.class, net.minecraft.network.syncher.EntityDataSerializers.FLOAT);
    // 百分比伤害（基于目标最大生命值，%值）
    private static final EntityDataAccessor<Float> HPDAMAGE = SynchedEntityData.defineId(MasterSpark_Entity.class, net.minecraft.network.syncher.EntityDataSerializers.FLOAT);

    @OnlyIn(Dist.CLIENT)
    private Vec3[] attractorPos; // 粒子吸引点位置（用于激光粒子的聚合效果，客户端视觉专属）

    public MasterSpark_Entity(EntityType<? extends MasterSpark_Entity> type, Level world) {
        super(type,world);
        noCulling = true;
        // 初始化碰撞位置变量
        collidePosX = 0;
        collidePosY = 0;
        collidePosZ = 0;
        prevCollidePosX = 0;
        prevCollidePosY = 0;
        prevCollidePosZ = 0;
        renderYaw = 0;
        renderPitch = 0;
        prevYaw = 0;
        prevPitch = 0;
        if (world.isClientSide) {
            attractorPos = new Vec3[] {new Vec3(0, 0, 0)}; // 客户端仅初始化粒子属性
        }
    }
    public MasterSpark_Entity(EntityType<? extends MasterSpark_Entity> type, Level world, LivingEntity caster,
                              double x, double y, double z, float yaw, float pitch,
                              int duration, float damage, float Hpdamage) {
        this(type, world); // 调用无参构造，初始化基础属性
        this.caster = caster; // 赋值施法者（服务端唯一持有，客户端后续通过ID获取）
        this.setYaw(yaw); // 设置同步属性：偏航角
        this.setPitch(pitch); // 设置同步属性：俯仰角
        this.setDuration(duration); // 设置同步属性：持续时间
        this.setPos(x, y, z); // 设置激光起点（施法者头部/胸口位置）
        this.setDamage(damage); // 设置同步属性：基础伤害
        this.setHpDamage(Hpdamage); // 设置同步属性：百分比伤害
        this.calculateEndPos(); // 计算激光初始理论终点
        // 初始化碰撞位置为理论终点
        this.collidePosX = this.endPosX;
        this.collidePosY = this.endPosY;
        this.collidePosZ = this.endPosZ;
        this.prevCollidePosX = this.collidePosX;
        this.prevCollidePosY = this.collidePosY;
        this.prevCollidePosZ = this.collidePosZ;
        if (!world.isClientSide) {
            this.setCasterID(caster.getId()); // 服务端仅同步施法者ID，客户端不执行
        }
    }

    @Override
    public void tick() {
        super.tick();
        prevCollidePosX = collidePosX;
        prevCollidePosY = collidePosY;
        prevCollidePosZ = collidePosZ;
        prevYaw = renderYaw;
        prevPitch = renderPitch;
        xo = getX(); yo = getY(); zo = getZ();

        //客户端初始化施法者
        if (tickCount == 1 && level().isClientSide) {
            caster = (LivingEntity) level().getEntity(getCasterID());
        }

        // 步骤3：服务端更新激光位置/角度
        if (!level().isClientSide) {
            if (caster != null) {
                this.updateWithMarisa();
            }
        }

        // 步骤4：更新渲染角度（服务端/客户端均执行，跟随施法者头部旋转）
        if (caster != null) {
            // 施法者头部偏航角→激光偏航角（弧度制，+90°是模型朝向适配）
            renderYaw = (float) ((caster.yHeadRot + 90.0d) * Math.PI / 180.0d);
            // 施法者头部俯仰角→激光俯仰角（弧度制，取负是坐标系适配）
            renderPitch = (float) (-caster.getXRot() * Math.PI / 180.0d);
        }

//        // 步骤5：激光关闭且动画结束 → 销毁实体（生命周期管理）
        if (!on && tickCount > (20 + getDuration() + 20)) {
            this.discard();
        }

        if (on && tickCount > 20) {
            appear.increaseTimer(); // 渐显
        } else {
            appear.decreaseTimer(); // 渐隐
        }

        // 步骤7：施法者死亡 → 立即销毁激光（安全兜底，避免无主激光存在）
        if (caster != null && !caster.isAlive()) discard();

        // 步骤8：激光核心逻辑（20帧预热后执行，避免创建时的误判）
        if (tickCount > 20) {
            this.calculateEndPos(); // 实时计算激光理论终点（跟随施法者角度变化）
            // 射线检测：获取激光路径上的实体/方块碰撞结果

            List<LivingEntity> hit = raytraceEntities(level(), new Vec3(getX(), getY(), getZ()), new Vec3(endPosX, endPosY, endPosZ), false, true, true).entities;

            // 子步骤8.1：碰撞到方块 → 执行方块交互+粒子生成
            if (blockSide != null) {
                if (!this.level().isClientSide) { // 服务端执行方块交互
                    for (BlockPos pos : BlockPos.betweenClosed(Mth.floor(collidePosX - 0.5F), Mth.floor(collidePosY - 0.5F), Mth.floor(collidePosZ - 0.5F), Mth.floor(collidePosX + 0.5F), Mth.floor(collidePosY + 0.5F), Mth.floor(collidePosZ + 0.5F))) {
                        BlockState block = level().getBlockState(pos);
                        if (!block.isAir() && block.is(BlockTags.LOGS) && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level(), this)) {
                            level().destroyBlock(pos, true);
                        }
                    }
                }
            }

            // 子步骤8.2：服务端执行实体伤害计算
            if (!level().isClientSide) {
                for (LivingEntity target : hit) {
                    if (caster != null) {
                        // 过滤：非友方+非施法者自身
                        if (!this.caster.isAlliedTo(target) && target != caster) {
                            // 伤害计算公式：基础伤害 + 最小(基础伤害, 目标最大生命值*百分比伤害*0.01)
                            // 加Math.min是为了限制百分比伤害的上限，避免伤害过高
                            boolean flag = target.hurt(TTZDamageTypes.causeMagicDamage( this, caster),
                                    (float) (this.getDamage() + Math.min(this.getDamage(), target.getMaxHealth() * this.getHpDamage() * 0.01)));
                        }
                    }
                }
            }
        }

        // 步骤9：激光持续时间结束 → 关闭激光（触发渐隐动画）
        if (tickCount - 20 > getDuration()) {
            on = false;
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(YAW, 0F); // 初始化默认值
        this.entityData.define(PITCH, 0F);
        this.entityData.define(DURATION, 0);
        this.entityData.define(CASTER, -1); // -1表示无施法者
        this.entityData.define(DAMAGE, 0F);
        this.entityData.define(HPDAMAGE, 0F);
    }

    private void calculateEndPos() {
        if (level().isClientSide()) {
            endPosX = getX() + MAX_DISTANCE * Math.cos(renderYaw) * Math.cos(renderPitch);
            endPosZ = getZ() + MAX_DISTANCE * Math.sin(renderYaw) * Math.cos(renderPitch);
            endPosY = getY() + MAX_DISTANCE * Math.sin(renderPitch);
        } else {
            endPosX = getX() + MAX_DISTANCE * Math.cos(getYaw()) * Math.cos(getPitch());
            endPosZ = getZ() + MAX_DISTANCE * Math.sin(getYaw()) * Math.cos(getPitch());
            endPosY = getY() + MAX_DISTANCE * Math.sin(getPitch());
        }
    }

    public float getDamage() {
        return entityData.get(DAMAGE);
    }

    public void setDamage(float damage) {
        entityData.set(DAMAGE, damage);
    }

    public float getHpDamage() {
        return entityData.get(HPDAMAGE);
    }

    public void setHpDamage(float damage) {
        entityData.set(HPDAMAGE, damage);
    }


    public float getYaw() {
        return entityData.get(YAW);
    }

    public void setYaw(float yaw) {
        entityData.set(YAW, yaw);
    }

    public float getPitch() {
        return entityData.get(PITCH);
    }

    public void setPitch(float pitch) {
        entityData.set(PITCH, pitch);
    }

    public int getDuration() {
        return entityData.get(DURATION);
    }

    public void setDuration(int duration) {
        entityData.set(DURATION, duration);
    }

    public int getCasterID() {
        return entityData.get(CASTER);
    }

    public void setCasterID(int id) {
        entityData.set(CASTER, id);
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE; // 激光无视活塞推动
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {}
    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {}

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void push(Entity entityIn) {} // 无视其他实体的推动
    @Override
    public boolean canBeCollidedWith() { return false; } // 不可被碰撞
    @Override
    public boolean isPushable() { return false; } // 不可被推动

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096; // 渲染距离平方=4096 → 实际渲染距离=64格
    }

    private void updateWithMarisa() {
        this.setYaw((float) ((caster.yHeadRot + 90) * Math.PI / 180.0d));
        this.setPitch((float) (-caster.getXRot() * Math.PI / 180.0d));
        this.setPos(caster.getX(), caster.getY() + caster.getEyeHeight() - 0.1, caster.getZ());
    }

    public LaserbeamHitResult raytraceEntities(Level world, Vec3 from, Vec3 to,
                                               boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox,
                                               boolean returnLastUncollidableBlock) {
        LaserbeamHitResult result = new LaserbeamHitResult();
        result.setBlockHit(world.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)));
        if (result.blockHit != null) {
            Vec3 hitVec = result.blockHit.getLocation();
            collidePosX = hitVec.x;
            collidePosY = hitVec.y;
            collidePosZ = hitVec.z;
            blockSide = result.blockHit.getDirection();
        } else {
            collidePosX = endPosX;
            collidePosY = endPosY;
            collidePosZ = endPosZ;
            blockSide = null;
        }
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class,
                new AABB(Math.min(from.x, collidePosX),
                        Math.min(from.y, collidePosY),
                        Math.min(from.z, collidePosZ),
                        Math.max(from.x, collidePosX),
                        Math.max(from.y, collidePosY),
                        Math.max(from.z, collidePosZ)).inflate(0.5, 0.5, 0.5));
        for (LivingEntity entity : entities) {
            if (entity == caster) {
                continue;
            }
            float pad = entity.getPickRadius() + 0.25f;
            AABB aabb = entity.getBoundingBox().inflate(pad, pad, pad);
            Optional<Vec3> hit = aabb.clip(from, new Vec3(collidePosX, collidePosY, collidePosZ));
            if (aabb.contains(from)) {
                result.addEntityHit(entity);
            } else if (hit.isPresent()) {
                result.addEntityHit(entity);
            }
        }
        return result;
    }

    public static class LaserbeamHitResult {
        private BlockHitResult blockHit;
        private final List<LivingEntity> entities = new ArrayList<>();

        public BlockHitResult getBlockHit() {
            return blockHit;
        }

        public void setBlockHit(HitResult rayTraceResult) {
            if (rayTraceResult.getType() == HitResult.Type.BLOCK)
                this.blockHit = (BlockHitResult) rayTraceResult;
        }

        public void addEntityHit(LivingEntity entity) {
            entities.add(entity);
        }
    }

}
