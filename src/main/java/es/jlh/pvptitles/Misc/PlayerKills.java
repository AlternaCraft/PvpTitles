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
package es.jlh.pvptitles.Misc;

import java.util.HashMap;
import java.util.Map;

public class PlayerKills {
    private final Map<String, Integer> victims = new HashMap();

    public PlayerKills() {
    }

    public boolean hasVictim(String victim) {
        return victims.containsKey(victim);
    }
    
    public void addVictim(String victim) {
        int kills = (victims.containsKey(victim) ? victims.get(victim)+1 : 1);
        victims.put(victim, kills);
    }
    
    public int getKillsOnVictim(String victim) {
        return victims.get(victim);
    }
    
    public void cleanVictim(String victim) {
        if (victims.containsKey(victim))
            victims.remove(victim);
    }
    
    public void cleanAll() {
        victims.clear();
    }
}
