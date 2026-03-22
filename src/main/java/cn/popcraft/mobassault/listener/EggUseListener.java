package cn.popcraft.mobassault.listener;

import cn.popcraft.mobassault.MobAssault;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class EggUseListener implements Listener {

    private final MobAssault plugin;
    private final MobListener mobListener;

    public EggUseListener(MobAssault plugin, MobListener mobListener) {
        this.plugin = plugin;
        this.mobListener = mobListener;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        String mobType = plugin.getSpawnerManager().getMobTypeFromEgg(item);
        if (mobType != null) {
            // 记录玩家最后使用的刷怪蛋类型
            mobListener.setLastUsedEgg(player, mobType);
        }
    }
}
