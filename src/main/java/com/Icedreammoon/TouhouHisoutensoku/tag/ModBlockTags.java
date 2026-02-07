package com.Icedreammoon.TouhouHisoutensoku.tag;

import com.Icedreammoon.TouhouHisoutensoku.TouhouHisoutensoku;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModBlockTags {

    public static TagKey<Block> ORE = create("ore");

    private static TagKey<Block> create(String pName) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(TouhouHisoutensoku.MOD_ID, pName));
    }
    private static TagKey<Block> createForgeTag(String pName) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("forge", pName));
    }

}
