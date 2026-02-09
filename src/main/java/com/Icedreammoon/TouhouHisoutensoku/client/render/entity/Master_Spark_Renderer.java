package com.Icedreammoon.TouhouHisoutensoku.client.render.entity;

import com.Icedreammoon.TouhouHisoutensoku.TouhouHisoutensoku;
import com.Icedreammoon.TouhouHisoutensoku.client.render.TTZRenderTypes;
import com.Icedreammoon.TouhouHisoutensoku.entity.Marisa.MasterSpark_Entity;
import com.Icedreammoon.TouhouHisoutensoku.utils.TTZMathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class Master_Spark_Renderer extends EntityRenderer<MasterSpark_Entity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(TouhouHisoutensoku.MOD_ID, "textures/entity/marisa/master_spark.png");
    private static final float TEXTURE_WIDTH = 256;
    private static final float TEXTURE_HEIGHT = 32;
    private static final float START_RADIUS = 0.75f; // 激光起点的半径（游戏内单位，1单位=1方块）
    private static final float BEAM_RADIUS = 1.2F; // 激光束主体的半径
    private boolean clearerView = false;// 是否是第一人称视角（如果是玩家发射，第一人称不渲染起点，避免遮挡）

    public Master_Spark_Renderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(MasterSpark_Entity entity){
        return TEXTURE;
    }

    @Override
    public void render(MasterSpark_Entity masterspark, float entityYaw, float delta, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn){
        clearerView = masterspark.caster instanceof Player
                && Minecraft.getInstance().player == masterspark.caster
                && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON;

        double collidePosX = masterspark.prevCollidePosX + (masterspark.collidePosX - masterspark.prevCollidePosX) * delta;
        double collidePosY = masterspark.prevCollidePosY + (masterspark.collidePosY - masterspark.prevCollidePosY) * delta;
        double collidePosZ = masterspark.prevCollidePosZ + (masterspark.collidePosZ - masterspark.prevCollidePosZ) * delta;
        
        double posX = masterspark.xo + (masterspark.getX() - masterspark.xo) * delta;
        double posY = masterspark.yo + (masterspark.getY() - masterspark.yo) * delta;
        double posZ = masterspark.zo + (masterspark.getZ() - masterspark.zo) * delta;
        
        float yaw = masterspark.prevYaw + (masterspark.renderYaw - masterspark.prevYaw) * delta;
        float pitch = masterspark.prevPitch + (masterspark.renderPitch - masterspark.prevPitch) * delta;

        float length = (float) Math.sqrt(
                Math.pow(collidePosX - posX, 2) +
                        Math.pow(collidePosY - posY, 2) +
                        Math.pow(collidePosZ - posZ, 2)
        );


        int frame = Mth.floor((masterspark.appear.getTimer() - 1 + delta) * 2);
        if (frame < 0) {
            frame = 6;
        }
        VertexConsumer consumer = bufferIn.getBuffer(TTZRenderTypes.getGlowingEffect(getTextureLocation(masterspark)));
        renderBeam(length,180f/(float)Math.PI*yaw, 180f/(float)Math.PI*pitch,frame,matrixStackIn, consumer, packedLightIn);

        matrixStackIn.pushPose();
        matrixStackIn.translate(collidePosX - posX, collidePosY - posY, collidePosZ - posZ);
        render_end(frame,masterspark.blockSide,matrixStackIn,consumer, packedLightIn);
        matrixStackIn.popPose();
    }

    private void renderBeam(float length, float yaw, float pitch, int frame, PoseStack matrixStackIn, VertexConsumer builder, int packedLightIn) {
        matrixStackIn.pushPose();
        matrixStackIn.mulPose(TTZMathUtil.quatFromRotationXYZ(90,0,0,true));
        matrixStackIn.mulPose(TTZMathUtil.quatFromRotationXYZ(0,0,yaw-90,true));
        matrixStackIn.mulPose(TTZMathUtil.quatFromRotationXYZ(0-pitch,0,0,true));

        matrixStackIn.pushPose();
        if (!clearerView) {
            matrixStackIn.mulPose((new Quaternionf()).rotationY((Minecraft.getInstance().gameRenderer.getMainCamera().getXRot() + 90)));
        }
        drawBeam(length, frame, matrixStackIn, builder, packedLightIn);
        matrixStackIn.popPose();

        if (!clearerView) {
            matrixStackIn.pushPose();
            matrixStackIn.mulPose((new Quaternionf()).rotationY((-Minecraft.getInstance().gameRenderer.getMainCamera().getXRot() - 90) * ((float) Math.PI / 180F)));
            drawBeam(length, frame, matrixStackIn, builder, packedLightIn);
            matrixStackIn.popPose();
        }
        matrixStackIn.popPose();
    }

    private void drawBeam(float length,int frame, PoseStack matrixStackIn, VertexConsumer builder, int packedLightIn) {
        float minU =0;
        float minV = 16/TEXTURE_HEIGHT + 1/TEXTURE_HEIGHT*frame;
        float maxU = minU+20/TEXTURE_WIDTH;
        float maxV = minV+1/TEXTURE_HEIGHT;

        PoseStack.Pose matrixstack$entry = matrixStackIn.last();
        Matrix4f matrix4f = matrixstack$entry.pose();
        Matrix3f matrix3f = matrixstack$entry.normal();

        float offset = clearerView ? -1:0;

        drawVertex(matrix4f, matrix3f, builder, -BEAM_RADIUS, offset, 0, minU, minV, 1, packedLightIn);
        drawVertex(matrix4f, matrix3f, builder, -BEAM_RADIUS, length, 0, minU, maxV, 1, packedLightIn);
        drawVertex(matrix4f, matrix3f, builder, BEAM_RADIUS, length, 0, maxU, maxV, 1, packedLightIn);
        drawVertex(matrix4f, matrix3f, builder, BEAM_RADIUS, offset, 0, maxU, minV, 1, packedLightIn);
    }

    private void renderQuad(int frame,PoseStack matrixStackIn, VertexConsumer builder, int packedLightIn) {
        float minU =0+16F/TEXTURE_WIDTH*frame;
        float minV = 0;
        float maxU = minU+16F/TEXTURE_WIDTH;
        float maxV = minV+16F/TEXTURE_HEIGHT;

        PoseStack.Pose matrixstack$entry = matrixStackIn.last();
        Matrix4f matrix4f = matrixstack$entry.pose();
        Matrix3f matrix3f = matrixstack$entry.normal();

        drawVertex(matrix4f, matrix3f, builder, -START_RADIUS, -START_RADIUS, 0, minU, minV, 1, packedLightIn);
        drawVertex(matrix4f, matrix3f, builder, -START_RADIUS, START_RADIUS, 0, minU, maxV, 1, packedLightIn);
        drawVertex(matrix4f, matrix3f, builder, START_RADIUS, START_RADIUS, 0, maxU, maxV, 1, packedLightIn);
        drawVertex(matrix4f, matrix3f, builder, START_RADIUS, -START_RADIUS, 0, maxU, minV, 1, packedLightIn);
    }

    private void render_end(int frame, Direction side, PoseStack matrixStackIn, VertexConsumer builder, int packedLightIn) {
        matrixStackIn.pushPose();

        Quaternionf camera = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        matrixStackIn.mulPose(camera);
        renderQuad(frame,matrixStackIn,builder,packedLightIn);
        matrixStackIn.popPose();

        if(side==null){
            return;
        }
        matrixStackIn.pushPose();
        Quaternionf sidequat = side.getRotation();
        sidequat.mul(TTZMathUtil.quatFromRotationXYZ(90,0,0,true));
        matrixStackIn.mulPose(sidequat);
        matrixStackIn.translate(0,0,-0.01f);
        renderQuad(frame,matrixStackIn,builder,packedLightIn);
        matrixStackIn.popPose();

    }

    private void drawVertex(
            Matrix4f matrix,
            Matrix3f normal,
            VertexConsumer consumer,
            float offsetX,
            float offsetY,
            float offsetZ,
            float textureU,
            float textureV,
            float alpha,
            int packedLight
    ){
        consumer.vertex(matrix,offsetX,offsetY,offsetZ)
                .color(1,1,1,1*alpha)
                .uv(textureU,textureV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal,0.0F,1.0F,0.0F)
                .endVertex();

    }


}
