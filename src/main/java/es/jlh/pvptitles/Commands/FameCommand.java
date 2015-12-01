package es.jlh.pvptitles.Commands;

import es.jlh.pvptitles.Events.FameAddEvent;
import es.jlh.pvptitles.Events.FameSetEvent;
import es.jlh.pvptitles.Configs.LangFile;
import es.jlh.pvptitles.Configs.LangFile.LangType;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.Localizer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author AlternaCraft
 */
public class FameCommand implements CommandExecutor {

    private PvpTitles pt = null;
    private Manager dm = null;

    public FameCommand(PvpTitles pt) {
        this.pt = pt;
        this.dm = pt.cm;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        LangType messages = (sender instanceof Player) ? Localizer.getLocale((Player) sender) : Manager.messages;

        if (args.length == 0 || args.length > 3) {
            sender.sendMessage(PLUGIN + LangFile.COMMAND_ARGUMENTS.getText(messages));
            return false;
        }

        return this.modRank(sender, args, messages);
    }

    /**
     * Método para aniadir/ver/modificar los puntos de fama de un jugador
     *
     * @param sender Jugador que ejecuta el comando
     * @param args Parametros del comando
     * @return Booleano con el resultado de la ejecucion
     */
    private boolean modRank(CommandSender sender, String[] args, LangType messages) {
        if (args.length <= 1) {
            return false;
        }

        OfflinePlayer opl = Bukkit.getServer().getOfflinePlayer(args[1]);
        
        // Evitar NullPointerException
        if (opl == null) {
            sender.sendMessage(PLUGIN + ChatColor.RED + args[1] + " doesn't "
                    + "exist");
            return true;
        }

        // Evitar number exception
        if (!args[0].equalsIgnoreCase("see") && args.length > 2) {
            try {
                Integer.valueOf(args[2]);
            } catch (NumberFormatException ex) {
                return false;
            }
        }

        if (args[0].equalsIgnoreCase("add")) {
            if (args.length == 3) {
                int fameA = this.dm.dbh.getDm().loadPlayerFame(opl.getUniqueId(), null);
                int fameIncr = Integer.valueOf(args[2]);

                FameAddEvent event = new FameAddEvent(opl, fameA, fameIncr);

                Bukkit.getServer().getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    this.dm.dbh.getDm().savePlayerFame(opl.getUniqueId(), event.getFameTotal());
                    sender.sendMessage(PLUGIN + LangFile.FAME_ADD.getText(messages).
                            replace("%tag%", this.dm.params.getTag())
                    );
                } else {
                    sender.sendMessage(PLUGIN + LangFile.FAME_MODIFY_ERROR.getText(messages).
                            replace("%player%", args[1]).
                            replace("%tag%", this.dm.params.getTag())
                    );
                }
            } else {
                sender.sendMessage(PLUGIN + ChatColor.RED + "Syntax: 'pvpfame add <player> <famepoints>'");
            }
            return true;
        } else if (args[0].equalsIgnoreCase("see")) {
            if (args.length == 2 || args.length == 3) {
                int fameTotal = 0;

                if (dm.params.isMw_enabled()) {
                    if (args.length == 2) {
                        String world = pt.getServer().getWorlds().get(0).getName();
                        
                        if (sender instanceof Player) {
                            Player pl = (Player) sender;
                            world = pl.getWorld().getName();
                        }
                        
                        fameTotal = this.dm.dbh.getDm().loadPlayerFame(opl.getUniqueId(), world);
                    }
                    else {
                        fameTotal = this.dm.dbh.getDm().loadPlayerFame(opl.getUniqueId(), args[2]);
                    }
                } else {
                    fameTotal = this.dm.dbh.getDm().loadPlayerFame(opl.getUniqueId(), null);
                }

                sender.sendMessage(PLUGIN + LangFile.FAME_SEE.getText(messages).
                        replace("%player%", args[1]).
                        replace("%fame%", String.valueOf(fameTotal)).
                        replace("%tag%", this.dm.params.getTag())
                );
            } else {
                sender.sendMessage(PLUGIN + ChatColor.RED + "Syntax: 'pvpfame see <player> [<world_name>]'");
            }
            return true;
        } else if (args[0].equalsIgnoreCase("set")) {
            if (args.length == 3) {
                int fame = this.dm.dbh.getDm().loadPlayerFame(opl.getUniqueId(), null);
                int fameTotal = Integer.valueOf(args[2]);

                fameTotal = (fameTotal < 0) ? 0 : fameTotal;

                FameSetEvent event = new FameSetEvent(opl, fame, fameTotal);

                Bukkit.getServer().getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    this.dm.dbh.getDm().savePlayerFame(opl.getUniqueId(), event.getFameTotal());
                    sender.sendMessage(PLUGIN + LangFile.FAME_SET.getText(messages).
                            replace("%tag%", this.dm.params.getTag()));
                } else {
                    sender.sendMessage(PLUGIN + LangFile.FAME_MODIFY_ERROR.getText(messages).
                            replace("%player%", args[1]).
                            replace("%tag%", this.dm.params.getTag())
                    );
                }
            } else {
                sender.sendMessage(PLUGIN + ChatColor.RED + "Syntax: 'pvpfame set <player> <famepoints>'");
            }
            return true;
        }

        return false;
    }
}
