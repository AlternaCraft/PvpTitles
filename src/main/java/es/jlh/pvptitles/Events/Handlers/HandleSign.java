package es.jlh.pvptitles.Events.Handlers;

import es.jlh.pvptitles.Configs.LangFile;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Objects.LBSigns.CustomSign;
import es.jlh.pvptitles.Misc.Localizer;
import es.jlh.pvptitles.Objects.LBSigns.LBData;
import static es.jlh.pvptitles.Objects.LBSigns.LBData.EAST;
import static es.jlh.pvptitles.Objects.LBSigns.LBData.NORTH;
import static es.jlh.pvptitles.Objects.LBSigns.LBData.SOUTH;
import static es.jlh.pvptitles.Objects.LBSigns.LBData.WEST;
import es.jlh.pvptitles.Objects.LBSigns.LBModel;
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

    private static final BlockFace[] axis = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    private final org.bukkit.material.Sign matSign = new org.bukkit.material.Sign(Material.WALL_SIGN);

    private PvpTitles pt = null;
    private Manager cm = null;

    public HandleSign(PvpTitles plugin) {
        this.pt = plugin;
        this.cm = plugin.cm;
    }

    @EventHandler
    public void OnCreateSign(SignChangeEvent event) {
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

            LBModel sm = cm.searchModel(lineas[1]);
            
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

                LBData data = new LBData(nombre, modelo, server, locSign);
                data.setBlockface(blockface);
                data.setOrientacion(orientacion);

                CustomSign cs = new CustomSign(data, sm);
                cs.setLineas(lineas);
                cs.setMatSign(matSign);

                if (pt.cm.getLbm().addSign(cs, pl)) {
                    pl.sendMessage(PLUGIN + LangFile.SIGN_CREATED_CORRECTLY.
                            getText(Localizer.getLocale(pl)).replace("%name%", nombre));
                }
                else {
                    event.setCancelled(true);
                }
            } else {
                pl.sendMessage(PLUGIN + LangFile.SIGN_MODEL_NOT_EXISTS.getText(Localizer.getLocale(pl)));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDeleteSign(BlockBreakEvent event) {
        Block b = event.getBlock();
        Location locblock = b.getLocation();
        World world = locblock.getWorld();

        // Caso especial en caso de que rompa el bloque que sostiene a un cartel
        if (world.getBlockAt(locblock).getType() != Material.WALL_SIGN) {
            // Compruebo si el bloque adyacente es un cartel
            for (BlockFace f : BlockFace.values()) {
                if (b.getRelative(f).getType() == Material.WALL_SIGN) {
                    pt.cm.getLbm().deleteSign(locblock, event);
                }
            }
        } else {
            pt.cm.getLbm().deleteSign(locblock, event);
        }
    }

    /**
     * Método que devuelve la cara del bloque horizontal según el ángulo
     *
     * @param yaw Ángulo
     * @return La cara del bloque
     */
    private BlockFace yawToFace(float yaw) {
        return axis[Math.round(yaw / 90f) & 0x3];
    }
}
