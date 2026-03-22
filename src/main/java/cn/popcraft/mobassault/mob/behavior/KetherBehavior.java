package cn.popcraft.mobassault.mob.behavior;

import cn.popcraft.mobassault.mob.CustomMob;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class KetherBehavior implements MobBehavior {

    private final String script;

    public KetherBehavior(String script) {
        this.script = script;
    }

    @Override
    public void execute(CustomMob mob, LivingEntity target) {
        if (mob.getEntity() == null || mob.getEntity().isDead()) return;
        if (target == null || target.isDead()) return;
        if (script == null || script.isEmpty()) return;

        try {
            // 处理脚本中的变量替换
            Map<String, String> variables = new HashMap<>();
            variables.put("%mob_id%", String.valueOf(mob.getId()));
            variables.put("%mob_type%", mob.getConfig().getEntityType());
            variables.put("%mob_name%", mob.getConfig().getName());
            variables.put("%target%", target.getName());
            variables.put("%x%", String.valueOf(mob.getEntity().getLocation().getX()));
            variables.put("%y%", String.valueOf(mob.getEntity().getLocation().getY()));
            variables.put("%z%", String.valueOf(mob.getEntity().getLocation().getZ()));
            variables.put("%target_x%", String.valueOf(target.getLocation().getX()));
            variables.put("%target_y%", String.valueOf(target.getLocation().getY()));
            variables.put("%target_z%", String.valueOf(target.getLocation().getZ()));

            // 如果目标是玩家，添加玩家相关变量
            if (target instanceof Player) {
                Player player = (Player) target;
                variables.put("%player%", player.getName());
                variables.put("%player_name%", player.getName());
                variables.put("%health%", String.valueOf(player.getHealth()));
                variables.put("%max_health%", String.valueOf(player.getMaxHealth()));
            }

            // 解析脚本命令
            String[] lines = script.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                // 替换变量
                for (Map.Entry<String, String> entry : variables.entrySet()) {
                    line = line.replace(entry.getKey(), entry.getValue());
                }

                // 执行命令
                if (line.startsWith("tell ")) {
                    String message = line.substring(5);
                    if (target instanceof Player) {
                        ((Player) target).sendMessage(message.replace("&", "\u00a7"));
                    }
                } else if (line.startsWith("damage ")) {
                    try {
                        double damage = Double.parseDouble(line.substring(7));
                        target.damage(damage, mob.getEntity());
                    } catch (NumberFormatException e) {
                        Bukkit.getLogger().warning("[MobAssault] 无效的伤害值: " + line);
                    }
                } else if (line.startsWith("potion-effect ")) {
                    String[] parts = line.substring(14).split(" ");
                    if (parts.length >= 2) {
                        try {
                            org.bukkit.potion.PotionEffectType type = org.bukkit.potion.PotionEffectType.getByName(parts[0].toUpperCase());
                            int duration = Integer.parseInt(parts[1]) * 20;
                            int amplifier = parts.length >= 3 ? Integer.parseInt(parts[2]) : 0;
                            if (type != null) {
                                target.addPotionEffect(new org.bukkit.potion.PotionEffect(type, duration, amplifier));
                            }
                        } catch (Exception e) {
                            Bukkit.getLogger().warning("[MobAssault] 无效的药水效果: " + line);
                        }
                    }
                } else if (line.startsWith("particle ")) {
                    // 简单的粒子效果处理
                    String[] parts = line.substring(9).split(" ");
                    if (parts.length >= 1) {
                        // 这里可以扩展粒子效果
                        Bukkit.getLogger().info("[MobAssault] 粒子效果: " + parts[0]);
                    }
                } else if (line.startsWith("execute ")) {
                    String command = line.substring(8);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            }

        } catch (Exception e) {
            Bukkit.getLogger().warning("[MobAssault] 脚本执行错误: " + e.getMessage());
        }
    }
}
