/*
 * Copyright (C) 2018 AlternaCraft
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
package com.alternacraft.pvptitles.Managers;

import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Misc.TimedPlayer;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MovementManager {

    private PvpTitles plugin = null;
    private Map<UUID, Long> lastMovement = null;
    private int timeThreshold = 0;

    public MovementManager(PvpTitles plugin) {
        this.plugin = plugin;
        this.lastMovement = new HashMap();
    }

    public boolean isAFK(OfflinePlayer player) {
        if (!hasLastMovement(player)) {
            return false;
        }
        return getAFKTime(player) > 0; // Devuelve '0' si el comprobador esta desactivado
    }

    /**
     * Tiempo desde su ultimo movimiento hasta ahora menos el tiempo mínimo para
     * que sea considerado AFK
     *
     * @param player OfflinePlayer
     * @return Time
     */
    public int getAFKTime(OfflinePlayer player) {
        long lastMove = this.lastMovement.get(player.getUniqueId());
        long currTime = System.currentTimeMillis();
        int timeDiff = (int) ((currTime - lastMove) / 1000L);

        // 0 para evitar lios
        return (plugin.getManager().params.isCheckAFK()) ? timeDiff - this.timeThreshold : 0;
    }

    public boolean hasLastMovement(OfflinePlayer player) {
        return this.lastMovement.containsKey(player.getUniqueId());
    }

    /**
     * Última vez que se movió
     *
     * @param player OfflinePlayer
     * @return Time in millis
     */
    public long getLastMovement(OfflinePlayer player) {
        if (!hasLastMovement(player)) {
            return 0L;
        }
        return this.lastMovement.get(player.getUniqueId());
    }

    /**
     * Cuando le jugador se mueve registro la hora del sistema
     *
     * @param player OfflinePlayer
     */
    public void addLastMovement(OfflinePlayer player) {
        if (isAFK(player)) {
            TimedPlayer tPlayer = this.plugin.getManager().getTimerManager().getPlayer(player);
            tPlayer.setAFKTime(tPlayer.getAFKTime() + getAFKTime(player));
        }
        this.lastMovement.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void removeLastMovement(OfflinePlayer player) {
        this.lastMovement.remove(player.getUniqueId());
    }

    public void updateTimeAFK() {
        this.timeThreshold = plugin.getManager().params.getAFKTime() * 60;
    }
}
