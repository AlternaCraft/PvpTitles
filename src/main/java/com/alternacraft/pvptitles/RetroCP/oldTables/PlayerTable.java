/*
 * Copyright (C) 2016 AlternaCraft
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.alternacraft.pvptitles.RetroCP.oldTables;

import com.avaje.ebean.validation.NotNull;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;

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
