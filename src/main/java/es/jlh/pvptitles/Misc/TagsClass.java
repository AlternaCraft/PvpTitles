package es.jlh.pvptitles.Misc;

import es.jlh.pvptitles.Tables.PlayerTable;
import es.jlh.pvptitles.Tables.PlayerWTable;
import es.jlh.pvptitles.Tables.SignTable;
import es.jlh.pvptitles.Tables.TimeTable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author julito
 */
public class TagsClass {

    public enum Player {

        uuid,
        points,
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

    public enum PlayerTime {

        playerUUID,
        playedTime;
    }

    public static Object createPlayer(PlayerTable next) {
        JSONObject player = new JSONObject();
        String fecha = new java.sql.Date(next.getUltMod().getTime()).toString();

        player.put(Player.uuid.toString(), next.getPlayerName());
        player.put(Player.points.toString(), next.getFamePoints());
        player.put(Player.lastJoin.toString(), fecha);

        return player;
    }

    public static List<PlayerTable> getPlayers(JSONArray players) {
        List<PlayerTable> playersT = new ArrayList();

        // Check if not exists
        if (players == null)
            return playersT;
        
        Iterator<JSONObject> eachPlayer = players.iterator();
        while (eachPlayer.hasNext()) {
            JSONObject js = eachPlayer.next();
            PlayerTable pt = new PlayerTable();

            pt.setPlayerName((String) js.get(Player.uuid.toString()));
            pt.setFamePoints(((Long) js.get(Player.points.toString())).intValue());
            pt.setUltMod(getDate((String) js.get(Player.lastJoin.toString())));

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

    public static Object createSign(SignTable next) {
        JSONObject sign = new JSONObject();

        sign.put(Sign.name.toString(), next.getNombre());
        sign.put(Sign.model.toString(), next.getModelo());
        sign.put(Sign.orientation.toString(), next.getOrientacion());
        sign.put(Sign.blockface.toString(), next.getBlockface());

        JSONObject loc = new JSONObject();

        loc.put(Sign.Location.x.toString(), next.getX());
        loc.put(Sign.Location.y.toString(), next.getY());
        loc.put(Sign.Location.z.toString(), next.getZ());
        loc.put(Sign.Location.world.toString(), next.getWorld());

        sign.put(Sign.location.toString(), loc);

        return sign;
    }

    public static List<SignTable> getSigns(JSONArray signs) {
        List<SignTable> signsT = new ArrayList();

        // Check if not exists
        if (signs == null)
            return signsT;
        
        Iterator<JSONObject> eachSign = signs.iterator();
        while (eachSign.hasNext()) {
            JSONObject js = eachSign.next();
            SignTable st = new SignTable();

            st.setNombre((String) js.get(Sign.name.toString()));
            st.setModelo((String) js.get(Sign.model.toString()));
            st.setOrientacion((String) js.get(Sign.orientation.toString()));
            st.setBlockface(((Long) js.get(Sign.blockface.toString())).intValue());

            JSONObject loc = (JSONObject) js.get(Sign.location.toString());

            st.setWorld((String) loc.get(Sign.Location.world.toString()));
            st.setX(((Long) loc.get(Sign.Location.x.toString())).intValue());
            st.setY(((Long) loc.get(Sign.Location.y.toString())).intValue());
            st.setZ(((Long) loc.get(Sign.Location.z.toString())).intValue());

            signsT.add(st);
        }

        return signsT;
    }

    public static Object createPlayerW(PlayerWTable next) {
        JSONObject pworld = new JSONObject();

        pworld.put(PlayerWorld.uuid.toString(), next.getPlayerName());
        pworld.put(PlayerWorld.points.toString(), next.getFamePoints());
        pworld.put(PlayerWorld.world.toString(), next.getWorld());

        return pworld;
    }

    public static List<PlayerWTable> getPWorlds(JSONArray pworlds) {
        List<PlayerWTable> pworldT = new ArrayList();

        // Check if not exists
        if (pworlds == null) 
            return pworldT;
        
        Iterator<JSONObject> eachPWorld = pworlds.iterator();
        while (eachPWorld.hasNext()) {
            JSONObject js = eachPWorld.next();
            PlayerWTable pwt = new PlayerWTable();

            pwt.setPlayerName((String) js.get(PlayerWorld.uuid.toString()));
            pwt.setFamePoints(((Long) js.get(PlayerWorld.points.toString())).intValue());
            pwt.setWorld((String) js.get(PlayerWorld.world.toString()));

            pworldT.add(pwt);
        }

        return pworldT;
    }
    
    public static Object createPlayerTime(TimeTable next) {
        JSONObject pTime = new JSONObject();

        pTime.put(PlayerTime.playerUUID.toString(), next.getPlayerName());
        pTime.put(PlayerTime.playedTime.toString(), next.getPlayedTime());

        return pTime;
    }
    
    public static List<TimeTable> getPlayersTime(JSONArray pTimes) {
        List<TimeTable> pTime = new ArrayList();

        // Check if not exists
        if (pTimes == null)
            return pTime;
        
        Iterator<JSONObject> eachPTime = pTimes.iterator();
        while (eachPTime.hasNext()) {
            JSONObject js = eachPTime.next();
            TimeTable tt = new TimeTable();

            tt.setPlayerName((String) js.get(PlayerTime.playerUUID.toString()));
            tt.setPlayedTime(((Long) js.get(PlayerTime.playedTime.toString())).intValue());

            pTime.add(tt);
        }

        return pTime;
    }
}
