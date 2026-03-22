package cn.popcraft.mobassault.listener;

import cn.popcraft.mobassault.MobAssault;
import cn.popcraft.mobassault.mob.CustomMob;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class MobListener implements Listener {

    private final MobAssault plugin;

    public MobListener(MobAssault plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // 检查是否是自定义怪物
        for (CustomMob mob : plugin.getMobManager().getAllMobs()) {
            if (mob.getEntity().equals(entity)) {
                // 移除怪物
                plugin.getMobManager().removeMob(mob.getId());
                break;
            }
        }
    }
}
