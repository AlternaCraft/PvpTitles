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

public class Session {

    private long startTime;
    private long stopTime;

    public Session(long startTime) {
        this(startTime, 0L);
    }

    public Session(long startTime, long stopTime) {
        this.startTime = startTime;
        this.stopTime = stopTime;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getStopTime() {
        return this.stopTime;
    }

    public void setStartTime(long time) {
        this.startTime = time;
    }

    public void setStopTime(long time) {
        this.stopTime = time;
    }
}
