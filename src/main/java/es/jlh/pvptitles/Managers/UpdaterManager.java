package es.jlh.pvptitles.Managers;

import es.jlh.pvptitles.Libraries.Updater;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.showMessage;
import java.io.File;
import org.bukkit.ChatColor;

/**
 *
 * @author AlternaCraft
 */
public class UpdaterManager {

    public void testUpdate(final PvpTitles plugin, File file) {
        if (plugin.cm.params.isUpdate()) {
            Updater updater = new Updater(plugin, 89518, file, Updater.UpdateType.DEFAULT, plugin.cm.params.isAlert());
        } else if (plugin.cm.params.isAlert()) {
            Updater updater = new Updater(plugin, 89518, file, Updater.UpdateType.NO_DOWNLOAD, true);
            if (updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE) {
                showMessage(ChatColor.YELLOW + "A new update has been found: "
                        + ChatColor.GREEN + updater.getLatestName());
                showMessage(ChatColor.YELLOW + "http://dev.bukkit.org/bukkit-plugins/pvptitles");
            }
        }
    }
}
