package com.Icedreammoon.TouhouHisoutensoku.client.render;


import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Function;


@OnlyIn(Dist.CLIENT)
public class TTZRenderTypes extends RenderType {
    public TTZRenderTypes(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
    }
    public static RenderType getGlowingEffect(ResourceLocation location) {
        return GLOWING_EFFECT.apply(location);
    }

    private static final Function<ResourceLocation,RenderType> GLOWING_EFFECT = Util.memoize(
            p_286169_ ->{
                CompositeState compositeState = CompositeState.builder()
                        .setTextureState(new TextureStateShard(p_286169_,false,false))
                        .setCullState(NO_CULL)
                        .setShaderState(RENDERTYPE_BEACON_BEAM_SHADER)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(COLOR_WRITE)
                        .setOverlayState(OVERLAY)
                        .createCompositeState(false);
                return RenderType.create(
                    "grow_effect",
                        DefaultVertexFormat.NEW_ENTITY,
                        VertexFormat.Mode.QUADS,
                        1536,
                        true,
                        true,
                        compositeState
                );
            });
}
