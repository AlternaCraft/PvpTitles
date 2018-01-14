/*
 * Copyright (C) 2018 AlternaCraft
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
package com.alternacraft.pvptitles.Misc;

import org.bukkit.Location;
import org.bukkit.World;

public class CustomLocation extends Location {

    public CustomLocation(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Location other = (Location) obj;
        if ((this.getWorld() != other.getWorld()) && ((this.getWorld() == null) || (!this.getWorld().equals(other.getWorld())))) {
            return false;
        }
        if (Double.doubleToLongBits(this.getX()) != Double.doubleToLongBits(other.getX())) {
            return false;
        }
        if (Double.doubleToLongBits(this.getY()) != Double.doubleToLongBits(other.getY())) {
            return false;
        }
        return Double.doubleToLongBits(this.getZ()) == Double.doubleToLongBits(other.getZ());
    }

    @Override
    public int hashCode() {
        int hash = 3;

        hash = 19 * hash + (this.getWorld() != null ? this.getWorld().hashCode() : 0);
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.getX()) ^ Double.doubleToLongBits(this.getX()) >>> 32);
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.getY()) ^ Double.doubleToLongBits(this.getY()) >>> 32);
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.getZ()) ^ Double.doubleToLongBits(this.getZ()) >>> 32);
        
        return hash;
    }
    
    public static CustomLocation toCustomLocation(Location l) {
        return new CustomLocation(l.getWorld(), l.getX(), l.getY(), l.getZ());
    }
    
    @Override
    public String toString() {
        return "[" + this.getWorld().getName() + ", " + this.getBlockX() + ", " 
                + this.getBlockY() + ", " + this.getBlockZ() + "]";
    }
}
