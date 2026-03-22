package cn.popcraft.mobassault.config;

import cn.popcraft.mobassault.MobAssault;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {

    private final MobAssault plugin;
    private FileConfiguration mainConfig;
    private final Map<String, MobConfig> mobConfigs = new HashMap<>();

    public ConfigManager(MobAssault plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        // 加载主配置
        loadMainConfig();
        
        // 加载怪物配置
        loadMobConfigs();
    }

    private void loadMainConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        mainConfig = YamlConfiguration.loadConfiguration(configFile);
        
        // 加载默认配置
        InputStream defaultStream = plugin.getResource("config.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
            );
            mainConfig.setDefaults(defaultConfig);
        }
    }

    private void loadMobConfigs() {
        mobConfigs.clear();
        File mobsFolder = new File(plugin.getDataFolder(), "mobs");
        if (!mobsFolder.exists()) {
            mobsFolder.mkdirs();
            // 复制默认配置文件
            saveResource("mobs/zombie.yml", false);
            saveResource("mobs/skeleton.yml", false);
            saveResource("mobs/custom/fire_golem.yml", false);
        }
        
        // 加载所有怪物配置
        loadMobConfigsFromFolder(mobsFolder, "");
    }

    private void loadMobConfigsFromFolder(File folder, String prefix) {
        File[] files = folder.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                loadMobConfigsFromFolder(file, prefix + file.getName() + "/");
            } else if (file.getName().endsWith(".yml")) {
                String mobType = prefix + file.getName().replace(".yml", "");
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                MobConfig mobConfig = new MobConfig(mobType, config);
                mobConfigs.put(mobType.toLowerCase(), mobConfig);
            }
        }
    }

    public void reloadConfigs() {
        loadConfigs();
    }

    public FileConfiguration getMainConfig() {
        return mainConfig;
    }

    public MobConfig getMobConfig(String mobType) {
        return mobConfigs.get(mobType.toLowerCase());
    }

    public Map<String, MobConfig> getAllMobConfigs() {
        return mobConfigs;
    }

    public String getMessage(String key) {
        String prefix = mainConfig.getString("messages.prefix", "&8[&cMobAssault&8] &7");
        String message = mainConfig.getString("messages." + key, key);
        return colorize(prefix + message);
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return message;
    }

    private String colorize(String text) {
        return text.replace("&", "\u00a7");
    }

    private void saveResource(String resourcePath, boolean replace) {
        File outFile = new File(plugin.getDataFolder(), resourcePath);
        if (outFile.exists() && !replace) {
            return;
        }
        
        InputStream in = plugin.getResource(resourcePath);
        if (in == null) {
            plugin.getLogger().log(Level.WARNING, "无法找到资源文件: " + resourcePath);
            return;
        }
        
        try {
            // 确保父目录存在
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            
            java.nio.file.Files.copy(in, outFile.toPath(), 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "无法保存资源文件: " + resourcePath, e);
        }
    }
}
