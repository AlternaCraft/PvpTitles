package es.jlh.pvptitles.Commands;

import es.jlh.pvptitles.Configs.LangFile;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.Inventories;
import es.jlh.pvptitles.Objects.LBSigns.CustomSign;
import es.jlh.pvptitles.Misc.Localizer;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 
 * @author AlternaCraft
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
        List<CustomSign> css = pt.cm.getLbm().getSigns();
        
        pl.openInventory(Inventories.createInventory(css, Localizer.getLocale(pl)).get(0));
        
        return true;
    }
}