/*
 * Copyright (C) 2017 AlternaCraft
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
package com.alternacraft.pvptitles.Managers;

import com.alternacraft.pvptitles.Exceptions.RanksException;
import com.alternacraft.pvptitles.Hooks.VaultHook;
import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Misc.Rank;
import com.alternacraft.pvptitles.Misc.Rank.NextRank;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author AlternaCraft
 */
public class RankManager {

    // Ordenado de mas a menos
    static class RankComparator implements Comparator<Rank> {

        @Override
        public int compare(Rank o1, Rank o2) {
            return o2.getPoints() - o1.getPoints();
        }
    }

    private static final List<Rank> RANKS = new LinkedList() {
        @Override
        public boolean contains(Object o2) {
            for (Object o1 : this) {
                Rank r = (Rank) o1;
                Rank r2 = (Rank) o2;
                if (r.similar(r2)) {
                    return true;
                }
            }
            return false;
        }
    };

    public static void addRank(Rank rank) {
        if (RANKS.contains(rank)) {
            CustomLogger.logError("Rank ID " + rank.getId() + " is repeated!!");
        } else {
            RANKS.add(rank);
            Collections.sort(RANKS, new RankComparator());
        }
    }

    public static Rank getRank(String id) {
        for (Iterator<Rank> iterator = RANKS.iterator(); iterator.hasNext();) {
            Rank next = iterator.next();
            if (next.getId().equals(id)) {
                return next;
            }
        }
        return null;
    }

    public static Rank getRank(int points, long seconds, OfflinePlayer pl) throws RanksException {
        Rank r = null;

        for (Iterator<Rank> iterator = RANKS.iterator(); iterator.hasNext();) {
            Rank next = iterator.next();
            // Has enough points, has enough played time and has permission
            if (next.getPoints() <= points) {
                while (iterator.hasNext() && (next.getTime() > seconds
                        || (next.isRestricted() && pl != null && pl.isOnline()
                        && !hasRankPermission(pl.getPlayer(),
                                next.getDefaultPermission())))) {
                    next = iterator.next();
                }
                r = next;
                break;
            }
        }

        if (r == null) {
            Map<String, Object> data = new LinkedHashMap<>();

            data.put("Fame", points);
            data.put("Seconds", seconds);
            data.put("Available ranks", RANKS.size());

            throw new RanksException("Error getting rank", data);
        }

        return r;
    }

    public static NextRank getNextRank(Rank r, Player pl) {
        int rank_pos = Collections.binarySearch(RANKS, r, new RankComparator());
        if (rank_pos < 1 || rank_pos >= RANKS.size() - 1) {
            return null;
        }

        Rank nextRank = null;
        do {
            nextRank = RANKS.get(--rank_pos);
        } while (nextRank.isRestricted()
                && !hasRankPermission(pl, nextRank.getDefaultPermission())
                && rank_pos > 0);

        return new NextRank(r, nextRank);
    }

    public static void clear() {
        RANKS.clear();
    }

    private static boolean hasRankPermission(Player pl, String perm) {
        return VaultHook.hasPermission(perm, pl.getPlayer());
    }

    public static boolean isTimeReqUsed() {
        for (Iterator<Rank> iterator = RANKS.iterator(); iterator.hasNext();) {
            Rank next = iterator.next();
            if (next.hasTimeRequirement()) {
                return true;
            }
        }
        return false;
    }
}