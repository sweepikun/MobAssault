package cn.popcraft.mobassault.mob.behavior;

import cn.popcraft.mobassault.mob.CustomMob;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

public class FireballBehavior implements MobBehavior {

    private final double speed;
    private final double damage;
    private final double accuracy;
    private final String fireballType;

    public FireballBehavior(double speed, double damage, double accuracy, String fireballType) {
        this.speed = speed;
        this.damage = damage;
        this.accuracy = accuracy;
        this.fireballType = fireballType;
    }

    @Override
    public void execute(CustomMob mob, LivingEntity target) {
        if (mob.getEntity() == null || mob.getEntity().isDead()) return;
        if (target == null || target.isDead()) return;

        // 计算方向，考虑精准度
        Vector direction = calculateDirection(mob.getEntity(), target);

        // 根据类型生成火球
        Location spawnLoc = mob.getEntity().getLocation().add(0, 1, 0);
        Fireball fireball;

        switch (fireballType.toUpperCase()) {
            case "LARGE":
                fireball = mob.getEntity().getWorld().spawn(spawnLoc, LargeFireball.class);
                break;
            case "DRAGON":
                fireball = mob.getEntity().getWorld().spawn(spawnLoc, DragonFireball.class);
                break;
            case "SMALL":
            default:
                fireball = mob.getEntity().getWorld().spawn(spawnLoc, SmallFireball.class);
                break;
        }

        fireball.setDirection(direction);
        fireball.setShooter(mob.getEntity());
    }

    private Vector calculateDirection(LivingEntity shooter, LivingEntity target) {
        // 计算从射击者到目标的方向
        Location shooterLoc = shooter.getLocation().add(0, 1, 0); // 从眼睛高度发射
        Location targetLoc = target.getLocation().add(0, 1, 0); // 瞄准目标眼睛高度

        Vector direction = targetLoc.toVector().subtract(shooterLoc.toVector()).normalize();

        // 应用精准度偏移
        if (accuracy < 1.0) {
            double offset = (1.0 - accuracy) * 2.0; // 偏移量
            direction.setX(direction.getX() + (Math.random() - 0.5) * offset);
            direction.setY(direction.getY() + (Math.random() - 0.5) * offset * 0.5); // 垂直偏移较小
            direction.setZ(direction.getZ() + (Math.random() - 0.5) * offset);
            direction.normalize();
        }

        return direction.multiply(speed);
    }
}
