package es.jlh.pvptitles.Commands;

import es.jlh.pvptitles.Configs.LangFile;
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
public class DBCommand implements CommandExecutor {

    private PvpTitles pvpTitles = null;

    public DBCommand(PvpTitles pvpTitles) {
        this.pvpTitles = pvpTitles;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        LangFile.LangType messages = (sender instanceof Player) ? Localizer.getLocale((Player) sender) : Manager.messages;

        if (args.length == 0) {
            sender.sendMessage(PLUGIN + LangFile.COMMAND_ARGUMENTS.getText(messages));
            return false;
        }
        
        if (args[0].equals("export")) {
            pvpTitles.cm.dbh.getDm().DBExport();
        }
        else if (args[0].equals("import")) {
            if (pvpTitles.cm.dbh.getDm().DBImport()) {
                sender.sendMessage(PLUGIN + "Imported correctly");
            }
            else {
                sender.sendMessage(PLUGIN + "Filename has to be 'database'");
            }
        }

        // Exportar (Segun db)
        
        // Importar
        
        // 

        return true;
    }
}
