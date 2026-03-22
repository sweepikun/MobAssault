package cn.popcraft.mobassault;

import cn.popcraft.mobassault.command.MobAssaultCommand;
import cn.popcraft.mobassault.config.ConfigManager;
import cn.popcraft.mobassault.listener.MobListener;
import cn.popcraft.mobassault.mob.MobManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MobAssault extends JavaPlugin {

    private static MobAssault instance;
    private ConfigManager configManager;
    private MobManager mobManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // 初始化配置管理器
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        
        // 初始化怪物管理器
        mobManager = new MobManager(this);
        
        // 注册命令
        MobAssaultCommand.register();
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new MobListener(this), this);
        
        getLogger().info("MobAssault 插件已启用！");
    }

    @Override
    public void onDisable() {
        // 清理所有怪物
        if (mobManager != null) {
            mobManager.removeAllMobs();
        }
        
        getLogger().info("MobAssault 插件已禁用！");
    }

    public static MobAssault getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MobManager getMobManager() {
        return mobManager;
    }
}
