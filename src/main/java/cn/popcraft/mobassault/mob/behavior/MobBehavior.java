package cn.popcraft.mobassault.mob.behavior;

import cn.popcraft.mobassault.mob.CustomMob;
import org.bukkit.entity.LivingEntity;

public interface MobBehavior {
    void execute(CustomMob mob, LivingEntity target);
}
