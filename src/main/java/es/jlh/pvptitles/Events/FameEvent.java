package es.jlh.pvptitles.Events;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author julito
 */
public class FameEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final OfflinePlayer player;
    
    private final int fame;
    private final int fameIncr;
    
    private boolean cancelled;

    public FameEvent(OfflinePlayer player, int fame, int fameIncr) {
        this.player = player;
        this.fame = fame;
        this.fameIncr = fameIncr;
    }
    
    public OfflinePlayer getOfflinePlayer() {
        return player;
    }
    
    public Player getPlayer() {
        return player.getPlayer();
    }

    public boolean isOnline() {
        return this.player.isOnline();
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
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
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
