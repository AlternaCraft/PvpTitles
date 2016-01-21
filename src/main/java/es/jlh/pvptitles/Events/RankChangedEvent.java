package es.jlh.pvptitles.Events;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author AlternaCraft
 */
public class RankChangedEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    
    private OfflinePlayer player = null;
    private String lastRank = null;
    private String newRank = null;

    private boolean cancelled;
    
    public RankChangedEvent(OfflinePlayer pl, String lastRank, String newRank) {
        this.player = pl;
        this.lastRank = lastRank;
        this.newRank = newRank;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public String getLastRank() {
        return lastRank;
    }

    public String getNewRank() {
        return newRank;
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
