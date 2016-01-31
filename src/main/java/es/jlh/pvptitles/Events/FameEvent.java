package es.jlh.pvptitles.Events;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author AlternaCraft
 */
public class FameEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final OfflinePlayer player;
        
    private final int fame;
    private final int fameIncr;
    
    private String worldname = null;
    private int killstreak = 0;
    
    private boolean silent = false;
    private boolean cancelled;

    public FameEvent(OfflinePlayer player, int fame, int fameIncr) {
        this.player = player;
        this.fame = fame;
        this.fameIncr = fameIncr;
    }
    
    public OfflinePlayer getOfflinePlayer() {
        return player;
    }
    
    public int getFame() {
        return fame;
    }

    public int getFameIncr() {
        return fameIncr;
    }

    public int getFameTotal() {
        int total = ((this.fame+this.fameIncr) < 0) ? 0 : this.fame+this.fameIncr;
        return total;
    } 

    public int getKillstreak() {
        return killstreak;
    }

    public void setKillstreak(int killstreak) {
        this.killstreak = killstreak;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public String getWorldname() {
        return worldname;
    }

    public void setWorldname(String worldname) {
        this.worldname = worldname;
    }
    
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }    

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean bln) {
        this.cancelled = bln;
    }    
}
