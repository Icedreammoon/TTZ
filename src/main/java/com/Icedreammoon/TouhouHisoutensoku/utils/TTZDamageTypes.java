package com.Icedreammoon.TouhouHisoutensoku.utils;

import com.Icedreammoon.TouhouHisoutensoku.TouhouHisoutensoku;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;



public class TTZDamageTypes {
    public static final ResourceKey<DamageType> MAGIC = ResourceKey.create(Registries.DAMAGE_TYPE,new ResourceLocation(TouhouHisoutensoku.MOD_ID,"magic"));

    public static final DamageSource causeMagicDamage(Entity attacker, LivingEntity caster) {
        return new DamageSource(attacker.level().registryAccess().registry(Registries.DAMAGE_TYPE).get().getHolderOrThrow(MAGIC), attacker, caster);
    }
}

