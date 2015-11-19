package es.jlh.pvptitles.Backend.EbeanTables;

import com.avaje.ebean.validation.NotNull;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.bukkit.Location;

/**
 *
 * @author Julian
 */
@SuppressWarnings("PersistenceUnitPresent")
@Entity()
@Table(name = "pt_signs")
public class SignPT implements Serializable {

    @Id
    private int id = 0;
    
    @NotNull
    public String world = "";
    
    public int x = 0;
    public int y = 0;
    public int z = 0;

    @NotNull
    private String name = "";

    @NotNull
    private String model = "";

    @NotNull
    private String orientation = "";

    @NotNull
    private short blockface = 0;

    public SignPT() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
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

    public void setLocation(Location l) {
        setWorld(l.getWorld().getName());
        setX(l.getBlockX());
        setY(l.getBlockY());
        setZ(l.getBlockZ());
    }

    public short getBlockface() {
        return blockface;
    }

    public void setBlockface(short blockface) {
        this.blockface = blockface;
    }

    @Override
    public String toString() {
        return this.x + ", " + this.y + ", " + this.z + "; " + this.world;
    }
}
