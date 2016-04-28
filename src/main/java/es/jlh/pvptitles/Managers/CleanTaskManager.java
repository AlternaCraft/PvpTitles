package es.jlh.pvptitles.Managers;

import es.jlh.pvptitles.Main.PvpTitles;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author AlternaCraft
 */
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
                }, afm.getPlugin().manager.params.getTimeL() * TICKS * 1L)
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
