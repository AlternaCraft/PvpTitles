package es.jlh.pvptitles.Commands;

import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.LangDetector.Localizer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author julito
 */
public class InfoCommand implements CommandExecutor {

    private PvpTitles pvpTitles = null;
    
    public InfoCommand(PvpTitles pvpTitles) {
        this.pvpTitles = pvpTitles;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        LangFile.LangType messages = (sender instanceof Player) ? Localizer.getLocale((Player)sender) : Manager.messages;
        
        if (args.length > 0) {            
            sender.sendMessage(PLUGIN + LangFile.COMMAND_ARGUMENTS.getText(messages));
            return false;
        }

        sender.sendMessage("");
        
        sender.sendMessage(PLUGIN + ChatColor.YELLOW + "v" + pvpTitles.pdf.getVersion());
        
        sender.sendMessage("  " + ChatColor.AQUA + "/pvprank " + 
                ChatColor.RESET + " ["+LangFile.COMMAND_RANK_INFO.getText(messages)+"]");
        
        sender.sendMessage("  " + ChatColor.AQUA + "/pvpladder " + 
                ChatColor.RESET + " ["+LangFile.COMMAND_LADDER_INFO.getText(messages)+"]");
        
        if (sender.hasPermission("pvptitles.setRank")) {
            sender.sendMessage("  " + ChatColor.AQUA + "/pvpfame add|see|set <player> [<famepoints>]"
                    + ChatColor.RESET + " ["+LangFile.COMMAND_FAME_INFO.getText(messages)+"]");
        }
        
        if (sender.hasPermission("pvptitles.sign")) {
            sender.sendMessage("  " + ChatColor.AQUA + "/pvpsign"
                    + ChatColor.RESET + " ["+LangFile.COMMAND_SIGN_INFO.getText(messages)+"]");
        }
        
        if (sender.hasPermission("pvptitles.purge")) {
            sender.sendMessage("  " + ChatColor.AQUA + "/pvppurge " + 
                    ChatColor.RESET + " ["+LangFile.COMMAND_PURGE_INFO.getText(messages)+"]");
        }
        
        if (sender.hasPermission("pvptitles.reload")) {
            sender.sendMessage("  " + ChatColor.AQUA + "/pvpreload " + 
                    ChatColor.RESET + " ["+LangFile.COMMAND_RELOAD_INFO.getText(messages)+"]");
        }     
        
        sender.sendMessage("■ " + ChatColor.GOLD + "Created By Julito" + 
                ChatColor.RESET + " ■");
        
        return true;
    }
}
