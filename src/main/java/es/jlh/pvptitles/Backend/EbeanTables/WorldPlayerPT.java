package es.jlh.pvptitles.Backend.EbeanTables;

import com.avaje.ebean.validation.NotNull;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author AlternaCraft
 */
@SuppressWarnings("PersistenceUnitPresent")
@Entity
@Table(name = "pt_wPlayers")
public class WorldPlayerPT implements Serializable {

    @Id
    private int id = 0;
    
    @NotNull
    String playerUUID = null;
    @NotNull
    String world = null;
    
    @NotNull
    private int points = 0;

    public WorldPlayerPT() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(String playerUUID) {
        this.playerUUID = playerUUID;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
