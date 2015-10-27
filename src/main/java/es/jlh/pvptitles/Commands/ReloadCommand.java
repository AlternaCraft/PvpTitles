package es.jlh.pvptitles.Commands;

import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Main.Manager;
import static es.jlh.pvptitles.Main.Manager.tipo;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.LangDetector.Localizer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author julito
 */
public class ReloadCommand implements CommandExecutor {

    private final PvpTitles pvpTitles;

    public ReloadCommand(PvpTitles pvpTitles) {
        this.pvpTitles = pvpTitles;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        LangFile.LangType messages = (sender instanceof Player) ? Localizer.getLocale((Player)sender) : Manager.messages;
        
        if (args.length > 0) {
            sender.sendMessage(PLUGIN + LangFile.COMMAND_ARGUMENTS.getText(messages));
            return false;
        }

        pvpTitles.cm.loadConfigPrincipal();
        pvpTitles.cm.selectDB();
        
        pvpTitles.cm.loadLang();
        pvpTitles.cm.loadCommands();
        pvpTitles.cm.loadModels();
        
        if (tipo == Manager.DBTYPE.MYSQL) {
            pvpTitles.cm.loadServers();
        }
        
        pvpTitles.cm.loadActualizador();
        pvpTitles.cm.loadRankChecker();

        sender.sendMessage(PLUGIN + LangFile.PLUGIN_RELOAD.getText(messages));

        return true;
    }
}
