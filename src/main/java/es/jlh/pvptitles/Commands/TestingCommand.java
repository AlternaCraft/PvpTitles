package es.jlh.pvptitles.Commands;

import es.jlh.pvptitles.Main.PvpTitles;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author AlternaCraft
 */
public class TestingCommand implements CommandExecutor {

    private PvpTitles pvpTitles = null;

    public TestingCommand(PvpTitles pvpTitles) {
        this.pvpTitles = pvpTitles;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        sender.sendMessage(ChatColor.YELLOW + "TESTING MESSAGE");
        sender.sendMessage("Values (T & M): " + (pvpTitles.getTimerManager() == null) + "; " + (pvpTitles.getMovementManager() == null));    
        
        return true;
    }
}
