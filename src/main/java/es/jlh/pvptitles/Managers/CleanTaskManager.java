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
import java.util.HashMap;
import java.util.Map;

public class CleanTaskManager {

    /**
     * contante TICKS para saber el tiempo en segundos
     */
    public static final long TICKS = 20L;

    private String killer = null;
    private AntiFarmManager afm = null;  
    
    private final Map<String, Integer> cleanKills = new HashMap();

    public CleanTaskManager(AntiFarmManager afm, String killer) {
        this.afm = afm;
        this.killer = killer;
    }

    public void addVictim(final String victim) {
        cleanKills.put(victim, PvpTitles.getInstance().getServer().getScheduler().
                scheduleSyncDelayedTask(afm.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        afm.cleanKillsOnVictim(killer, victim);
                        cleanKills.remove(victim);
                    }
                }, afm.getPlugin().getManager().params.getTimeL() * TICKS * 1L)
        );
    }

    public void cleanVictim(String victim) {
        int task = cleanKills.get(victim);
        PvpTitles.getInstance().getServer().getScheduler().cancelTask(task);
        cleanKills.remove(victim);
    }
    
    public void cleanAll() {
        for (Map.Entry<String, Integer> entrySet : cleanKills.entrySet()) {
            String victim = entrySet.getKey();
            cleanVictim(victim);
        }
    }
}
