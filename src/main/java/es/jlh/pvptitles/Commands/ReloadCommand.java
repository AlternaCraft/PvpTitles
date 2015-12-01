package es.jlh.pvptitles.Commands;

import es.jlh.pvptitles.Configs.LangFile;
import es.jlh.pvptitles.Main.Handlers.DBHandler.DBTYPE;
import static es.jlh.pvptitles.Main.Handlers.DBHandler.tipo;
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
public class ReloadCommand implements CommandExecutor {

    private final PvpTitles pvpTitles;

    public ReloadCommand(PvpTitles pvpTitles) {
        this.pvpTitles = pvpTitles;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        LangFile.LangType messages = (sender instanceof Player) ? Localizer.getLocale((Player) sender) : Manager.messages;

        if (args.length > 0) {
            sender.sendMessage(PLUGIN + LangFile.COMMAND_ARGUMENTS.getText(messages));
            return false;
        }

        pvpTitles.cm.getCh().setup(pvpTitles.cm.params);
        pvpTitles.cm.getDbh().selectDB();
        pvpTitles.cm.getDbh().autoExportData();

        pvpTitles.cm.loadLang();
        pvpTitles.cm.loadCommands();
        pvpTitles.cm.loadModels();

        if (tipo == DBTYPE.MYSQL) {
            pvpTitles.cm.loadServers();
        }

        pvpTitles.cm.loadActualizador();
        pvpTitles.cm.loadRankChecker();

        sender.sendMessage(PLUGIN + LangFile.PLUGIN_RELOAD.getText(messages));

        return true;
    }
}
