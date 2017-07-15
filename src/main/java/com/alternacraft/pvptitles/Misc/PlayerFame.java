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
package com.alternacraft.pvptitles.Misc;

import com.alternacraft.pvptitles.Exceptions.DBException;
import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.Manager;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

public class PlayerFame implements Comparable {

    private String uuid = null;
    private int fame = 0;
    private long seconds = 0;
    private short server = 0;
    private String world = "";

    public PlayerFame(String name, int fame, long seconds) {
        this.uuid = name;
        this.fame = fame;
        this.seconds = seconds;
    }

    public String getName() {
        String nombre = getPlayer().getName();
        return (nombre == null) ? "<?>" : nombre;
    }

    public OfflinePlayer getPlayer() {
        UUID playerUUID = UUID.fromString(this.uuid);
        return Bukkit.getServer().getOfflinePlayer(playerUUID);
    }

    public int getFame() {
        return fame;
    }

    public void setFame(int fame) {
        this.fame = fame;
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

    public long getSeconds() {
        return this.seconds;
    }

    public long getRealSeconds() {
        long actual = 0;
        try {
            actual = Manager.getInstance().getDBH().getDM().loadPlayedTime(UUID.fromString(uuid));
        } catch (DBException ex) {
            CustomLogger.logArrayError(ex.getCustomStackTrace());
        }

        long session = Manager.getInstance().getTimerManager().getPlayer(Bukkit.getServer()
                .getOfflinePlayer(UUID.fromString(uuid))).getTotalOnline();

        return actual + session;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public String getServerName() throws DBException {
        return Manager.getInstance().getDBH().getDM().getServerName(this.server);
    }

    public String getMWName() {
        String worldName = "";
        if (!"".equals(this.world)) {
            worldName = "[" + this.world + "] ";
        }
        return worldName + this.getName();
    }

    @Override
    public String toString() {
        String worldName = "";
        if (!"".equals(this.world)) {
            worldName = "[" + this.world + "] ";
        }
        return worldName + this.getName() + " (" + ChatColor.AQUA + this.getFame() + ChatColor.RESET + ")";
    }

    @Override
    public int compareTo(Object o) {
        PlayerFame pf = (PlayerFame) o;

        if (pf.getFame() > this.getFame()) {
            return 1;
        } else if (pf.getFame() < this.getFame()) {
            return -1;
        } else {
            return 0;
        }
    }
}
