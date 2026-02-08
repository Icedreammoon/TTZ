package com.Icedreammoon.TouhouHisoutensoku.init;

import com.Icedreammoon.TouhouHisoutensoku.TouhouHisoutensoku;
import com.Icedreammoon.TouhouHisoutensoku.entity.Marisa.MasterSpark;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TouhouHisoutensoku.MOD_ID);

    public static final RegistryObject<EntityType<MasterSpark>> MASTER_SPARK = ENTITIES.register("master_spark",
            () -> EntityType.Builder.<MasterSpark>of(MasterSpark::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(64)
                    .fireImmune()
                    .updateInterval(1)
                    .build("master_spark"));
}
