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
package com.alternacraft.pvptitles.Misc;

import com.alternacraft.pvptitles.Backend.EbeanTables.PlayerPT;
import com.alternacraft.pvptitles.Backend.EbeanTables.SignPT;
import com.alternacraft.pvptitles.Backend.EbeanTables.WorldPlayerPT;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TagsClass {

    public enum Player {

        uuid,
        points,
        playedTime,
        lastJoin;
    }

    public enum Sign {

        name,
        model,
        location,
        orientation,
        blockface;

        public enum Location {

            x,
            y,
            z,
            world;
        }
    }

    public enum PlayerWorld {

        uuid,
        points,
        world;
    }

    public static Object createPlayer(PlayerPT next) {
        JSONObject player = new JSONObject();
        String fecha = new java.sql.Date(next.getLastLogin().getTime()).toString();

        player.put(Player.uuid.toString(), next.getPlayerUUID());
        player.put(Player.points.toString(), next.getPoints());
        player.put(Player.playedTime.toString(), next.getPlayedTime());
        player.put(Player.lastJoin.toString(), fecha);

        return player;
    }

    public static List<PlayerPT> getPlayers(JSONArray players) {
        List<PlayerPT> playersT = new ArrayList();

        // Check if not exists
        if (players == null)
            return playersT;
        
        Iterator<JSONObject> eachPlayer = players.iterator();
        while (eachPlayer.hasNext()) {
            JSONObject js = eachPlayer.next();
            PlayerPT pt = new PlayerPT();

            pt.setPlayerUUID((String) js.get(Player.uuid.toString()));
            pt.setPoints(((Long) js.get(Player.points.toString())).intValue());
            pt.setPlayedTime(((Long) js.get(Player.playedTime.toString())).intValue());
            pt.setLastLogin(getDate((String) js.get(Player.lastJoin.toString())));

            playersT.add(pt);
        }

        return playersT;
    }

    private static Date getDate(String date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date newDate = new Date();

        try {
            newDate = formatter.parse(date);
        } catch (java.text.ParseException ex) {
        }

        return newDate;
    }

    public static Object createSign(SignPT next) {
        JSONObject sign = new JSONObject();

        sign.put(Sign.name.toString(), next.getName());
        sign.put(Sign.model.toString(), next.getModel());
        sign.put(Sign.orientation.toString(), next.getOrientation());
        sign.put(Sign.blockface.toString(), next.getBlockface());

        JSONObject loc = new JSONObject();

        loc.put(Sign.Location.x.toString(), next.getX());
        loc.put(Sign.Location.y.toString(), next.getY());
        loc.put(Sign.Location.z.toString(), next.getZ());
        loc.put(Sign.Location.world.toString(), next.getWorld());

        sign.put(Sign.location.toString(), loc);

        return sign;
    }

    public static List<SignPT> getSigns(JSONArray signs) {
        List<SignPT> signsT = new ArrayList();

        // Check if not exists
        if (signs == null)
            return signsT;
        
        Iterator<JSONObject> eachSign = signs.iterator();
        while (eachSign.hasNext()) {
            JSONObject js = eachSign.next();
            SignPT st = new SignPT();

            st.setName((String) js.get(Sign.name.toString()));
            st.setModel((String) js.get(Sign.model.toString()));
            st.setOrientation((String) js.get(Sign.orientation.toString()));
            st.setBlockface(((Long) js.get(Sign.blockface.toString())).shortValue());

            JSONObject loc = (JSONObject) js.get(Sign.location.toString());

            st.setWorld((String) loc.get(Sign.Location.world.toString()));
            st.setX(((Long) loc.get(Sign.Location.x.toString())).intValue());
            st.setY(((Long) loc.get(Sign.Location.y.toString())).intValue());
            st.setZ(((Long) loc.get(Sign.Location.z.toString())).intValue());

            signsT.add(st);
        }

        return signsT;
    }

    public static Object createPlayerW(WorldPlayerPT next) {
        JSONObject pworld = new JSONObject();

        pworld.put(PlayerWorld.uuid.toString(), next.getPlayerUUID());
        pworld.put(PlayerWorld.points.toString(), next.getPoints());
        pworld.put(PlayerWorld.world.toString(), next.getWorld());

        return pworld;
    }

    public static List<WorldPlayerPT> getPWorlds(JSONArray pworlds) {
        List<WorldPlayerPT> pworldT = new ArrayList();

        // Check if not exists
        if (pworlds == null) 
            return pworldT;
        
        Iterator<JSONObject> eachPWorld = pworlds.iterator();
        while (eachPWorld.hasNext()) {
            JSONObject js = eachPWorld.next();
            WorldPlayerPT pwt = new WorldPlayerPT();

            pwt.setPlayerUUID((String) js.get(PlayerWorld.uuid.toString()));
            pwt.setPoints(((Long) js.get(PlayerWorld.points.toString())).intValue());
            pwt.setWorld((String) js.get(PlayerWorld.world.toString()));

            pworldT.add(pwt);
        }

        return pworldT;
    }
}
