package es.jlh.pvptitles.Commands;

import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.Localizer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author AlternaCraft
 */
public class PurgeCommand implements CommandExecutor {
    private final PvpTitles pvpTitles;
    private final Manager dh;

    public PurgeCommand(PvpTitles pvpTitles) {
        this.pvpTitles = pvpTitles;
        this.dh = this.pvpTitles.cm;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        LangFile.LangType messages = (sender instanceof Player) ? Localizer.getLocale((Player)sender) : Manager.messages;
        
        if (args.length > 0) {
            sender.sendMessage(PLUGIN + LangFile.COMMAND_ARGUMENTS.getText(messages));
            return false;
        }
        
        int cantidad = dh.dbh.getDm().purgeData();
        
        sender.sendMessage(PLUGIN + LangFile.PURGE_RESULT.getText(messages).
                replace("%cant%", String.valueOf(cantidad)));
        
        return true;
    }
}
