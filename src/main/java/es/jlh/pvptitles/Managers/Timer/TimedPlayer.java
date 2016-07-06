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
package es.jlh.pvptitles.Managers.Timer;

import es.jlh.pvptitles.Main.PvpTitles;
import es.jlh.pvptitles.Managers.MovementManager;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.OfflinePlayer;

public class TimedPlayer {

    private PvpTitles plugin = null;
    private UUID uuid = null;
    private Set<Session> sessions = null;
    private Session activeSession = null;
    private int afkTime = 0;

    public TimedPlayer(PvpTitles plugin, OfflinePlayer player) {
        this(plugin, player.getUniqueId());
    }

    public TimedPlayer(PvpTitles plugin, UUID uuid) {
        this.plugin = plugin;
        this.uuid = uuid;
        this.sessions = new HashSet();
    }

    public boolean startSession() {
        Session session = new Session(System.currentTimeMillis());
        setActiveSession(session);
        return addSession(session);
    }

    public boolean addSession(Session session) {
        return this.sessions.add(session);
    }

    public boolean stopSession() {
        if (!hasSession()) {
            return false;
        }
        MovementManager movementManager = this.plugin.getMovementManager();
        OfflinePlayer player = this.plugin.getServer().getOfflinePlayer(getUniqueId());
        
        Session session = getSession();
        
        if (movementManager.isAFK(player)) {
            setAFKTime(getAFKTime() + movementManager.getAFKTime(player));
        }
        
        session.setStopTime(System.currentTimeMillis() - getAFKTime() * 1000L);
        setActiveSession(null);
        setAFKTime(0);
        return true;
    }

    public int getTotalOnline() {
        OfflinePlayer player = this.plugin.getServer().getOfflinePlayer(getUniqueId());
        
        int timeOnline = 0;
        for (Session s : this.sessions) {   
            if (s.equals(getSession())) {
                timeOnline = (int) (timeOnline + (System.currentTimeMillis() - s.getStartTime()) / 1000L);
                timeOnline -= getAFKTime();
                
                if (this.plugin.getMovementManager().isAFK(player)) {
                    timeOnline -= this.plugin.getMovementManager().getAFKTime(player);
                }
            } else {
                timeOnline += (int) ((s.getStopTime() - s.getStartTime()) / 1000L);
            }
        }

        return timeOnline;
    }

    public OfflinePlayer getOfflinePlayer() {
        return this.plugin.getServer().getOfflinePlayer(getUniqueId());
    }

    public boolean hasSession() {
        return getSession() != null;
    }

    public boolean isCurrentlyAFK() {
        OfflinePlayer player = this.plugin.getServer().getOfflinePlayer(getUniqueId());
        return this.plugin.getMovementManager().isAFK(player);
    }

    public Session getSession() {
        return activeSession;
    }

    public void setActiveSession(Session activeSession) {
        this.activeSession = activeSession;
    }

    public boolean removeSession(Session session) {
        return this.sessions.remove(session);
    }

    public void removeSessions() {
        this.sessions.clear();
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public void setUniqueId(UUID uuid) {
        this.uuid = uuid;
    }

    public Set<Session> getSessions() {
        return sessions;
    }

    public void setSessions(Set<Session> sessions) {
        this.sessions = sessions;
    }

    public int getAFKTime() {
        return afkTime;
    }

    public void setAFKTime(int afkTime) {
        this.afkTime = afkTime;
    }
}
