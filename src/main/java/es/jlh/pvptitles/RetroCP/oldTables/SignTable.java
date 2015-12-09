package es.jlh.pvptitles.RetroCP.oldTables;

import com.avaje.ebean.validation.NotNull;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.bukkit.Location;

/**
 *
 * @author AlternaCraft
 */
@SuppressWarnings("PersistenceUnitPresent")
@Entity()
@Table(name = "pt_signs")
public class SignTable implements Serializable {

    @Column(unique = true)
    @Id
    private int id = 0;

    @NotNull
    private String nombre = "";

    @NotNull
    private String modelo = "";

    @NotNull
    private String orientacion = "";

    @NotNull
    private String world = "";

    @NotNull
    private int x = 0;

    @NotNull
    private int y = 0;

    @NotNull
    private int z = 0;

    @NotNull
    private int blockface = 0;

    public SignTable() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getOrientacion() {
        return orientacion;
    }

    public void setOrientacion(String orientacion) {
        this.orientacion = orientacion;
    }

    public void setLocation(Location l) {
        setWorld(l.getWorld().getName());
        setX(l.getBlockX());
        setY(l.getBlockY());
        setZ(l.getBlockZ());
    }

    public int getBlockface() {
        return blockface;
    }

    public void setBlockface(int blockface) {
        this.blockface = blockface;
    }

    @Override
    public String toString() {
        return this.x + ", " + this.y + ", " + this.z + "; " + this.world;
    }
}
