package cn.popcraft.mobassault.listener;

import cn.popcraft.mobassault.MobAssault;
import cn.popcraft.mobassault.config.MobConfig;
import cn.popcraft.mobassault.mob.CustomMob;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MobListener implements Listener {

    private final MobAssault plugin;
    // 记录玩家最后使用的刷怪蛋配置类型
    private final Map<UUID, String> lastUsedEgg = new HashMap<>();

    public MobListener(MobAssault plugin) {
        this.plugin = plugin;
    }

    public void setLastUsedEgg(Player player, String mobType) {
        lastUsedEgg.put(player.getUniqueId(), mobType);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled()) return;
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) return;

        LivingEntity entity = event.getEntity();

        // 查找最近使用刷怪蛋的玩家
        Player spawner = null;
        String mobType = null;

        for (Player player : entity.getWorld().getPlayers()) {
            if (player.getLocation().distance(entity.getLocation()) < 10) {
                String type = lastUsedEgg.get(player.getUniqueId());
                if (type != null) {
                    spawner = player;
                    mobType = type;
                    break;
                }
            }
        }

        if (mobType == null) return;

        MobConfig config = plugin.getConfigManager().getMobConfig(mobType);
        if (config == null) return;

        // 检查实体类型是否匹配
        String expectedType = config.getEntityType().toUpperCase();
        if (!entity.getType().name().equalsIgnoreCase(expectedType)) return;

        // 应用配置
        applyConfig(entity, config, spawner != null ? spawner.getName() : null);

        // 清除已使用的记录
        if (spawner != null) {
            lastUsedEgg.remove(spawner.getUniqueId());
        }
    }

    private void applyConfig(LivingEntity entity, MobConfig config, String targetName) {
        // 设置属性
        if (entity.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(config.getHealth());
            entity.setHealth(config.getHealth());
        }

        if (entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(config.getDamage());
        }

        if (entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
            entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(config.getSpeed());
        }

        if (entity.getAttribute(Attribute.GENERIC_ARMOR) != null) {
            entity.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(config.getArmor());
        }

        if (entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE) != null) {
            entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(config.getKnockbackResistance());
        }

        // 设置装备
        if (entity instanceof Mob) {
            Mob mob = (Mob) entity;

            ItemStack mainHand = config.getMainHandWeapon();
            if (mainHand != null) {
                mob.getEquipment().setItemInMainHand(mainHand);
                mob.getEquipment().setItemInMainHandDropChance(0.0f);
            }

            ItemStack offHand = config.getOffHandWeapon();
            if (offHand != null) {
                mob.getEquipment().setItemInOffHand(offHand);
                mob.getEquipment().setItemInOffHandDropChance(0.0f);
            }

            ItemStack helmet = config.getHelmet();
            if (helmet != null) {
                mob.getEquipment().setHelmet(helmet);
                mob.getEquipment().setHelmetDropChance(0.0f);
            }

            ItemStack chestplate = config.getChestplate();
            if (chestplate != null) {
                mob.getEquipment().setChestplate(chestplate);
                mob.getEquipment().setChestplateDropChance(0.0f);
            }

            ItemStack leggings = config.getLeggings();
            if (leggings != null) {
                mob.getEquipment().setLeggings(leggings);
                mob.getEquipment().setLeggingsDropChance(0.0f);
            }

            ItemStack boots = config.getBoots();
            if (boots != null) {
                mob.getEquipment().setBoots(boots);
                mob.getEquipment().setBootsDropChance(0.0f);
            }
        }

        // 注册为自定义怪物
        int id = plugin.getMobManager().getNextId();
        String target = targetName != null ? targetName : (config.getTargetPlayers().isEmpty() ? null : config.getTargetPlayers().get(0));
        CustomMob customMob = new CustomMob(id, entity, config, target);
        plugin.getMobManager().registerMob(id, customMob);
        plugin.getMobManager().startBehaviorTasks(customMob, target);
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
