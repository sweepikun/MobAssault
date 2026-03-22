package cn.popcraft.mobassault.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobConfig {

    private final String type;
    private final ConfigurationSection config;

    public MobConfig(String type, ConfigurationSection config) {
        this.type = type;
        this.config = config;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return colorize(config.getString("name", type));
    }

    public String getEntityType() {
        return config.getString("type", "ZOMBIE");
    }

    // 属性配置
    public double getHealth() {
        return config.getDouble("attributes.health", 20.0);
    }

    public double getDamage() {
        return config.getDouble("attributes.damage", 5.0);
    }

    public double getSpeed() {
        return config.getDouble("attributes.speed", 0.25);
    }

    public double getArmor() {
        return config.getDouble("attributes.armor", 0.0);
    }

    public double getKnockbackResistance() {
        return config.getDouble("attributes.knockback-resistance", 0.0);
    }

    // 攻击配置
    public int getAttackInterval() {
        return config.getInt("attack.interval", 20);
    }

    public double getAttackAccuracy() {
        return config.getDouble("attack.accuracy", 0.8);
    }

    public double getAttackRange() {
        return config.getDouble("attack.range", 3.0);
    }

    // 目标配置
    public String getTargetType() {
        return config.getString("target.type", "PLAYER");
    }

    public List<String> getTargetPlayers() {
        return config.getStringList("target.players");
    }

    public List<String> getTargetGroups() {
        return config.getStringList("target.groups");
    }

    // 武器配置
    public ItemStack getMainHandWeapon() {
        return createItemFromConfig("weapon.main-hand");
    }

    public ItemStack getOffHandWeapon() {
        return createItemFromConfig("weapon.off-hand");
    }

    // 护甲配置
    public ItemStack getHelmet() {
        return createItemFromConfig("armor.helmet");
    }

    public ItemStack getChestplate() {
        return createItemFromConfig("armor.chestplate");
    }

    public ItemStack getLeggings() {
        return createItemFromConfig("armor.leggings");
    }

    public ItemStack getBoots() {
        return createItemFromConfig("armor.boots");
    }

    // 行为配置
    public boolean isFireballEnabled() {
        return config.getBoolean("behaviors.fireball.enabled", false);
    }

    public int getFireballInterval() {
        return config.getInt("behaviors.fireball.interval", 60);
    }

    public double getFireballSpeed() {
        return config.getDouble("behaviors.fireball.speed", 1.5);
    }

    public double getFireballDamage() {
        return config.getDouble("behaviors.fireball.damage", 5.0);
    }

    public double getFireballAccuracy() {
        return config.getDouble("behaviors.fireball.accuracy", 0.7);
    }

    public String getFireballType() {
        return config.getString("behaviors.fireball.type", "SMALL");
    }

    public boolean isExplosionEnabled() {
        return config.getBoolean("behaviors.explosion.enabled", false);
    }

    public double getExplosionTriggerDistance() {
        return config.getDouble("behaviors.explosion.trigger-distance", 5.0);
    }

    public float getExplosionPower() {
        return (float) config.getDouble("behaviors.explosion.power", 2.0);
    }

    public boolean isExplosionFire() {
        return config.getBoolean("behaviors.explosion.fire", false);
    }

    public boolean isExplosionBreakBlocks() {
        return config.getBoolean("behaviors.explosion.break-blocks", false);
    }

    public boolean isCommandEnabled() {
        return config.getBoolean("behaviors.command.enabled", false);
    }

    public int getCommandInterval() {
        return config.getInt("behaviors.command.interval", 100);
    }

    public List<String> getCommands() {
        return config.getStringList("behaviors.command.commands");
    }

    public boolean isKetherEnabled() {
        return config.getBoolean("behaviors.kether.enabled", false);
    }

    public int getKetherInterval() {
        return config.getInt("behaviors.kether.interval", 50);
    }

    public String getKetherScript() {
        return config.getString("behaviors.kether.script", "");
    }

    private ItemStack createItemFromConfig(String path) {
        ConfigurationSection itemConfig = config.getConfigurationSection(path);
        if (itemConfig == null) return null;

        String materialName = itemConfig.getString("material", "STONE");
        Material material = Material.matchMaterial(materialName);
        if (material == null) return null;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // 设置名称
        String name = itemConfig.getString("name");
        if (name != null) {
            meta.setDisplayName(colorize(name));
        }

        // 设置描述
        List<String> lore = itemConfig.getStringList("lore");
        if (!lore.isEmpty()) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(colorize(line));
            }
            meta.setLore(coloredLore);
        }

        item.setItemMeta(meta);

        // 添加附魔
        List<Map<?, ?>> enchantments = itemConfig.getMapList("enchantments");
        for (Map<?, ?> enchMap : enchantments) {
            String enchName = (String) enchMap.get("type");
            int level = (int) enchMap.get("level");
            Enchantment enchantment = Enchantment.getByName(enchName);
            if (enchantment != null) {
                item.addUnsafeEnchantment(enchantment, level);
            }
        }

        return item;
    }

    private String colorize(String text) {
        if (text == null) return null;
        return text.replace("&", "\u00a7");
    }

    public ConfigurationSection getConfig() {
        return config;
    }
}
