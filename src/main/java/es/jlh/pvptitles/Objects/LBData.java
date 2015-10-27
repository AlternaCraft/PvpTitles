package es.jlh.pvptitles.Objects;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

/**
 *
 * @author julito
 */
public class LBData {
    private static final String XP = "X+";
    private static final String XN = "X-";
    private static final String ZP = "Z+";
    private static final String ZN = "Z-";
    
    private static final int NORTH = 1;
    private static final int SOUTH = 2;
    private static final int EAST = 3;
    private static final int WEST = 4;
    
    private String nombre = null;
    private String modelo = null;
    private String server = null;
    
    // Orientacion
    private boolean xp = false;
    private boolean xn = false;
    private boolean zp = false;
    private boolean zn = false;
    
    // BlockFace
    private int blockface = 0;
    
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

    public void setBlockface(int blockface) {
        this.blockface = blockface;
    }
    
    @Override
    public String toString() {
        return this.nombre;
    }
}
