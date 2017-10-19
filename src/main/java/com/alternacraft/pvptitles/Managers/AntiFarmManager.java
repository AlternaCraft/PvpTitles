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

import com.alternacraft.pvptitles.Files.LangsFile;
import com.alternacraft.pvptitles.Main.Manager;
import static com.alternacraft.pvptitles.Main.Manager.TICKS;
import com.alternacraft.pvptitles.Main.PvpTitles;
import static com.alternacraft.pvptitles.Main.PvpTitles.getPluginName;
import com.alternacraft.pvptitles.Misc.Localizer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class AntiFarmManager {

    // Jugador mas un arraylist con los nombres de sus victimas y sus respectivas bajas
    private final Map<String, PlayerKills> killers = new HashMap();

    // Jugadores que no conseguiran fama por abuso de kills
    private final Map<String, Map<String, Long>> vetoed = new HashMap();

    // Tarea de limpieza de bajas
    private final Map<String, KillsTask> cleaner = new HashMap();

    //<editor-fold defaultstate="collapsed" desc="KILLS">
    public void addKiller(String uuid) {
        this.killers.put(uuid, new PlayerKills());
        this.cleaner.put(uuid, new KillsTask(uuid, this));
    }

    public void addKillOnVictim(String killeruuid, String victimuuid) {
        killers.get(killeruuid).addVictim(victimuuid);
        if (cleaner.get(killeruuid).hasVictim(victimuuid)) {
            cleaner.get(killeruuid).cleanVictim(victimuuid);
        }
        cleaner.get(killeruuid).addVictim(victimuuid); // Create task
    }

    public void cleanKillsOnVictim(String killeruuid, String victimuuid) {
        killers.get(killeruuid).cleanVictim(victimuuid);
        if (cleaner.get(killeruuid).hasVictim(victimuuid)) {
            cleaner.get(killeruuid).cleanVictim(victimuuid);
        }
    }

    public void cleanAllVictims(String killeruuid) {
        killers.get(killeruuid).cleanAll();
        cleaner.get(killeruuid).cleanAll(); // Remove tasks
    }
    
    public boolean hasKiller(String uuid) {
        return killers.containsKey(uuid);
    }

    public boolean hasVictim(String killeruuid, String victimuuid) {
        return killers.get(killeruuid).hasVictim(victimuuid);
    }
    
    public int getKillsOnVictim(String killeruuid, String victimuuid) {
        return killers.get(killeruuid).getKillsOnVictim(victimuuid);
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="VETO">
    public void veto(String killeruuid, String victimuuid, long time) {
        if (!this.vetoed.containsKey(killeruuid)) {
            this.vetoed.put(killeruuid, new HashMap<>());
        }
        vetoed.get(killeruuid).put(victimuuid, time);

        Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(Manager.getInstance().getPvpTitles(), () -> {
            this.cleanKillsOnVictim(killeruuid, victimuuid); // Reset kills and timer

            OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(killeruuid));
            if (Manager.getInstance().params.isPreventFromEvery()) {
                this.cleanVeto(killeruuid);
                if (op.isOnline()) {
                    op.getPlayer().sendMessage(getPluginName() + LangsFile.VETO_FINISHED
                            .getText(Localizer.getLocale(op.getPlayer())));
                }
            } else {
                this.cleanVetoOn(killeruuid, victimuuid);
                if (op.isOnline()) {
                    op.getPlayer().sendMessage(getPluginName() + LangsFile.VETOED_BY_FINISHED
                        .getText(Localizer.getLocale(op.getPlayer()))
                        .replace("%player%", Bukkit.getOfflinePlayer(UUID
                                .fromString(victimuuid)).getName())
                    );
                }                
            }
        }, Manager.getInstance().params.getVetoTime() * TICKS);
    }
    
    public void cleanVeto(String killeruuid) {
        vetoed.remove(killeruuid);
    }

    public void cleanVetoOn(String killeruuid, String victimuuid) {
        vetoed.get(killeruuid).remove(victimuuid);
    }
    
    public boolean isVetoed(String killeruuid) {
        return vetoed.containsKey(killeruuid);
    }

    public boolean isVetoedFor(String killeruuid, String victimuuid) {
        return vetoed.containsKey(killeruuid) && vetoed.get(killeruuid).containsKey(victimuuid);
    }
    
    public int getVetoTime(String killeruuid) {
        int time = Manager.getInstance().params.getVetoTime();
        return (int) ((vetoed.get(killeruuid).values().iterator().next() 
                + (time * 1000L) - System.currentTimeMillis()) / 1000L);
    }

    public int getVetoTimeOn(String killeruuid, String victimuuid) {
        int time = Manager.getInstance().params.getVetoTime();
        return (int) ((vetoed.get(killeruuid).get(victimuuid) 
                + (time * 1000L) - System.currentTimeMillis()) / 1000L);
    }
    
    public Map<String, Long> getVetoes(String killeruuid) {
        return (this.vetoed.containsKey(killeruuid)) 
                ? this.vetoed.get(killeruuid) : new HashMap<>();
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="PLAYERKILLS">    
    class PlayerKills {

        private final Map<String, Integer> victims = new HashMap();

        public boolean hasVictim(String victim) {
            return victims.containsKey(victim);
        }

        public void addVictim(String victim) {
            int kills = (victims.containsKey(victim) ? victims.get(victim) + 1 : 1);
            victims.put(victim, kills);
        }

        public int getKillsOnVictim(String victim) {
            return victims.get(victim);
        }

        public void cleanVictim(String victim) {
            if (victims.containsKey(victim)) {
                victims.remove(victim);
            }
        }

        public void cleanAll() {
            victims.clear();
        }
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="KILLSTASK">    
    class KillsTask {

        private final String killer;
        private final AntiFarmManager afm;

        // Victim + task
        private final Map<String, Integer> victims = new HashMap();

        public KillsTask(String killer, AntiFarmManager afm) {
            this.killer = killer;
            this.afm = afm;
        }

        public void addVictim(final String victim) {
            victims.put(victim, PvpTitles.getInstance().getServer().getScheduler().
                    scheduleSyncDelayedTask(Manager.getInstance().getPvpTitles(), () -> {
                        afm.cleanKillsOnVictim(killer, victim);
                        victims.remove(victim);
                    }, Manager.getInstance().getPvpTitles().getManager()
                            .params.getCleanerTime() * Manager.TICKS * 1L)
            );
        }

        public boolean hasVictim(String victim) {
            return this.victims.containsKey(victim);
        }

        public void cleanVictim(String victim) {
            int task = victims.get(victim);
            Bukkit.getServer().getScheduler().cancelTask(task);
            victims.remove(victim);
        }

        public void cleanAll() {
            victims.entrySet()
                    .stream()
                    .map(Map.Entry::getKey)
                    .forEach(this::cleanVictim);
        }
    }
    //</editor-fold>
}
