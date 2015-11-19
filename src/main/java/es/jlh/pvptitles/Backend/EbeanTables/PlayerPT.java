package es.jlh.pvptitles.Backend.EbeanTables;

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
public class PlayerPT implements Serializable {
    
    @Column(unique = true)
    @Id
    private String playerUUID = null;

    private int points = 0;

    private int playedTime = 0;
    
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date lastLogin = null;

    public String getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(String playerUUID) {
        this.playerUUID = playerUUID;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPlayedTime() {
        return playedTime;
    }

    public void setPlayedTime(int playedTime) {
        this.playedTime = playedTime;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }
}
