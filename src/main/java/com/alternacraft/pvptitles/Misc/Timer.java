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
package com.alternacraft.pvptitles.Misc;

import com.alternacraft.pvptitles.Main.DBLoader;
import com.alternacraft.pvptitles.Main.PvpTitles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Timer {

    private final Map<String, Long> timeAtStart = new HashMap();    
    private final Map<String, List<Long>> register = new HashMap();

    public void start(String id) {
        this.timeAtStart.put(id, System.currentTimeMillis());
    }

    public long getStartTime(String id) {
        return this.timeAtStart.get(id);
    }

    public long recordValue(String id) {
        String cid = id + " (" + DBLoader.tipo + ")";
        long finalTime = System.currentTimeMillis();

        if (!this.register.containsKey(cid)) {
            this.register.put(cid, new ArrayList());
        }
        
        long elapsedtime = finalTime - this.timeAtStart.get(id);
        this.register.get(cid).add(elapsedtime);
        return elapsedtime;
    }
    
    public void saveToLog(String filename) {
        PluginLog pl = new PluginLog(PvpTitles.getInstance(), filename);
        
        register.forEach((key, value) -> {
            int size = value.size();
            int total = 0;
            for (Long record : value) {
                total += record;
            }
            total /= size;
            pl.addMessage(key + " - " + total);
        });
        
        pl.export(true);
    }
}
