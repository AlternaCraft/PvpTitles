package es.jlh.pvptitles.Managers.BoardsAPI;

import es.jlh.pvptitles.Objects.CustomLocation;
import org.bukkit.Location;

/**
 *
 * @author AlternaCraft
 */
public class BoardData {

    protected String nombre = null;
    protected String modelo = null;
    protected String server = "";
    
    protected Location l = null;
    protected Location cl = null;

    public BoardData(Location l) {
        this.l = l;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setL(Location l) {
        this.l = l;
    }

    public void setCl(Location cl) {
        this.cl = cl;
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

    public Location getFullLocation() {
        return this.l;
    }
    
    public Location getLocation() {
        return CustomLocation.toCustomLocation(l);
    }

    public Location getCustomL() {
        return cl;
    }
    
    @Override
    public String toString() {
        return "AbstractBoardData{" + "nombre=" + nombre + '}';
    }

}
