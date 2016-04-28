package es.jlh.pvptitles.Commands;

import es.jlh.pvptitles.Files.HologramsFile;
import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Integrations.HolographicSetup;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Managers.BoardsCustom.HologramBoard;
import es.jlh.pvptitles.Managers.BoardsAPI.Board;
import es.jlh.pvptitles.Managers.BoardsAPI.BoardData;
import es.jlh.pvptitles.Managers.BoardsAPI.BoardModel;
import es.jlh.pvptitles.Managers.BoardsAPI.ModelController;
import static es.jlh.pvptitles.Managers.BoardsCustom.HologramBoard.DEFAULT_POSITION;
import es.jlh.pvptitles.Misc.Inventories;
import es.jlh.pvptitles.Misc.Localizer;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author AlternaCraft
 */
public class BoardCommand implements CommandExecutor {

    enum SUPPORTED_BOARDS {
        HOLOGRAM
    }

    private final Manager cm;
    private final PvpTitles pt;

    public BoardCommand(PvpTitles pvpTitles) {
        this.pt = pvpTitles;
        this.cm = pvpTitles.manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        LangFile.LangType messages = (sender instanceof Player) ? Localizer.getLocale((Player) sender) : Manager.messages;

        // Esto no puede ser ejecutado por la consola
        if (!(sender instanceof Player)) {
            sender.sendMessage(PLUGIN + LangFile.COMMAND_FORBIDDEN.getText(messages));
            return true;
        }

        Player pl = (Player) sender;

        if (args.length >= 1) {
            if (args.length == 1 || (!args[0].equals("create") && !args[0].equals("remove"))) {
                return false;
            }
            
            if (args[1].equalsIgnoreCase(SUPPORTED_BOARDS.HOLOGRAM.name())
                    && !HolographicSetup.isHDEnable) {
                pl.sendMessage(PLUGIN + ChatColor.RED + "HolographicDisplays is not enabled");
                return true;
            }

            if (!args[1].equalsIgnoreCase(SUPPORTED_BOARDS.HOLOGRAM.name())) {
                pl.sendMessage(PLUGIN + ChatColor.RED + "Supported types: "
                        + SUPPORTED_BOARDS.HOLOGRAM.name().toLowerCase());
                return true;
            }

            switch (args[0]) {
                case "create":
                    if (args.length >= 4) {
                        String name = args[2];
                        String model = args[3];
                        String filter = (args.length >= 5) ? args[4] : "";
                        create(name, model, filter, pl);
                    } else {
                        pl.sendMessage(PLUGIN + ChatColor.RED
                                + "Syntax: 'pvpboard create <board_type> <name> <board_model> [<server_name>]'");
                    }
                    break;
                case "remove":
                    if (args.length >= 3) {
                        BoardData bd = HologramsFile.loadHologram(args[2]);

                        if (bd == null) {
                            pl.sendMessage(PLUGIN + LangFile.BOARD_NAME_NOT_EXISTS.getText(Localizer.getLocale(pl)));
                        } else {
                            cm.getLbm().deleteBoard(bd.getLocation(), pl);
                        }
                    } else {
                        pl.sendMessage(PLUGIN + ChatColor.RED
                                + "Syntax: 'pvpboard remove <board_type> <name>'");
                    }
                    break;
                default:
                    return false;
            }
        } else {
            List<Board> boards = pt.manager.getLbm().getBoards();
            pl.openInventory(Inventories.createInventory(boards, Localizer.getLocale(pl)).get(0));
        }

        return true;
    }

    private void create(String name, String model, String filter, Player pl) {
        BoardData bda = HologramsFile.loadHologram(name);
        if (bda != null) {
            pl.sendMessage(PLUGIN + LangFile.BOARD_NAME_ALREADY_EXISTS.getText(Localizer.getLocale(pl)));
            return;
        }

        BoardModel bm = cm.searchModel(model);
        if (bm == null) {
            pl.sendMessage(PLUGIN + LangFile.BOARD_MODEL_NOT_EXISTS.getText(Localizer.getLocale(pl)));
            return;
        }

        Location ploc = pl.getLocation();
        Location l = new Location(ploc.getWorld(), ploc.getX(), ploc.getY(), ploc.getZ());
        l.add(0.0, DEFAULT_POSITION, 0.0);

        BoardData bd = new BoardData(l);
        bd.setNombre(name);
        bd.setModelo(model);
        bd.setServer(filter);

        ModelController mc = new ModelController();
        mc.preprocessUnit(bm.getParams());

        Board b = new HologramBoard(bd, bm, mc);

        cm.getLbm().addBoard(b, pl);
    }
}
