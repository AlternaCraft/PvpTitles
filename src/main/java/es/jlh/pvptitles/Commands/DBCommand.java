package es.jlh.pvptitles.Commands;

import es.jlh.pvptitles.Backend.DatabaseManager;
import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Main.Handlers.DBHandler;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.Localizer;
import org.bukkit.ChatColor;
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

        DatabaseManager dm = pvpTitles.cm.getDbh().getDm();

        String filename;

        switch (args[0]) {
            case "export":
                if (args.length > 1) {
                    filename = args[1] + ((DBHandler.tipo == DBHandler.DBTYPE.EBEAN) ? ".sql" : ".json");
                } else {
                    filename = pvpTitles.cm.dbh.getDm().getDefaultFExport();
                }
                dm.DBExport(filename);
                sender.sendMessage(PLUGIN + ChatColor.YELLOW + "Exported correctly");
                break;
            case "import":
                if (args.length > 1) {
                    filename = args[1] + ((DBHandler.tipo == DBHandler.DBTYPE.EBEAN) ? ".json" : ".sql");
                } else {
                    filename = pvpTitles.cm.dbh.getDm().getDefaultFImport();
                }
                if (dm.DBImport(filename)) {
                    sender.sendMessage(PLUGIN + ChatColor.YELLOW + "Imported correctly");
                } else {
                    sender.sendMessage(PLUGIN + ChatColor.RED + "File '"
                            + filename + "' not found...");
                }
                break;
            default:
                return false;
        }

        return true;
    }
}
