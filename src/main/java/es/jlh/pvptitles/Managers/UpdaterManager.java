package es.jlh.pvptitles.Managers;

import es.jlh.pvptitles.Libraries.Updater;
import es.jlh.pvptitles.Libraries.Updater.UpdateResult;
import es.jlh.pvptitles.Libraries.Updater.UpdateType;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.showMessage;
import static es.jlh.pvptitles.Main.PvpTitles.logMessage;
import java.io.File;
import org.bukkit.ChatColor;

/**
 *
 * @author AlternaCraft
 */
public class UpdaterManager {

    public void testUpdate(final PvpTitles plugin, File file) {
        boolean shouldupdate = plugin.cm.params.isUpdate();
        boolean shouldalert = plugin.cm.params.isAlert();

        if (!shouldupdate && !shouldalert) { // Optimizacion
            return;
        }

        UpdateType ut = (shouldupdate) ? UpdateType.DEFAULT : UpdateType.NO_DOWNLOAD;
        Updater updater = new Updater(plugin, 89518, file, ut, shouldalert);

        if (shouldalert) {
            UpdateResult result = updater.getResult();
            switch (result) {
                case SUCCESS:
                    // Success: The updater found an update, and has readied it to be loaded the next time the server restarts/reloads
                    break;
                case NO_UPDATE:
                    logMessage("No update was found.");
                    break;
                case DISABLED:
                    // Won't Update: The updater was disabled in its configuration file.
                    break;
                case FAIL_DOWNLOAD:
                    // Download Failed: The updater found an update, but was unable to download it.
                    break;
                case FAIL_DBO:
                    // dev.bukkit.org Failed: For some reason, the updater was unable to contact DBO to download the file.
                    break;
                case FAIL_NOVERSION:
                    // No version found: When running the version check, the file on DBO did not contain the a version in the format 'vVersion' such as 'v1.0'.
                    break;
                case FAIL_BADID:
                    // Bad id: The id provided by the plugin running the updater was invalid and doesn't exist on DBO.
                    break;
                case FAIL_APIKEY:
                    // Bad API key: The user provided an invalid API key for the updater to use.
                    break;
                case UPDATE_AVAILABLE:
                    showMessage(ChatColor.YELLOW + "A new update has been found: "
                            + ChatColor.GREEN + updater.getLatestName());
                    showMessage(ChatColor.YELLOW + "http://dev.bukkit.org/bukkit-plugins/pvptitles");
            }
        }
    }
}
