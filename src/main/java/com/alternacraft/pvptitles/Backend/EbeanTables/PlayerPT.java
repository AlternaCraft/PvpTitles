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
package com.alternacraft.pvptitles.Backend.EbeanTables;

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
    
    @Override
    public String toString() {
        return this.playerUUID + " ||| " + this.points;
    }
}
