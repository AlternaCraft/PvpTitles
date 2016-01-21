package es.jlh.pvptitles.Objects;

import es.jlh.pvptitles.Main.PvpTitles;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 *
 * @author AlternaCraft
 */
public class PlayerFame implements Comparable {
    private String uuid = null;
    private int fame = 0;
    private int seconds = 0;
    private short server = 0;
    private String world = "";
    private PvpTitles plugin = null;
    
    public PlayerFame(String name, int fame, int seconds, PvpTitles pl) {
        this.uuid = name;
        this.fame = fame;
        this.seconds = seconds;
        this.plugin = pl;
    }
    
    public String getName() {
        UUID playerUUID = UUID.fromString(this.uuid);
        String nombre = Bukkit.getOfflinePlayer(playerUUID).getName();
        return (nombre == null) ? "<?>":nombre;
    }
    
    public int getFame() {
        return fame;
    }
    
    public String getUUID() {
        return uuid;
    }

    public int getServer() {
        return server;
    }

    public void setServer(short server) {
        this.server = server;
    }

    public int getSeconds() {        
        return this.seconds;
    }
    
    public int getRealSeconds() {
        int actual = plugin.cm.dbh.getDm().loadPlayedTime(UUID.fromString(uuid));
        int session = plugin.getPlayerManager().getPlayer(Bukkit
                .getOfflinePlayer(UUID.fromString(uuid))).getTotalOnline();
        
        return actual+session;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }
    
    public String getServerName() {
        return plugin.cm.dbh.getDm().getServerName(this.server);
    }
    
    @Override
    public String toString() {
        String worldName = "";
        if (!"".equals(this.world)) {
            worldName = "["+this.world+"] ";
        }
        return worldName + this.getName() + " (" + ChatColor.AQUA + this.getFame() + ChatColor.RESET + ")";
    }

    @Override
    public int compareTo(Object o) {
        PlayerFame pf = (PlayerFame) o;
        
        if (pf.getFame() > this.getFame()) {
            return 1;
        }
        else if (pf.getFame() < this.getFame()) {
            return -1;
        }
        else {
            return 0;
        }
    }
}
