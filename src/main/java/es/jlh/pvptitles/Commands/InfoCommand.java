package es.jlh.pvptitles.Commands;

import es.jlh.pvptitles.Files.LangFile;
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
public class InfoCommand implements CommandExecutor {

    private PvpTitles pvpTitles = null;

    public InfoCommand(PvpTitles pvpTitles) {
        this.pvpTitles = pvpTitles;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        LangFile.LangType messages = (sender instanceof Player) ? Localizer.getLocale((Player) sender) : Manager.messages;

        if (args.length > 0) {
            sender.sendMessage(PLUGIN + LangFile.COMMAND_ARGUMENTS.getText(messages));
            return false;
        }

        String pvprank = pvpTitles.getDescription().getCommands().get("pvpRank")
                .get("usage").toString().replace("<command>", "pvprank");

        String pvpladder = pvpTitles.getDescription().getCommands().get("pvpLadder")
                .get("usage").toString().replace("<command>", "pvpladder");

        String pvpfame = pvpTitles.getDescription().getCommands().get("pvpFame")
                .get("usage").toString().replace("<command>", "pvpfame");

        String pvpBoard = pvpTitles.getDescription().getCommands().get("pvpBoard")
                .get("usage").toString().replace("<command>", "pvpboard");

        String pvppurge = pvpTitles.getDescription().getCommands().get("pvpPurge")
                .get("usage").toString().replace("<command>", "pvppurge");

        String pvpreload = pvpTitles.getDescription().getCommands().get("pvpReload")
                .get("usage").toString().replace("<command>", "pvpreload");
        
        String pvpdb = pvpTitles.getDescription().getCommands().get("pvpDatabase")
                .get("usage").toString().replace("<command>", "pvpdatabase");

        sender.sendMessage("");

        sender.sendMessage(PLUGIN + ChatColor.YELLOW + "v" + pvpTitles.getDescription().getVersion());

        sender.sendMessage("  " + ChatColor.AQUA + pvprank
                + ChatColor.RESET + " [" + LangFile.COMMAND_RANK_INFO.getText(messages) + "]");

        sender.sendMessage("  " + ChatColor.AQUA + pvpladder
                + ChatColor.RESET + " [" + LangFile.COMMAND_LADDER_INFO.getText(messages) + "]");

        String setrankperm = pvpTitles.getDescription().getCommands().get("pvpfame").get("permission").toString();
        if (sender.hasPermission(setrankperm)) {
            sender.sendMessage("  " + ChatColor.AQUA + pvpfame
                    + ChatColor.RESET + " [" + LangFile.COMMAND_FAME_INFO.getText(messages) + "]");
        }

        String boardperm = pvpTitles.getDescription().getCommands().get("pvpboard").get("permission").toString();
        if (sender.hasPermission(boardperm)) {
            sender.sendMessage("  " + ChatColor.AQUA + pvpBoard
                    + ChatColor.RESET + " [" + LangFile.COMMAND_BOARD_INFO.getText(messages) + "]");
        }

        String purgeperm = pvpTitles.getDescription().getCommands().get("pvppurge").get("permission").toString();
        if (sender.hasPermission(purgeperm)) {
            sender.sendMessage("  " + ChatColor.AQUA + pvppurge
                    + ChatColor.RESET + " [" + LangFile.COMMAND_PURGE_INFO.getText(messages) + "]");
        }

        String reloadperm = pvpTitles.getDescription().getCommands().get("pvpreload").get("permission").toString();
        if (sender.hasPermission(reloadperm)) {
            sender.sendMessage("  " + ChatColor.AQUA + pvpreload
                    + ChatColor.RESET + " [" + LangFile.COMMAND_RELOAD_INFO.getText(messages) + "]");
        }
        
        String dbperm = pvpTitles.getDescription().getCommands().get("pvpdatabase").get("permission").toString();
        if (sender.hasPermission(dbperm)) {
            sender.sendMessage("  " + ChatColor.AQUA + pvpdb
                    + ChatColor.RESET + " [" + LangFile.COMMAND_DATABASE_INFO.getText(messages) + "]");
        }

        sender.sendMessage("■ " + ChatColor.GOLD + "Created By "
                + pvpTitles.getDescription().getAuthors().get(0)
                + ChatColor.RESET + " ■");

        return true;
    }
}
