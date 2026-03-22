package cn.popcraft.mobassault.command;

import cn.popcraft.mobassault.MobAssault;
import cn.popcraft.mobassault.config.MobConfig;
import cn.popcraft.mobassault.listener.MobListener;
import cn.popcraft.mobassault.mob.CustomMob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class MobAssaultCommand implements CommandExecutor, TabCompleter {

    private final MobAssault plugin;

    public MobAssaultCommand(MobAssault plugin) {
        this.plugin = plugin;
    }

    public static void register() {
        MobAssault plugin = MobAssault.getInstance();
        MobAssaultCommand command = new MobAssaultCommand(plugin);

        plugin.getCommand("mobassault").setExecutor(command);
        plugin.getCommand("mobassault").setTabCompleter(command);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "spawn":
                return handleSpawn(sender, args);
            case "egg":
                return handleEgg(sender, args);
            case "remove":
                return handleRemove(sender, args);
            case "removeall":
                return handleRemoveAll(sender);
            case "list":
                return handleList(sender);
            case "reload":
                return handleReload(sender);
            case "config":
                return handleConfig(sender, args);
            case "help":
                sendHelp(sender);
                return true;
            default:
                sendMessage(sender, "invalid-args");
                return true;
        }
    }

    private boolean handleSpawn(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mobassault.spawn")) {
            sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length < 2) {
            sendMessage(sender, "invalid-args", "%usage%", "/ma spawn <怪物类型> [玩家名/权限组]");
            return true;
        }

        String mobType = args[1].toLowerCase();
        MobConfig config = plugin.getConfigManager().getMobConfig(mobType);

        if (config == null) {
            sendMessage(sender, "unknown-mob-type", "%type%", mobType);
            return true;
        }

        // 确定目标
        String target = null;
        Location spawnLocation = null;

        if (args.length >= 3) {
            String targetArg = args[2];

            // 检查是否是权限组参数
            if (targetArg.equalsIgnoreCase("-g") && args.length >= 4) {
                String group = args[3];
                target = "group:" + group;
            } else {
                // 指定玩家
                target = targetArg;
                Player targetPlayer = Bukkit.getPlayer(targetArg);
                if (targetPlayer != null) {
                    spawnLocation = targetPlayer.getLocation().add(
                        (Math.random() - 0.5) * 10,
                        0,
                        (Math.random() - 0.5) * 10
                    );
                }
            }
        }

        // 如果没有指定目标或目标是玩家，使用发送者位置
        if (spawnLocation == null) {
            if (sender instanceof Player) {
                spawnLocation = ((Player) sender).getLocation();
            } else {
                sendMessage(sender, "console-not-allowed");
                return true;
            }
        }

        // 如果没有指定目标，使用配置中的默认目标
        if (target == null) {
            if (config.getTargetPlayers().isEmpty()) {
                target = sender instanceof Player ? sender.getName() : null;
            } else {
                target = config.getTargetPlayers().get(0);
            }
        }

        // 生成怪物
        CustomMob customMob = plugin.getMobManager().spawnMob(mobType, spawnLocation, target);

        if (customMob != null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("id", String.valueOf(customMob.getId()));
            sendMessage(sender, "mob-spawned", placeholders);
        } else {
            sendMessage(sender, "unknown-mob-type", "%type%", mobType);
        }

        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mobassault.remove")) {
            sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length < 2) {
            sendMessage(sender, "invalid-args", "%usage%", "/ma remove <实体ID>");
            return true;
        }

        try {
            int id = Integer.parseInt(args[1]);
            CustomMob mob = plugin.getMobManager().getMob(id);

            if (mob != null) {
                plugin.getMobManager().removeMob(id);
                sendMessage(sender, "mob-removed");
            } else {
                sendMessage(sender, "invalid-args");
            }
        } catch (NumberFormatException e) {
            sendMessage(sender, "invalid-args");
        }

        return true;
    }

    private boolean handleRemoveAll(CommandSender sender) {
        if (!sender.hasPermission("mobassault.removeall")) {
            sendMessage(sender, "no-permission");
            return true;
        }

        plugin.getMobManager().removeAllMobs();
        sendMessage(sender, "all-mobs-removed");
        return true;
    }

    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("mobassault.list")) {
            sendMessage(sender, "no-permission");
            return true;
        }

        Collection<CustomMob> mobs = plugin.getMobManager().getAllMobs();

        if (mobs.isEmpty()) {
            sendMessage(sender, "mob-list-empty");
            return true;
        }

        sendMessage(sender, "mob-list-header");
        for (CustomMob mob : mobs) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("id", String.valueOf(mob.getId()));
            placeholders.put("type", mob.getConfig().getEntityType());
            placeholders.put("target", mob.getTargetName());
            sendMessage(sender, "mob-list-entry", placeholders);
        }

        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("mobassault.reload")) {
            sendMessage(sender, "no-permission");
            return true;
        }

        plugin.getConfigManager().reloadConfigs();
        sendMessage(sender, "config-reloaded");
        return true;
    }

    private boolean handleConfig(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mobassault.config")) {
            sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length < 2) {
            sendMessage(sender, "invalid-args", "%usage%", "/ma config <怪物类型>");
            return true;
        }

        String mobType = args[1].toLowerCase();
        MobConfig config = plugin.getConfigManager().getMobConfig(mobType);

        if (config == null) {
            sendMessage(sender, "unknown-mob-type", "%type%", mobType);
            return true;
        }

        sendMessage(sender, "config-header", "%type%", mobType);
        sender.sendMessage("§7类型: §f" + config.getEntityType());
        sender.sendMessage("§7生命值: §f" + config.getHealth());
        sender.sendMessage("§7攻击力: §f" + config.getDamage());
        sender.sendMessage("§7移动速度: §f" + config.getSpeed());
        sender.sendMessage("§7攻击间隔: §f" + config.getAttackInterval() + " ticks");
        sender.sendMessage("§7攻击精准度: §f" + (config.getAttackAccuracy() * 100) + "%");

        return true;
    }

    private boolean handleEgg(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mobassault.egg")) {
            sendMessage(sender, "no-permission");
            return true;
        }

        if (!(sender instanceof Player)) {
            sendMessage(sender, "console-not-allowed");
            return true;
        }

        if (args.length < 2) {
            sendMessage(sender, "invalid-args", "%usage%", "/ma egg <怪物类型> [数量]");
            return true;
        }

        String mobType = args[1].toLowerCase();
        MobConfig config = plugin.getConfigManager().getMobConfig(mobType);

        if (config == null) {
            sendMessage(sender, "unknown-mob-type", "%type%", mobType);
            return true;
        }

        // 获取数量
        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount < 1) amount = 1;
                if (amount > 64) amount = 64;
            } catch (NumberFormatException e) {
                // 保持默认值 1
            }
        }

        Player player = (Player) sender;
        ItemStack egg = plugin.getSpawnerManager().createSpawnEgg(mobType);

        if (egg == null) {
            sendMessage(sender, "unknown-mob-type", "%type%", mobType);
            return true;
        }

        egg.setAmount(amount);
        player.getInventory().addItem(egg);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("type", mobType);
        placeholders.put("amount", String.valueOf(amount));
        sendMessage(sender, "egg-received", placeholders);

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== MobAssault 帮助 ===");
        sender.sendMessage("§7/ma spawn <怪物类型> [玩家名] §8- §f生成怪物攻击玩家");
        sender.sendMessage("§7/ma spawn <怪物类型> -g <权限组> §8- §f生成怪物攻击权限组");
        sender.sendMessage("§7/ma egg <怪物类型> [数量] §8- §f获取自定义刷怪蛋");
        sender.sendMessage("§7/ma remove <实体ID> §8- §f移除怪物");
        sender.sendMessage("§7/ma removeall §8- §f移除所有怪物");
        sender.sendMessage("§7/ma list §8- §f列出所有活动怪物");
        sender.sendMessage("§7/ma reload §8- §f重载配置");
        sender.sendMessage("§7/ma config <怪物类型> §8- §f查看怪物配置");
        sender.sendMessage("§7/ma help §8- §f显示此帮助");
    }

    private void sendMessage(CommandSender sender, String key) {
        sender.sendMessage(plugin.getConfigManager().getMessage(key));
    }

    private void sendMessage(CommandSender sender, String key, String placeholder, String value) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(placeholder, value);
        sender.sendMessage(plugin.getConfigManager().getMessage(key, placeholders));
    }

    private void sendMessage(CommandSender sender, String key, Map<String, String> placeholders) {
        sender.sendMessage(plugin.getConfigManager().getMessage(key, placeholders));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filterCompletions(args[0], Arrays.asList(
                "spawn", "egg", "remove", "removeall", "list", "reload", "config", "help"
            ));
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("spawn") || subCommand.equals("config") || subCommand.equals("egg")) {
                return filterCompletions(args[1], plugin.getMobManager().getMobTypes());
            } else if (subCommand.equals("remove")) {
                return plugin.getMobManager().getAllMobs().stream()
                    .map(mob -> String.valueOf(mob.getId()))
                    .collect(Collectors.toList());
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("spawn")) {
            List<String> completions = new ArrayList<>();
            completions.add("-g");
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList()));
            return filterCompletions(args[2], completions);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("egg")) {
            return filterCompletions(args[2], Arrays.asList("1", "8", "16", "32", "64"));
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("spawn") && args[2].equalsIgnoreCase("-g")) {
            return filterCompletions(args[3], Arrays.asList("player", "vip", "mvp", "admin"));
        }

        return Collections.emptyList();
    }

    private List<String> filterCompletions(String input, List<String> completions) {
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
            .collect(Collectors.toList());
    }
}
