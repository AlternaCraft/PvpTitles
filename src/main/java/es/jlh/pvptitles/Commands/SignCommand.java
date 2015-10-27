package es.jlh.pvptitles.Commands;

import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Managers.InventoryManager;
import es.jlh.pvptitles.Misc.LangDetector.Localizer;
import es.jlh.pvptitles.Objects.LBData;
import java.util.ArrayList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 
 * @author julito
 */
public class SignCommand implements CommandExecutor {    
    private final Manager dm;
    private PvpTitles pt;
            
    public SignCommand(PvpTitles pvpTitles) {
        this.pt = pvpTitles;
        this.dm = pvpTitles.cm;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        LangFile.LangType messages = (sender instanceof Player) ? Localizer.getLocale((Player)sender) : Manager.messages;
        
        if (args.length > 0) {
            sender.sendMessage(PLUGIN + LangFile.COMMAND_ARGUMENTS.getText(messages));
            return false;
        }
        
        // Esto no puede ser ejecutado por la consola
        if (!(sender instanceof Player)) {
            sender.sendMessage(PLUGIN + LangFile.COMMAND_FORBIDDEN.getText(messages));
            return true;
        }

        Player pl = (Player)sender;
        ArrayList<LBData> sd = dm.getDm().buscaCarteles();
        
        pl.openInventory(InventoryManager.createInventory(sd, Localizer.getLocale(pl)));
        
        return true;
    }
}