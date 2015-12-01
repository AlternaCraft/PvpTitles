package es.jlh.pvptitles.Managers;

import es.jlh.pvptitles.Objects.TimedPlayer;
import es.jlh.pvptitles.Main.PvpTitles;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author AlternaCraft
 */
public class MovementManager {

    private PvpTitles plugin = null;
    private Map<UUID, Long> lastMovement = null;
    private final int timeThreshold;

    public MovementManager(PvpTitles plugin) {
        this.plugin = plugin;
        this.lastMovement = new HashMap();        
        this.timeThreshold = plugin.cm.params.getAFKTime() * 60;
    }

    public boolean isAFK(OfflinePlayer player) {
        if (!hasLastMovement(player)) {
            return false;
        }
        return getAFKTime(player) >= 0;
    }

    public int getAFKTime(OfflinePlayer player) {
        long lastMove = this.lastMovement.get(player.getUniqueId());
        long currTime = System.currentTimeMillis();
        int timeDiff = (int) ((currTime - lastMove) / 1000L);       
        
        return timeDiff - this.timeThreshold;
    }

    public boolean hasLastMovement(OfflinePlayer player) {
        return this.lastMovement.containsKey(player.getUniqueId());
    }

    public long getLastMovement(OfflinePlayer player) {
        if (!hasLastMovement(player)) {
            return 0L;
        }
        return this.lastMovement.get(player.getUniqueId());
    }

    public void addLastMovement(OfflinePlayer player) {
        if (!this.plugin.cm.params.isCheckAFK()) {
            return;
        }
        if (isAFK(player)) {
            TimedPlayer tPlayer = this.plugin.getPlayerManager().getPlayer(player);
            tPlayer.setAFKTime(tPlayer.getAFKTime() + getAFKTime(player));
        }
        this.lastMovement.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void removeLastMovement(OfflinePlayer player) {
        this.lastMovement.remove(player.getUniqueId());
    }
}
