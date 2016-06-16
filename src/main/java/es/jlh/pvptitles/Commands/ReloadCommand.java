package es.jlh.pvptitles.Commands;

import es.jlh.pvptitles.Backend.MySQLConnection;
import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Hook.HolographicHook;
import static es.jlh.pvptitles.Hook.HolographicHook.DEFAULT_TITLE_HEIGHT;
import static es.jlh.pvptitles.Hook.HolographicHook.HEIGHT_PER_ROW;
import es.jlh.pvptitles.Main.Handlers.DBHandler;
import es.jlh.pvptitles.Main.Handlers.DBHandler.DBTYPE;
import static es.jlh.pvptitles.Main.Handlers.DBHandler.tipo;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.Localizer;
import es.jlh.pvptitles.RetroCP.DBChecker;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
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

        pvpTitles.manager.getCh().loadConfig(pvpTitles.manager.params);
        
        if (DBHandler.tipo.equals(DBHandler.DBTYPE.MYSQL)) try {
            MySQLConnection.closeConnection();
        } catch (SQLException ex) {
        }
        
        pvpTitles.manager.getDbh().selectDB();
        new DBChecker(pvpTitles).setup();

        pvpTitles.manager.loadLang();
        pvpTitles.manager.loadModels();
        pvpTitles.manager.loadSavedBoards();
        pvpTitles.manager.loadCommands();

        if (tipo == DBTYPE.MYSQL) {
            pvpTitles.manager.loadServers();
        }

        pvpTitles.manager.loadActualizador();
        pvpTitles.manager.loadRankTimeChecker();
        
        if (HolographicHook.ISHDENABLED && pvpTitles.manager.params.displayLikeHolo()) {
            HolographicHook.RANK_LINE = pvpTitles.manager.params.getHolotagformat();
            HolographicHook.TITLE_HEIGHT = ((pvpTitles.manager.params.getHoloHeightMod() - 1) * HEIGHT_PER_ROW) + DEFAULT_TITLE_HEIGHT;
            HolographicHook.loadPlayersInServer();
        }
        else if (HolographicHook.ISHDENABLED && HolographicHook.HOLOPLAYERS.size() > 0) {
            /*
             * En caso de hacer un pvpreload habiendo desactivado los hologramas en
             * el config, borro los que haya en el server creados anteriormente.
             */
            HolographicHook.deleteHoloPlayers();         
        }

        sender.sendMessage(PLUGIN + LangFile.PLUGIN_RELOAD.getText(messages));

        return true;
    }
}
