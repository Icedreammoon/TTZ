package com.Icedreammoon.TouhouHisoutensoku.init;

import com.Icedreammoon.TouhouHisoutensoku.TouhouHisoutensoku;
import com.Icedreammoon.TouhouHisoutensoku.entity.Marisa.MasterSpark_Entity;
import com.Icedreammoon.TouhouHisoutensoku.entity.Marisa.Star;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TouhouHisoutensoku.MOD_ID);

    public static final RegistryObject<EntityType<MasterSpark_Entity>> MASTER_SPARK = ENTITIES.register("master_spark",
            () -> EntityType.Builder.<MasterSpark_Entity>of(MasterSpark_Entity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(64)
                    .fireImmune()
                    .updateInterval(1)
                    .build("master_spark"));
    public static final RegistryObject<EntityType<Star>> STAR = ENTITIES.register("star",
            () -> EntityType.Builder.<Star>of(Star::new,MobCategory.MISC)
                    .sized(0.3F,0.3F)
                    .clientTrackingRange(64)
                    .fireImmune()
                    .updateInterval(1)
                    .build("star"));

}
