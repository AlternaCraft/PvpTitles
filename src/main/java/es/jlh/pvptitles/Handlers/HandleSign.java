package es.jlh.pvptitles.Handlers;

import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Managers.SignManager;
import es.jlh.pvptitles.Misc.LangDetector.Localizer;
import es.jlh.pvptitles.Objects.LBData;
import es.jlh.pvptitles.Objects.LBModel;
import es.jlh.pvptitles.Objects.PlayerFame;
import java.util.ArrayList;
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
    private static final BlockFace[] axis = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };    
    private final org.bukkit.material.Sign matSign = new org.bukkit.material.Sign(Material.WALL_SIGN);
    
    private Manager cm;
    
    public HandleSign(PvpTitles plugin) {
        this.cm = plugin.cm;
    }
    
    @EventHandler
    public void OnCreateSign(SignChangeEvent event) {
        Sign sign = (Sign)event.getBlock().getState();
        String[] lineas = event.getLines();
        
        Player pl = event.getPlayer();   
        
        // Otros datos
        String nombre = "default";
        String modelo = "";
        String orientacion = "";
        String server = "";
        int blockface = 0;
        //int refresh = 5;
        
        boolean existe = false;

        if (lineas[0].contains("[PvpTitles]") || lineas[0].contains("[pvptitles]")
                || lineas[0].compareToIgnoreCase("[pvptitles]") == 0) {
            
            // Compruebo permisos
            if (!event.getPlayer().hasPermission("pvptitles.managesign")) {
                pl.sendMessage(PLUGIN + LangFile.COMMAND_NO_PERMISSIONS.getText(Localizer.getLocale(pl)));
                return;
            }
            
            for (int i = 0; i < cm.modelos.size(); i++) {
                LBModel sm = (LBModel)cm.modelos.get(i);                                
                
                if (lineas[1].compareToIgnoreCase(sm.getNombre()) == 0) {
                    modelo = sm.getNombre();
                    Location locSign = sign.getLocation();
                    
                    // Orientacion de la creacion de los carteles \\                    
                    boolean usaXP = false;
                    boolean usaXN = false;
                    boolean usaZP = false;
                    boolean usaZN = false;
                    
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
                            usaXP = true;
                            orientacion = "X+";
                            blockface = 2;
                        }
                        else {
                            usaXN = true;
                            orientacion = "X-";
                            blockface = 1;
                        }
                    }
                    else {
                        if (bf.equals(BlockFace.WEST)) {
                            usaZP = true;
                            orientacion = "Z+";
                            blockface = 4;
                        }
                        else {
                            usaZN = true;
                            orientacion = "Z-";
                            blockface = 3;
                        }
                    }
                    
                    matSign.setFacingDirection(bf);
                    
                    // ------------------------------------------ \\
                    
                    // Compruebo si ya hay algo ocupando el sitio            
                    ArrayList<PlayerFame> pf = this.cm.getDm().getTopPlayers(sm.getCantidad(), server);
                    int jugadores = pf.size();
                    int filas = sm.getFilas(jugadores);                    
                    int cols = sm.getCols();

                    Location thisblock = event.getBlock().getLocation();

                    Location locblock = new Location(thisblock.getWorld(), 
                            thisblock.getX(), thisblock.getY(), thisblock.getZ());
                    
                    // Comprobar por ambos lados
                    for (int j = 0; j < filas; j++) {
                        for (int k = 0; k < cols; k++) {
                            if (usaXN) {
                                locblock.setX(locblock.getX()-k);
                            }
                            else if (usaXP) {
                                locblock.setX(locblock.getX()+k);
                            }
                            else if (usaZN) {
                                locblock.setZ(locblock.getZ()-k);
                            }
                            else if (usaZP) {
                                locblock.setZ(locblock.getZ()+k);
                            }

                            if (!locblock.equals(thisblock) && !locblock.getBlock().isEmpty()) {
                                pl.sendMessage(PLUGIN + LangFile.SIGN_CANT_BE_PLACED.getText(Localizer.getLocale(pl)) + 
                                        " (" + filas + "x" + cols + ")");
                                return;
                            }
                        } 

                        locblock.setX(thisblock.getX());                            
                        locblock.setY(locblock.getY()-1);
                        locblock.setZ(thisblock.getZ());
                    }                                          
                    
                    //
                    SignManager.definirSB(cm, sm, locSign, usaXP, usaXN, usaZP, 
                            bf, matSign, lineas, sign.getLocation(), server);
                    
                    existe = true;
                    break;
                }
            }
            
            if (!existe) {
                pl.sendMessage(PLUGIN + LangFile.SIGN_MODEL_NOT_EXISTS.getText(Localizer.getLocale(pl)));
                event.setCancelled(true);
            }
            else {
                this.cm.getDm().registraCartel(nombre, modelo, server, event.getBlock().getLocation(), orientacion, blockface);
                pl.sendMessage(PLUGIN + LangFile.SIGN_CREATED_CORRECTLY.getText(Localizer.getLocale(pl)).replace("%name%", nombre));
            }
        }
    }  
    
    @EventHandler
    public void onDeleteSign(BlockBreakEvent event) {
        ArrayList<LBData> sd = cm.getDm().buscaCarteles();
        Block b = event.getBlock();
        Location locblock = b.getLocation();
        World world = locblock.getWorld();
        Player pl = event.getPlayer();        
        
        // Caso especial en caso de que rompa el bloque que sostiene a un cartel
        if (world.getBlockAt(locblock).getType() != Material.WALL_SIGN) {
            // Compruebo si el bloque adyacente es un cartel
            for (BlockFace f : BlockFace.values()) {
                if (b.getRelative(f).getType() == Material.WALL_SIGN) {
                    SignManager.borrarSBEvento(event, cm, sd, b.getRelative(f).getLocation());
                }
            }
        }
        else {
            SignManager.borrarSBEvento(event, cm, sd, locblock);            
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
