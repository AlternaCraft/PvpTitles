package es.jlh.pvptitles.Managers;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;

/**
 *
 * @author julito
 */
public class CleanTaskManager {

    /**
     * contante TICKS para saber el tiempo en segundos
     */
    public static final int TICKS = 20;

    private String killer = null;
    private AntiFarmManager afm = null;  
    
    private final Map<String, Integer> cleanKills = new HashMap();

    public CleanTaskManager(AntiFarmManager afm, String killer) {
        this.afm = afm;
        this.killer = killer;
    }

    public void addVictim(final String victim) {
        cleanKills.put(victim, Bukkit.getServer().getScheduler().
                scheduleSyncDelayedTask(afm.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        afm.cleanKillsOnVictim(killer, victim);
                        cleanKills.remove(victim);
                    }
                }, afm.getPlugin().cm.params.getTimeL() * TICKS)
        );
    }

    public void cleanVictim(String victim) {
        int task = cleanKills.get(victim);
        Bukkit.getServer().getScheduler().cancelTask(task);
        cleanKills.remove(victim);
    }
    
    public void cleanAll() {
        for (Map.Entry<String, Integer> entrySet : cleanKills.entrySet()) {
            String victim = entrySet.getKey();
            cleanVictim(victim);
        }
    }
}
