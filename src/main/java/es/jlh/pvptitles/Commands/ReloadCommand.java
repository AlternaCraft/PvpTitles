package es.jlh.pvptitles.Commands;

import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Integrations.HolographicSetup;
import es.jlh.pvptitles.Main.Handlers.DBHandler.DBTYPE;
import static es.jlh.pvptitles.Main.Handlers.DBHandler.tipo;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.Localizer;
import es.jlh.pvptitles.RetroCP.DBChecker;
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

        pvpTitles.cm.getCh().loadConfig(pvpTitles.cm.params);
        pvpTitles.cm.getDbh().selectDB();
        new DBChecker(pvpTitles).setup();

        pvpTitles.cm.loadLang();
        pvpTitles.cm.loadModels();
        pvpTitles.cm.loadSavedBoards();
        pvpTitles.cm.loadCommands();

        if (tipo == DBTYPE.MYSQL) {
            pvpTitles.cm.loadServers();
        }

        pvpTitles.cm.loadActualizador();
        pvpTitles.cm.loadRankTimeChecker();
        
        if (HolographicSetup.isHDEnable && pvpTitles.cm.params.displayLikeHolo()) {
            HolographicSetup.RANK_LINE = pvpTitles.cm.params.getHolotagformat();
            HolographicSetup.loadPlayersInServer();
        }
        else if (HolographicSetup.isHDEnable && HolographicSetup.HOLOPLAYERS.size() > 0) {
            /*
             * En caso de hacer un pvpreload habiendo desactivado los hologramas en
             * el config, borro los que haya en el server creados anteriormente.
             */
            HolographicSetup.deleteHoloPlayers();         
        }

        sender.sendMessage(PLUGIN + LangFile.PLUGIN_RELOAD.getText(messages));

        return true;
    }
}
