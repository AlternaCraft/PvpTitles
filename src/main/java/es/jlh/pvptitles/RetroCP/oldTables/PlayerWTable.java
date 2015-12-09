package es.jlh.pvptitles.RetroCP.oldTables;

import com.avaje.ebean.validation.NotNull;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author AlternaCraft
 */
@SuppressWarnings("PersistenceUnitPresent")
@Entity()
@Table(name = "pt_wPlayers")
public class PlayerWTable implements Serializable {

    @Column(unique = true)
    @Id
    private int id = 0;

    @NotNull
    private String playerName = null;
    
    @NotNull
    private int famePoints = 0;
    
    @NotNull
    private String world = null;

    public PlayerWTable() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getFamePoints() {
        return famePoints;
    }

    public void setFamePoints(int famePoints) {
        this.famePoints = famePoints;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }
}
