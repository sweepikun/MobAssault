package cn.popcraft.mobassault.mob.behavior;

import cn.popcraft.mobassault.mob.CustomMob;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

public class AttackBehavior implements MobBehavior {

    private final double attackRange;
    private final double accuracy;

    public AttackBehavior(double attackRange, double accuracy) {
        this.attackRange = attackRange;
        this.accuracy = accuracy;
    }

    @Override
    public void execute(CustomMob mob, LivingEntity target) {
        if (mob.getEntity() == null || mob.getEntity().isDead()) return;
        if (target == null || target.isDead()) return;

        // 检查距离
        double distance = mob.getEntity().getLocation().distance(target.getLocation());
        if (distance <= attackRange) {
            // 应用精准度
            if (Math.random() <= accuracy) {
                // 攻击目标
                if (mob.getEntity() instanceof Mob) {
                    Mob mobEntity = (Mob) mob.getEntity();
                    mobEntity.attack(target);
                }
            }
        } else {
            // 移动到目标
            if (mob.getEntity() instanceof Creature) {
                Creature creature = (Creature) mob.getEntity();
                creature.setTarget(target);
            }
        }
    }
}
