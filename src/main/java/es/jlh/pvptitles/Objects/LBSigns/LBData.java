package es.jlh.pvptitles.Objects.LBSigns;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Sign;

/**
 *
 * @author AlternaCraft
 */
public class LBData {
    private final Sign matSign = new Sign(Material.WALL_SIGN);
    
    private static final String XP = "X+";
    private static final String XN = "X-";
    private static final String ZP = "Z+";
    private static final String ZN = "Z-";
    
    public static final short NORTH = 1;
    public static final short SOUTH = 2;
    public static final short EAST = 3;
    public static final short WEST = 4;
    
    private String nombre = null;
    private String modelo = null;
    private String server = null;
    
    // Orientacion
    private boolean xp = false;
    private boolean xn = false;
    private boolean zp = false;
    private boolean zn = false;
    
    // BlockFace
    private short blockface = 0;
    
    private Location l = null;

    public LBData(String nombre, String modelo, String server, Location l) {
        this.nombre = nombre;
        this.modelo = modelo;
        this.server = server;
        this.l = l;
    }

    public String getNombre() {
        return nombre;
    }

    public String getModelo() {
        return modelo;
    }

    public String getServer() {
        return server;
    }

    public Location getL() {
        return l;
    }
    
    public void setOrientacion(String o) {
        switch(o) {
            case XP:
                xp = true;
                break;
            case XN:
                xn = true;
                break;
            case ZP:
                zp = true;
                break;
            case ZN:
                zn = true;
        }
    }
    
    public String getOrientacion() {
        if (xp) {
            return XP;
        }
        else if (xn) {
            return XN;
        }
        else if (zp) {
            return ZP;
        }
        else if (zn) {
            return ZN;
        }
        
        return null;
    }

    public boolean isXp() {
        return xp;
    }

    public boolean isXn() {
        return xn;
    }

    public boolean isZp() {
        return zp;
    }

    public boolean isZn() {
        return zn;
    }
    
    public Sign getSignMaterial() {        
        return this.matSign;
    }

    public BlockFace getBlockface() {
        switch (this.blockface) {
            case NORTH: 
                return BlockFace.NORTH;
            case SOUTH:
                return BlockFace.SOUTH;
            case EAST:
                return BlockFace.EAST;
            case WEST:
                return BlockFace.WEST;
            default:
                return null;
        }
    }

    public short getPrimitiveBlockface() {
        return this.blockface;
    }
    
    public void setBlockface(short blockface) {
        this.blockface = blockface;
        this.matSign.setFacingDirection(this.getBlockface());
    }
    
    @Override
    public String toString() {
        return this.nombre;
    }
}
