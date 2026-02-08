package com.Icedreammoon.TouhouHisoutensoku.client;

import com.Icedreammoon.TouhouHisoutensoku.entity.Marisa.MasterSpark;
import com.Icedreammoon.TouhouHisoutensoku.init.ModEntities;
import com.Icedreammoon.TouhouHisoutensoku.renderer.MasterSparkRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ttz", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModRenderers {
    
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 注册 MasterSpark 渲染器
        event.registerEntityRenderer(ModEntities.MASTER_SPARK.get(), (context) -> new MasterSparkRenderer(context));
    }
}
