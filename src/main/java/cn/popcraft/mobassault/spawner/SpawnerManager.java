package cn.popcraft.mobassault.spawner;

import cn.popcraft.mobassault.MobAssault;
import cn.popcraft.mobassault.config.MobConfig;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class SpawnerManager {

    private final MobAssault plugin;
    private final NamespacedKey mobTypeKey;

    public SpawnerManager(MobAssault plugin) {
        this.plugin = plugin;
        this.mobTypeKey = new NamespacedKey(plugin, "mobassault_type");
    }

    public ItemStack createSpawnEgg(String mobType) {
        MobConfig config = plugin.getConfigManager().getMobConfig(mobType);
        if (config == null) {
            return null;
        }

        // 获取对应的刷怪蛋材质
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(config.getEntityType().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }

        Material eggMaterial = getSpawnEggMaterial(entityType);
        if (eggMaterial == null) {
            return null;
        }

        ItemStack egg = new ItemStack(eggMaterial);
        ItemMeta meta = egg.getItemMeta();
        if (meta == null) {
            return egg;
        }

        // 设置名称
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getName() + " &f刷怪蛋"));

        // 设置描述
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "类型: " + ChatColor.WHITE + config.getEntityType());
        lore.add(ChatColor.GRAY + "生命值: " + ChatColor.WHITE + config.getHealth());
        lore.add(ChatColor.GRAY + "攻击力: " + ChatColor.WHITE + config.getDamage());
        lore.add(ChatColor.GRAY + "攻击间隔: " + ChatColor.WHITE + config.getAttackInterval() + " ticks");
        lore.add(ChatColor.GRAY + "精准度: " + ChatColor.WHITE + (int)(config.getAttackAccuracy() * 100) + "%");
        lore.add("");
        lore.add(ChatColor.YELLOW + "右键放置生成自定义怪物");
        meta.setLore(lore);

        // 存储配置类型到 PersistentDataContainer
        meta.getPersistentDataContainer().set(mobTypeKey, PersistentDataType.STRING, mobType);

        egg.setItemMeta(meta);
        return egg;
    }

    public String getMobTypeFromEgg(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        return meta.getPersistentDataContainer().get(mobTypeKey, PersistentDataType.STRING);
    }

    private Material getSpawnEggMaterial(EntityType type) {
        // 1.17+ 刷怪蛋命名规则
        try {
            String eggName = type.name() + "_SPAWN_EGG";
            return Material.valueOf(eggName);
        } catch (IllegalArgumentException e) {
            // 某些实体可能没有对应的刷怪蛋
            return null;
        }
    }

    public NamespacedKey getMobTypeKey() {
        return mobTypeKey;
    }
}
