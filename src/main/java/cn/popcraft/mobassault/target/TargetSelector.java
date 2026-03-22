package cn.popcraft.mobassault.target;

import cn.popcraft.mobassault.config.MobConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.List;

public class TargetSelector {

    public static List<LivingEntity> getTargets(MobConfig config, String targetName, Location center) {
        List<LivingEntity> targets = new ArrayList<>();

        String targetType = config.getTargetType();

        if ("PLAYER".equalsIgnoreCase(targetType)) {
            targets = getTargetsByPlayers(config, targetName);
        } else if ("PERMISSION_GROUP".equalsIgnoreCase(targetType)) {
            targets = getTargetsByPermissionGroups(config, center);
        }

        return targets;
    }

    private static List<LivingEntity> getTargetsByPlayers(MobConfig config, String targetName) {
        List<LivingEntity> targets = new ArrayList<>();
        List<String> playerNames = config.getTargetPlayers();

        // 如果提供了目标名称，优先使用
        if (targetName != null && !targetName.isEmpty()) {
            playerNames = new ArrayList<>();
            playerNames.add(targetName);
        }

        for (String playerName : playerNames) {
            // 处理变量
            if (playerName.startsWith("%") && playerName.endsWith("%")) {
                // 跳过变量，这些应该在生成时由命令处理
                continue;
            }

            Player player = Bukkit.getPlayer(playerName);
            if (player != null && player.isOnline()) {
                targets.add(player);
            }
        }

        return targets;
    }

    private static List<LivingEntity> getTargetsByPermissionGroups(MobConfig config, Location center) {
        List<LivingEntity> targets = new ArrayList<>();
        List<String> groups = config.getTargetGroups();

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String group : groups) {
                if (hasPermissionGroup(player, group)) {
                    targets.add(player);
                    break;
                }
            }
        }

        return targets;
    }

    private static boolean hasPermissionGroup(Player player, String group) {
        // 尝试使用 Vault API
        try {
            // 检查是否有 Vault 权限插件
            if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
                // 这里需要 Vault API 的依赖，但我们使用权限节点模拟
                String permission = "group." + group;
                return player.hasPermission(permission);
            }
        } catch (Exception e) {
            // Vault 不可用，使用权限节点
        }

        // 使用权限节点检查
        String permission = "group." + group;
        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            if (info.getPermission().equalsIgnoreCase(permission) && info.getValue()) {
                return true;
            }
        }

        return false;
    }
}
