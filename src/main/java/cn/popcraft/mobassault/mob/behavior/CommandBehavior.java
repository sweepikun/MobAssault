package cn.popcraft.mobassault.mob.behavior;

import cn.popcraft.mobassault.MobAssault;
import cn.popcraft.mobassault.mob.CustomMob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.List;

public class CommandBehavior implements MobBehavior {

    private final List<String> commands;

    public CommandBehavior(List<String> commands) {
        this.commands = commands;
    }

    @Override
    public void execute(CustomMob mob, LivingEntity target) {
        if (mob.getEntity() == null || mob.getEntity().isDead()) return;
        if (target == null || target.isDead()) return;

        Location mobLoc = mob.getEntity().getLocation();
        Location targetLoc = target.getLocation();

        for (String command : commands) {
            // 替换变量
            String processedCommand = command
                .replace("%mob_id%", String.valueOf(mob.getId()))
                .replace("%mob_type%", mob.getConfig().getEntityType())
                .replace("%mob_name%", mob.getConfig().getName())
                .replace("%target%", target.getName())
                .replace("%x%", String.valueOf(mobLoc.getX()))
                .replace("%y%", String.valueOf(mobLoc.getY()))
                .replace("%z%", String.valueOf(mobLoc.getZ()))
                .replace("%target_x%", String.valueOf(targetLoc.getX()))
                .replace("%target_y%", String.valueOf(targetLoc.getY()))
                .replace("%target_z%", String.valueOf(targetLoc.getZ()));

            // 执行命令
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
        }
    }
}
