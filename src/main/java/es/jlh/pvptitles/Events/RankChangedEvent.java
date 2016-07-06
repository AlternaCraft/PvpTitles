/*
 * Copyright (C) 2016 AlternaCraft
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.jlh.pvptitles.Events;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

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
