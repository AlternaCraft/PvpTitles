package es.jlh.pvptitles.Managers;

import es.jlh.pvptitles.Main.PvpTitles;
import es.jlh.pvptitles.Objects.PlayerKills;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author AlternaCraft
 */
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
        return (int) ((vetados.get(killer)+(plugin.manager.params.getTimeV()*1000L) - System.currentTimeMillis()) / 1000L);
    }
    
    public PvpTitles getPlugin() {
        return plugin;
    }
}
