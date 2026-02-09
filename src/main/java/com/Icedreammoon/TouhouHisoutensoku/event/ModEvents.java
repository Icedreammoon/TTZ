package com.Icedreammoon.TouhouHisoutensoku.event;

import com.Icedreammoon.TouhouHisoutensoku.init.ModEntities;
import com.Icedreammoon.TouhouHisoutensoku.entity.Marisa.MasterSpark_Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ttz", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    @SubscribeEvent
    public static void onRightClickAir(PlayerInteractEvent.RightClickEmpty event) {
        handleRightClick(event.getEntity(), event.getHand());
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        handleRightClick(event.getEntity(), event.getHand());
    }

    private static void handleRightClick(Player player, InteractionHand hand) {
        // 只处理主手 + 空手
        if (hand == InteractionHand.MAIN_HAND && player.getMainHandItem().isEmpty()) {
            spawnMasterSpark(player);
        }
    }
    private static void spawnMasterSpark(Player player) {
        if (!player.level().isClientSide) {
            // 计算激光的初始位置（玩家眼睛高度）
            double x = player.getX();
            double y = player.getY() + player.getEyeHeight() - 0.1;
            double z = player.getZ();

            // 计算激光的方向（添加90度偏移以匹配游戏坐标系）
            float yaw = (float) ((player.yHeadRot + 90.0d) * Math.PI / 180.0d);
            float pitch = (float) (-player.getXRot() * Math.PI / 180.0d);

            // 创建激光实体
            MasterSpark_Entity masterSpark = new MasterSpark_Entity(
                    ModEntities.MASTER_SPARK.get(),
                    player.level(),
                    player,
                    x, y, z,
                    yaw,
                    pitch,
                    60, // 持续时间（ticks）
                    5.0f, // 基础伤害
                    5.0f // 生命值百分比伤害
            );

            // 发射激光
            player.level().addFreshEntity(masterSpark);
        }
    }

}
