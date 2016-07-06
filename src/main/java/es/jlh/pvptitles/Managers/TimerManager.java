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
package es.jlh.pvptitles.Managers;

import es.jlh.pvptitles.Main.PvpTitles;
import es.jlh.pvptitles.Managers.Timer.TimedPlayer;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.OfflinePlayer;

public class TimerManager {

    private PvpTitles plugin = null;
    private Set<TimedPlayer> players = null;

    public TimerManager(PvpTitles plugin) {
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
