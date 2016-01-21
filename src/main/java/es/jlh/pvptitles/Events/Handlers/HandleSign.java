package es.jlh.pvptitles.Events.Handlers;

import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Managers.BoardsCustom.SignBoardData;
import static es.jlh.pvptitles.Managers.BoardsCustom.SignBoardData.EAST;
import static es.jlh.pvptitles.Managers.BoardsCustom.SignBoardData.NORTH;
import static es.jlh.pvptitles.Managers.BoardsCustom.SignBoardData.SOUTH;
import static es.jlh.pvptitles.Managers.BoardsCustom.SignBoardData.WEST;
import es.jlh.pvptitles.Managers.BoardsCustom.SignBoard;
import es.jlh.pvptitles.Misc.Localizer;
import es.jlh.pvptitles.Managers.BoardsAPI.BoardModel;
import es.jlh.pvptitles.Managers.BoardsAPI.ModelController;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

public class HandleSign implements Listener {

    private static final BlockFace[] AXIS = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    private final org.bukkit.material.Sign matSign = new org.bukkit.material.Sign(Material.WALL_SIGN);

    private PvpTitles pt = null;
    private Manager cm = null;

    public HandleSign(PvpTitles plugin) {
        this.pt = plugin;
        this.cm = plugin.cm;
    }

    @EventHandler
    public void onCreateSign(SignChangeEvent event) {
        Sign sign = (Sign) event.getBlock().getState();
        String[] lineas = event.getLines();

        Player pl = event.getPlayer();

        // Otros datos
        String nombre = "default";
        String modelo = "";
        String orientacion = "";
        String server = "";
        short blockface = 0;
        //int refresh = 5;

        if (lineas[0].contains("[PvpTitles]") || lineas[0].contains("[pvptitles]")
                || lineas[0].compareToIgnoreCase("[pvptitles]") == 0) {

            // Compruebo permisos
            if (!event.getPlayer().hasPermission("pvptitles.managesign")) {
                pl.sendMessage(PLUGIN + LangFile.COMMAND_NO_PERMISSIONS.getText(Localizer.getLocale(pl)));
                return;
            }

            BoardModel sm = cm.searchModel(lineas[1]);
            
            if (sm != null) {
                modelo = sm.getNombre();
                Location locSign = sign.getLocation();

                // Nombre
                if (lineas[2].compareTo("") != 0) {
                    nombre = lineas[2];
                }

                if (lineas[3].compareTo("") != 0) {
                    server = lineas[3];
                }

                BlockFace bf = yawToFace(pl.getLocation().getYaw());

                if (bf.equals(BlockFace.NORTH) || bf.equals(BlockFace.SOUTH)) {
                    if (bf.equals(BlockFace.SOUTH)) {
                        orientacion = "X+";
                        blockface = SOUTH;
                    } else {
                        orientacion = "X-";
                        blockface = NORTH;
                    }
                } else if (bf.equals(BlockFace.WEST)) {
                    orientacion = "Z+";
                    blockface = WEST;
                } else {
                    orientacion = "Z-";
                    blockface = EAST;
                }

                matSign.setFacingDirection(bf);

                SignBoardData data = new SignBoardData(nombre, modelo, server, locSign);
                data.setBlockface(blockface);
                data.setOrientacion(orientacion);

                ModelController mc = new ModelController();
                mc.preprocessUnit(sm.getParams());
                
                SignBoard cs = new SignBoard(data, sm, mc);
                cs.setLineas(lineas);
                cs.setMatSign(matSign);

                if (!pt.cm.getLbm().addBoard(cs, pl)) {                
                    event.setCancelled(true);
                }
            } else {
                pl.sendMessage(PLUGIN + LangFile.BOARD_MODEL_NOT_EXISTS.getText(Localizer.getLocale(pl)));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDeleteSign(BlockBreakEvent event) {
        Integer[] coords = new Integer[]{1,-1};
        
        Block b = event.getBlock();
        Location locblock = b.getLocation();
        World world = locblock.getWorld();

        // Caso especial en caso de que rompa el bloque que sostiene a un cartel
        if (world.getBlockAt(locblock).getType() != Material.WALL_SIGN) {
            for (Integer coord : coords) {
                Block b2 = new Location(b.getWorld(), b.getX() + coord, b.getY(), b.getZ()).getBlock();
                if (b2.getType() == Material.WALL_SIGN) {
                    pt.cm.getLbm().deleteBoard(b2.getLocation(), event);
                }
            }
            
            for (Integer coord : coords) {
                Block b2 = new Location(b.getWorld(), b.getX(), b.getY(), b.getZ() + coord).getBlock();
                if (b2.getType() == Material.WALL_SIGN) {
                    pt.cm.getLbm().deleteBoard(b2.getLocation(), event);
                }
            }
        } else {
            pt.cm.getLbm().deleteBoard(locblock, event);
        }
    }

    /**
     * Método que devuelve la cara del bloque horizontal según el ángulo
     *
     * @param yaw Ángulo
     * @return La cara del bloque
     */
    private BlockFace yawToFace(float yaw) {
        return AXIS[Math.round(yaw / 90f) & 0x3];
    }
}
