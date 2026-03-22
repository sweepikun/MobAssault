package cn.popcraft.mobassault.mob.behavior;

import cn.popcraft.mobassault.mob.CustomMob;
import org.bukkit.entity.LivingEntity;

public class ExplosionBehavior implements MobBehavior {

    private final double triggerDistance;
    private final float power;
    private final boolean fire;
    private final boolean breakBlocks;

    public ExplosionBehavior(double triggerDistance, float power, boolean fire, boolean breakBlocks) {
        this.triggerDistance = triggerDistance;
        this.power = power;
        this.fire = fire;
        this.breakBlocks = breakBlocks;
    }

    @Override
    public void execute(CustomMob mob, LivingEntity target) {
        if (mob.getEntity() == null || mob.getEntity().isDead()) return;
        if (target == null || target.isDead()) return;

        // 检查距离
        double distance = mob.getEntity().getLocation().distance(target.getLocation());
        if (distance <= triggerDistance) {
            // 创建爆炸
            mob.getEntity().getWorld().createExplosion(
                mob.getEntity().getLocation(),
                power,
                fire,
                breakBlocks
            );

            // 移除怪物
            mob.getEntity().remove();
        }
    }
}
