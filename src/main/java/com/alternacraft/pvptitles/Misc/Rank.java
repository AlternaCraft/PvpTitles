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

/**
 *
 * @author AlternaCraft
 */
public final class Rank {

    private final String id;
    private final int points;
    
    // Optional
    private String display;
    private long time;
    private boolean restricted;

    public Rank(String id, int points) {
        this.id = id;
        this.points = points;
    }

    public Rank(String id, int points, String display, long time, boolean restricted) {
        this(id, points);
        setDisplay(display);
        setTime(time);
        setRestricted(restricted);
    }

    public String getId() {
        return id;
    }

    public int getPoints() {
        return points;
    }

    public String getDisplay() {
        return StrUtils.translateColors(display);
    }

    public void setDisplay(String display) {
        if (display == null) {
            display = this.id;
        }
        this.display = display;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

    public String getDefaultPermission() {
        return "pvptitles.rank." + this.id;
    }

    public boolean similar(Rank r) {
        return (r instanceof Rank && r.getId().equals(this.getId()));
    }

    public boolean hasTimeRequirement() {
        return this.time > 0;
    }

    public static class NextRank {

        private final int actual_points;
        private final long actual_time;
        private final Rank next;

        public NextRank(int actual_points, long actual_time, Rank next) {
            this.actual_points = actual_points;
            this.actual_time = actual_time;
            this.next = next;
        }

        public int fameToRankUp() {
            return next.getPoints() - actual_points;
        }

        public long timeToRankUp() {
            return next.getTime() - actual_time;
        }

        public String nextRankTitle() {
            return next.getDisplay();
        }

        public Rank getNext() {
            return next;
        }
    }
}
