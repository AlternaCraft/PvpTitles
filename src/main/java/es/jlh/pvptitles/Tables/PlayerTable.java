package es.jlh.pvptitles.Tables;

import com.avaje.ebean.validation.NotNull;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author julito
 */
@SuppressWarnings("PersistenceUnitPresent")
@Entity()
@Table(name = "pt_players")
public class PlayerTable implements Serializable {

    @Column(unique = true)
    @Id
    private int id = 0;

    @NotNull
    private String playerName = "";

    @NotNull
    private int famePoints = 0;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date ultMod = null;

    public PlayerTable() {
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

    public Date getUltMod() {
        return ultMod;
    }

    public void setUltMod(Date ultMod) {
        this.ultMod = ultMod;
    }
}
