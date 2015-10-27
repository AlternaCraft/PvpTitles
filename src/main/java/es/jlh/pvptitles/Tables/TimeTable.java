package es.jlh.pvptitles.Tables;

import com.avaje.ebean.validation.NotNull;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author julito
 */
@SuppressWarnings("PersistenceUnitPresent")
@Entity()
@Table(name = "pt_tplayers")
public class TimeTable implements Serializable {

    @Column(unique = true)
    @Id
    private int id = 0;

    @NotNull
    private String playerName = "";    

    @NotNull
    // En segundos
    private int playedTime = 0;

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

    public int getPlayedTime() {
        return playedTime;
    }

    public void setPlayedTime(int playedTime) {
        this.playedTime = playedTime;
    }
}
