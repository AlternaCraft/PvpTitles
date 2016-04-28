package es.jlh.pvptitles.Managers;

import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.showMessage;
import static es.jlh.pvptitles.Main.PvpTitles.logMessage;
import java.io.File;
import java.util.regex.Pattern;
import net.gravitydevelopment.updater.Updater;
import net.gravitydevelopment.updater.Updater.UpdateType;
import net.gravitydevelopment.updater.Updater.UpdateResult;
import org.bukkit.ChatColor;

/**
 *
 * @author AlternaCraft
 */
public class UpdaterManager {

    public void testUpdate(final PvpTitles plugin, File file) {
        boolean shouldupdate = plugin.manager.params.isUpdate();
        boolean shouldalert = plugin.manager.params.isAlert();

        if (!shouldupdate && !shouldalert) { // Optimizacion
            return;
        }

        UpdateType ut = (shouldupdate) ? UpdateType.DEFAULT : UpdateType.NO_DOWNLOAD;
        
        Updater updater = new Updater(plugin, 89518, file, ut, shouldalert) {
            @Override
            public boolean shouldUpdate(String localVersion, String remoteVersion) {
                String[] longL = localVersion.split(Pattern.quote("."));
                String[] longR = remoteVersion.split(Pattern.quote("."));

                int longitud = (longL.length <= longR.length) ? longL.length : longR.length;

                for (int i = 0; i < longitud; i++) {
                    if (Integer.valueOf(longL[i]) < Integer.valueOf(longR[i])) {
                        return true;
                    }
                    else if (Integer.valueOf(longL[i]) > Integer.valueOf(longR[i])) {
                        return false;
                    }            
                }

                // Si sale quiere decir que, en la longitud que recorre el bucle, son iguales
                return longR.length > longL.length;
            }
        };

        if (shouldalert) {
            UpdateResult result = updater.getResult();
            switch (result) {
                case SUCCESS:
                    showMessage(ChatColor.YELLOW + "Update finished. Do reload to load it.");
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
