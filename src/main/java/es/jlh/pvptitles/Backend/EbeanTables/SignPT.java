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
package es.jlh.pvptitles.Backend.EbeanTables;

import com.avaje.ebean.validation.NotNull;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.bukkit.Location;

@SuppressWarnings("PersistenceUnitPresent")
@Entity()
@Table(name = "pt_signs")
public class SignPT implements Serializable {

    @Id
    private int id = 0;
    
    @NotNull
    private String world = "";
    
    private int x = 0;
    private int y = 0;
    private int z = 0;

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
