package cn.popcraft.mobassault.mob;

import cn.popcraft.mobassault.MobAssault;
import cn.popcraft.mobassault.config.MobConfig;
import cn.popcraft.mobassault.mob.behavior.*;
import cn.popcraft.mobassault.target.TargetSelector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class MobManager {

    private final MobAssault plugin;
    private final Map<Integer, CustomMob> activeMobs = new HashMap<>();
    private final Map<Integer, BukkitTask> behaviorTasks = new HashMap<>();
    private int nextId = 1;

    public MobManager(MobAssault plugin) {
        this.plugin = plugin;
    }

    public CustomMob spawnMob(String mobType, Location location, String target) {
        MobConfig config = plugin.getConfigManager().getMobConfig(mobType);
        if (config == null) {
            return null;
        }

        // 生成实体
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(config.getEntityType().toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("无效的实体类型: " + config.getEntityType());
            return null;
        }

        Entity entity = location.getWorld().spawnEntity(location, entityType);
        if (!(entity instanceof LivingEntity)) {
            entity.remove();
            return null;
        }

        LivingEntity livingEntity = (LivingEntity) entity;

        // 设置属性
        setupAttributes(livingEntity, config);

        // 设置装备
        setupEquipment(livingEntity, config);

        // 创建自定义怪物对象
        int id = nextId++;
        CustomMob customMob = new CustomMob(id, livingEntity, config, target);
        activeMobs.put(id, customMob);

        // 设置目标
        setupTarget(customMob, target);

        // 启动行为任务
        startBehaviorTasks(customMob);

        return customMob;
    }

    private void setupAttributes(LivingEntity entity, MobConfig config) {
        // 设置生命值
        if (entity.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(config.getHealth());
            entity.setHealth(config.getHealth());
        }

        // 设置攻击力
        if (entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(config.getDamage());
        }

        // 设置移动速度
        if (entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
            entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(config.getSpeed());
        }

        // 设置护甲
        if (entity.getAttribute(Attribute.GENERIC_ARMOR) != null) {
            entity.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(config.getArmor());
        }

        // 设置击退抗性
        if (entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE) != null) {
            entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(config.getKnockbackResistance());
        }
    }

    private void setupEquipment(LivingEntity entity, MobConfig config) {
        if (entity instanceof Mob) {
            Mob mob = (Mob) entity;

            // 设置主手武器
            ItemStack mainHand = config.getMainHandWeapon();
            if (mainHand != null) {
                mob.getEquipment().setItemInMainHand(mainHand);
                mob.getEquipment().setItemInMainHandDropChance(0.0f);
            }

            // 设置副手武器
            ItemStack offHand = config.getOffHandWeapon();
            if (offHand != null) {
                mob.getEquipment().setItemInOffHand(offHand);
                mob.getEquipment().setItemInOffHandDropChance(0.0f);
            }

            // 设置护甲
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
    }

    private void setupTarget(CustomMob customMob, String target) {
        // 获取目标玩家
        List<LivingEntity> targets = TargetSelector.getTargets(customMob.getConfig(), target, customMob.getEntity().getLocation());
        if (!targets.isEmpty()) {
            customMob.setCurrentTarget(targets.get(0));

            // 如果是怪物类型，设置AI目标
            if (customMob.getEntity() instanceof Mob) {
                Mob mob = (Mob) customMob.getEntity();
                mob.setTarget(targets.get(0));
            }
        }
    }

    private void startBehaviorTasks(CustomMob customMob) {
        MobConfig config = customMob.getConfig();

        // 火球行为
        if (config.isFireballEnabled()) {
            FireballBehavior behavior = new FireballBehavior(
                config.getFireballSpeed(),
                config.getFireballDamage(),
                config.getFireballAccuracy(),
                config.getFireballType()
            );
            startBehaviorTask(customMob, behavior, config.getFireballInterval());
        }

        // 爆炸行为
        if (config.isExplosionEnabled()) {
            ExplosionBehavior behavior = new ExplosionBehavior(
                config.getExplosionTriggerDistance(),
                config.getExplosionPower(),
                config.isExplosionFire(),
                config.isExplosionBreakBlocks()
            );
            startBehaviorTask(customMob, behavior, 10); // 每10tick检查一次
        }

        // 命令行为
        if (config.isCommandEnabled()) {
            CommandBehavior behavior = new CommandBehavior(config.getCommands());
            startBehaviorTask(customMob, behavior, config.getCommandInterval());
        }

        // Kether行为
        if (config.isKetherEnabled() && plugin.getConfigManager().getMainConfig().getBoolean("settings.enable-kether", true)) {
            KetherBehavior behavior = new KetherBehavior(config.getKetherScript());
            startBehaviorTask(customMob, behavior, config.getKetherInterval());
        }

        // 攻击任务
        AttackBehavior attackBehavior = new AttackBehavior(config.getAttackRange(), config.getAttackAccuracy());
        startBehaviorTask(customMob, attackBehavior, config.getAttackInterval());
    }

    private void startBehaviorTask(CustomMob customMob, MobBehavior behavior, int interval) {
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (customMob.getEntity() == null || customMob.getEntity().isDead()) {
                removeMob(customMob.getId());
                return;
            }

            // 更新目标
            updateTarget(customMob);

            // 执行行为
            if (customMob.getCurrentTarget() != null && !customMob.getCurrentTarget().isDead()) {
                behavior.execute(customMob, customMob.getCurrentTarget());
            }
        }, 0L, interval);

        customMob.addBehaviorTask(task.getTaskId(), task);
    }

    private void updateTarget(CustomMob customMob) {
        LivingEntity currentTarget = customMob.getCurrentTarget();

        // 检查当前目标是否有效
        if (currentTarget == null || currentTarget.isDead()) {
            // 重新选择目标
            List<LivingEntity> targets = TargetSelector.getTargets(
                customMob.getConfig(),
                customMob.getTargetName(),
                customMob.getEntity().getLocation()
            );

            if (!targets.isEmpty()) {
                customMob.setCurrentTarget(targets.get(0));
                if (customMob.getEntity() instanceof Mob) {
                    ((Mob) customMob.getEntity()).setTarget(targets.get(0));
                }
            }
        }
    }

    public void removeMob(int id) {
        CustomMob customMob = activeMobs.remove(id);
        if (customMob != null) {
            // 取消所有任务
            customMob.cancelAllTasks();

            // 移除实体
            if (customMob.getEntity() != null && !customMob.getEntity().isDead()) {
                customMob.getEntity().remove();
            }
        }
    }

    public void removeAllMobs() {
        for (CustomMob customMob : new ArrayList<>(activeMobs.values())) {
            removeMob(customMob.getId());
        }
    }

    public CustomMob getMob(int id) {
        return activeMobs.get(id);
    }

    public Collection<CustomMob> getAllMobs() {
        return Collections.unmodifiableCollection(activeMobs.values());
    }

    public List<String> getMobTypes() {
        return new ArrayList<>(plugin.getConfigManager().getAllMobConfigs().keySet());
    }

    public int getNextId() {
        return nextId++;
    }

    public void registerMob(int id, CustomMob customMob) {
        activeMobs.put(id, customMob);
    }

    public void startBehaviorTasks(CustomMob customMob, String target) {
        // 设置目标
        if (target != null) {
            setupTarget(customMob, target);
        }

        // 启动行为任务
        startBehaviorTasks(customMob);
    }
}
