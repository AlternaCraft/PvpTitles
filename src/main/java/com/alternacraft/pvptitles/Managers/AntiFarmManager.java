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
package com.alternacraft.pvptitles.Managers;

import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Misc.PlayerKills;
import java.util.HashMap;
import java.util.Map;

public class AntiFarmManager {

    private PvpTitles plugin = null;

    // Jugador mas un arraylist con los nombres de sus victimas y sus respectivas bajas
    private final Map<String, PlayerKills> killers = new HashMap();

    // Jugadores que no conseguiran fama por abuso de kills
    private final Map<String, Long> vetados = new HashMap();

    public AntiFarmManager(PvpTitles plugin) {
        this.plugin = plugin;
    }

    public void addKiller(String name) {
        killers.put(name, new PlayerKills());
    }

    public boolean hasKiller(String name) {
        return killers.containsKey(name);
    }

    public boolean hasVictim(String killer, String victim) {
        return killers.get(killer).hasVictim(victim);
    }

    public void addKillOnVictim(String killer, String victim) {
        killers.get(killer).addVictim(victim);
    }

    public int getKillsOnVictim(String killer, String victim) {
        return killers.get(killer).getKillsOnVictim(victim);
    }

    public void cleanKillsOnVictim(String killer, String victim) {
        killers.get(killer).cleanVictim(victim);
    }

    public void cleanAllVictims(String killer) {
        killers.get(killer).cleanAll();
    }
    
    public void vetar(String killer, long time) {
        vetados.put(killer, time);
    }
    
    public boolean isVetado(String killer) {
        return vetados.containsKey(killer);
    }
    
    public void cleanVeto(String killer) {
        vetados.remove(killer);
    }
    
    public int getVetoTime(String killer) {
        return (int) ((vetados.get(killer)+(plugin.getManager().params.getTimeV()*1000L) - System.currentTimeMillis()) / 1000L);
    }
    
    public PvpTitles getPlugin() {
        return plugin;
    }
}
