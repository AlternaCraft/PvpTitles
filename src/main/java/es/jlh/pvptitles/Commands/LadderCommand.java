package es.jlh.pvptitles.Commands;

import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.Localizer;
import es.jlh.pvptitles.Objects.PlayerFame;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


/**
 * 
 * @author AlternaCraft
 */
public class LadderCommand implements CommandExecutor {    
    private final PvpTitles pvpTitles;
            
    public LadderCommand(PvpTitles pvpTitles) {
        this.pvpTitles = pvpTitles;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        LangFile.LangType messages = (sender instanceof Player) ? Localizer.getLocale((Player)sender) : Manager.messages;
        
        if (args.length > 0) {            
            sender.sendMessage(PLUGIN + LangFile.COMMAND_ARGUMENTS.getText(messages));
            return false;
        }
        
        short top = this.pvpTitles.manager.params.getTop();
        
        ArrayList<PlayerFame> rankedPlayers = pvpTitles.manager.dbh.getDm().getTopPlayers(top, "");                
        
        sender.sendMessage("");
        sender.sendMessage(PLUGIN);
        sender.sendMessage(ChatColor.YELLOW + "  --------");
        sender.sendMessage(ChatColor.YELLOW + "    Top " + top + " ");
        sender.sendMessage(ChatColor.YELLOW + "  --------");

        for (int i = 0; i < rankedPlayers.size() && i < top; i++) {            
            sender.sendMessage("  " + (i+1) + ". " + rankedPlayers.get(i).toString());
        }
        
        return true;
    }
}