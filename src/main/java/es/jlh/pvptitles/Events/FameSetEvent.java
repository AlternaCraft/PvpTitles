package es.jlh.pvptitles.Events;

import org.bukkit.OfflinePlayer;

/**
 *
 * @author AlternaCraft
 */
public class FameSetEvent extends FameEvent {

    public FameSetEvent(OfflinePlayer player, int fameA, int fameTotal) {
        super(player, fameA, fameTotal);
    }
    
    @Override
    public int getFameTotal() {
        return this.getFameIncr();
    }
}
