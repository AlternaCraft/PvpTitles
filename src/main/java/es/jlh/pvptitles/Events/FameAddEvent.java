package es.jlh.pvptitles.Events;

import org.bukkit.OfflinePlayer;

/**
 *
 * @author AlternaCraft
 */
public class FameAddEvent extends FameEvent {

    public FameAddEvent(OfflinePlayer player, int fame, int fameIncr) {
        super(player, fame, fameIncr);
    }
}
