package cn.popcraft.mobassault.mob;

import cn.popcraft.mobassault.config.MobConfig;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class CustomMob {

    private final int id;
    private final LivingEntity entity;
    private final MobConfig config;
    private final String targetName;
    private LivingEntity currentTarget;
    private final Map<Integer, BukkitTask> behaviorTasks = new HashMap<>();

    public CustomMob(int id, LivingEntity entity, MobConfig config, String targetName) {
        this.id = id;
        this.entity = entity;
        this.config = config;
        this.targetName = targetName;
    }

    public int getId() {
        return id;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public MobConfig getConfig() {
        return config;
    }

    public String getTargetName() {
        return targetName;
    }

    public LivingEntity getCurrentTarget() {
        return currentTarget;
    }

    public void setCurrentTarget(LivingEntity currentTarget) {
        this.currentTarget = currentTarget;
    }

    public void addBehaviorTask(int taskId, BukkitTask task) {
        behaviorTasks.put(taskId, task);
    }

    public void cancelAllTasks() {
        for (BukkitTask task : behaviorTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        behaviorTasks.clear();
    }

    public boolean isValid() {
        return entity != null && !entity.isDead();
    }
}
