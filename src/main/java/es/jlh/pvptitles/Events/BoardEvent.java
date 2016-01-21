package es.jlh.pvptitles.Events;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author AlternaCraft
 */
public class BoardEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final OfflinePlayer player;
    private final Location l;

    private boolean cancelled;

    public BoardEvent(OfflinePlayer player, Location l) {
        this.player = player;
        this.l = l;
    }

    public OfflinePlayer getOfflinePlayer() {
        return player;
    }

    public OfflinePlayer getPlayer() {
        return player.getPlayer();
    }

    public boolean isOnline() {
        return this.player.isOnline();
    }

    public Location getL() {
        return l;
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
