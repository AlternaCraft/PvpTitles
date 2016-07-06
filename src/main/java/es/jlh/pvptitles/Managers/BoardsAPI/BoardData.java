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
package es.jlh.pvptitles.Managers.BoardsAPI;

import es.jlh.pvptitles.Misc.CustomLocation;
import org.bukkit.Location;

public class BoardData {

    protected String nombre = null;
    protected String modelo = null;
    protected String server = "";
    
    protected Location l = null;
    protected Location cl = null;

    public BoardData(Location l) {
        this.l = l;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setL(Location l) {
        this.l = l;
    }

    public void setCl(Location cl) {
        this.cl = cl;
    }

    public String getNombre() {
        return nombre;
    }

    public String getModelo() {
        return modelo;
    }

    public String getServer() {
        return server;
    }

    public Location getFullLocation() {
        return this.l;
    }
    
    public Location getLocation() {
        return CustomLocation.toCustomLocation(l);
    }

    public Location getCustomL() {
        return cl;
    }
    
    @Override
    public String toString() {
        return "AbstractBoardData{" + "nombre=" + nombre + '}';
    }

}
