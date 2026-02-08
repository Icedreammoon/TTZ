package com.Icedreammoon.TouhouHisoutensoku.renderer;

import com.Icedreammoon.TouhouHisoutensoku.entity.Marisa.MasterSpark;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MasterSparkRenderer extends EntityRenderer<MasterSpark> {
    
    public MasterSparkRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    
    @Override
    public void render(MasterSpark entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // 计算插值后的位置和角度（平滑渲染）
        double x = Mth.lerp(partialTicks, entity.xo, entity.getX());
        double y = Mth.lerp(partialTicks, entity.yo, entity.getY());
        double z = Mth.lerp(partialTicks, entity.zo, entity.getZ());
        
        double collideX = Mth.lerp(partialTicks, entity.prevCollidePosX, entity.collidePosX);
        double collideY = Mth.lerp(partialTicks, entity.prevCollidePosY, entity.collidePosY);
        double collideZ = Mth.lerp(partialTicks, entity.prevCollidePosZ, entity.collidePosZ);
        
        // 计算激光长度和方向
        Vec3 start = new Vec3(x, y, z);
        Vec3 end = new Vec3(collideX, collideY, collideZ);
        
        // 计算透明度（基于渐入渐出效果）
        float alpha = calculateAlpha(entity);
        
        // 绘制激光主体
        drawLaser(poseStack, start, end, alpha);
    }
    
    private void drawLaser(PoseStack poseStack, Vec3 start, Vec3 end, float alpha) {
        // 开始绘制
        poseStack.pushPose();
        
        // 计算激光方向和长度
        Vec3 direction = end.subtract(start).normalize();
        double length = start.distanceTo(end);
        
        // 移动到激光起点
        poseStack.translate(start.x, start.y, start.z);
        
        // 计算旋转角度
        float yaw = (float) Math.atan2(direction.z, direction.x);
        float pitch = (float) Math.asin(direction.y);
        
        // 旋转到激光方向
        poseStack.mulPose(Axis.YP.rotation((float) Math.PI / 2 - yaw));
        poseStack.mulPose(Axis.XP.rotation(-pitch));
        
        // 启用混合模式
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        
        // 创建顶点缓冲区
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        
        // 设置颜色（黄色）
        float r = 1.0f;
        float g = 1.0f;
        float b = 0.0f;
        
        // 激光宽度
        float width = 0.2f;
        
        // 绘制激光四边形
        bufferBuilder.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR);
        
        // 四个顶点
        bufferBuilder.vertex(poseStack.last().pose(), -width, (float) length, 0).color(r, g, b, alpha).endVertex();
        bufferBuilder.vertex(poseStack.last().pose(), width, (float) length, 0).color(r, g, b, alpha).endVertex();
        bufferBuilder.vertex(poseStack.last().pose(), width, 0, 0).color(r, g, b, alpha).endVertex();
        bufferBuilder.vertex(poseStack.last().pose(), -width, 0, 0).color(r, g, b, alpha).endVertex();
        
        tesselator.end();
        
        // 禁用混合模式
        RenderSystem.disableBlend();
        
        poseStack.popPose();
    }
    
    private float calculateAlpha(MasterSpark entity) {
        // 计算渐入渐出效果的透明度
        if (entity.tickCount <= 20) {
            // 渐入阶段（前20帧）
            return (float) entity.tickCount / 20.0f;
        } else if (!entity.on) {
            // 渐出阶段（激光关闭后）
            int fadeOutTime = 20; // 渐出时间（帧）
            int elapsedTime = entity.tickCount - (20 + entity.getDuration());
            return Math.max(0.0f, 1.0f - (float) elapsedTime / fadeOutTime);
        } else {
            // 稳定阶段
            return 1.0f;
        }
    }
    
    @Override
    protected int getBlockLightLevel(MasterSpark entity, BlockPos pos) {
        return 15; // 最大亮度，使激光更明显
    }
    
    @Override
    public ResourceLocation getTextureLocation(MasterSpark entity) {
        return null; // 不需要纹理，使用颜色绘制
    }
}
