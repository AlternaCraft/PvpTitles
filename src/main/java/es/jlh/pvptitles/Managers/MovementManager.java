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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.OfflinePlayer;

public class MovementManager {

    private PvpTitles plugin = null;
    private Map<UUID, Long> lastMovement = null;
    private final int timeThreshold;

    public MovementManager(PvpTitles plugin) {
        this.plugin = plugin;
        this.lastMovement = new HashMap();        
        this.timeThreshold = plugin.getManager().params.getAFKTime() * 60;
    }

    public boolean isAFK(OfflinePlayer player) {
        if (!hasLastMovement(player)) {
            return false;
        }
        return getAFKTime(player) > 0; // Devuelve '0' si el comprobador esta desactivado
    }

    public int getAFKTime(OfflinePlayer player) {
        long lastMove = this.lastMovement.get(player.getUniqueId());
        long currTime = System.currentTimeMillis();
        int timeDiff = (int) ((currTime - lastMove) / 1000L);       
        
        // 0 para evitar lios
        return (plugin.getManager().params.isCheckAFK()) ? timeDiff - this.timeThreshold:0;
    }

    public boolean hasLastMovement(OfflinePlayer player) {
        return this.lastMovement.containsKey(player.getUniqueId());
    }

    public long getLastMovement(OfflinePlayer player) {
        if (!hasLastMovement(player)) {
            return 0L;
        }
        return this.lastMovement.get(player.getUniqueId());
    }

    public void addLastMovement(OfflinePlayer player) {
        if (isAFK(player)) {
            TimedPlayer tPlayer = this.plugin.getTimerManager().getPlayer(player);
            tPlayer.setAFKTime(tPlayer.getAFKTime() + getAFKTime(player));
        }
        this.lastMovement.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void removeLastMovement(OfflinePlayer player) {
        this.lastMovement.remove(player.getUniqueId());
    }
}
