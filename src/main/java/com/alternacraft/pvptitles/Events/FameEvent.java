/*
 * Copyright (C) 2017 AlternaCraft
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
package com.alternacraft.pvptitles.Events;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

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
