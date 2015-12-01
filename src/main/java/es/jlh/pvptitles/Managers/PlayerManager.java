package es.jlh.pvptitles.Managers;

import es.jlh.pvptitles.Objects.TimedPlayer;
import es.jlh.pvptitles.Main.PvpTitles;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author AlternaCraft
 */
public class PlayerManager {

    private PvpTitles plugin = null;
    private Set<TimedPlayer> players = null;

    public PlayerManager(PvpTitles plugin) {
        this.plugin = plugin;
        this.players = new HashSet();
    }

    public boolean addPlayer(TimedPlayer tPlayer) {
        return this.players.add(tPlayer);
    }

    public TimedPlayer getPlayer(OfflinePlayer player) {
        for (TimedPlayer tPlayer : this.players) {
            if (tPlayer.getUniqueId().equals(player.getUniqueId())) {
                return tPlayer;
            }
        }
        return null;
    }

    public Set<TimedPlayer> getTimedPlayers() {
        return this.players;
    }

    public boolean hasPlayer(OfflinePlayer player) {
        return getPlayer(player) != null;
    }

    public void setTimedPlayers(Set<TimedPlayer> players) {
        this.players = players;
    }

    public void stopSessions() {
        for (TimedPlayer player : this.players) {
            player.stopSession();
        }
    }
}
