package es.jlh.pvptitles.Objects;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author julito
 */
public class PlayerKills {
    private Map<String, Integer> victims = new HashMap();

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
        victims = new HashMap();
    }
}
