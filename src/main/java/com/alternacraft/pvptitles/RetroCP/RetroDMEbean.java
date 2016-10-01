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
package com.alternacraft.pvptitles.RetroCP;

import com.alternacraft.pvptitles.Backend.Ebean;
import com.alternacraft.pvptitles.Backend.EbeanTables.PlayerPT;
import com.alternacraft.pvptitles.Backend.EbeanTables.SignPT;
import com.alternacraft.pvptitles.Backend.EbeanTables.WorldPlayerPT;
import com.alternacraft.pvptitles.Libraries.UUIDFetcher;
import static com.alternacraft.pvptitles.Main.Managers.MessageManager.showMessage;
import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Misc.TagsClass;
import com.alternacraft.pvptitles.Misc.UtilsFile;
import static com.alternacraft.pvptitles.RetroCP.DBChecker.EBEAN_MW_CREATED;
import static com.alternacraft.pvptitles.RetroCP.DBChecker.EBEAN_TIME_CREATED;
import com.alternacraft.pvptitles.RetroCP.oldTables.PlayerTable;
import com.alternacraft.pvptitles.RetroCP.oldTables.PlayerWTable;
import com.alternacraft.pvptitles.RetroCP.oldTables.SignTable;
import com.alternacraft.pvptitles.RetroCP.oldTables.TimeTable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class RetroDMEbean {

    public static final String FILENAME = "database_temp.json";
    public static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}";

    // <editor-fold defaultstate="collapsed" desc="VARIABLES AND CONSTRUCTOR">
    private PvpTitles pt = null;
    private Ebean ebeanServer = null;

    public RetroDMEbean(PvpTitles pt, Ebean ebeanServer) {
        this.pt = pt;
        this.ebeanServer = ebeanServer;
    }
    // </editor-fold>

    public void conversor() {
        File file = new File((new StringBuilder()).append(
                pt.getDataFolder()).append( // Ruta
                        File.separator).append( // Separador
                        "players").toString()); // Players

        if (file.exists()) {
            int contador = 0;

            File[] allFiles = file.listFiles();

            for (File item : allFiles) {
                // Fama del jugador
                FileConfiguration yaml = YamlConfiguration.loadConfiguration(item);

                String nPlayer = item.getName().substring(0, item.getName().indexOf('.'));
                int fame = yaml.getInt("Fame");
                
                UUID uuid = UUIDFetcher.getIDPlayer(nPlayer);
                
                // No repes
                PlayerPT pl = (PlayerPT) ebeanServer.getDatabase().find(PlayerPT.class)
                        .where("playerUUID like :name OR playerUUID like :uuid")
                            .setParameter("name", nPlayer)
                            .setParameter("uuid", uuid.toString())                        
                        .findUnique();

                if (pl == null) {
                    pl = new PlayerPT();

                    pl.setPlayerUUID(uuid.toString());
                    pl.setPoints(fame);
                    pl.setLastLogin(new Date());

                    ebeanServer.getDatabase().save(pl);

                    contador++;
                }
            }

            showMessage(ChatColor.YELLOW + "" + contador + " user/s " + ChatColor.AQUA
                    + "imported correctly");

            File backup = new File((new StringBuilder()).append(
                    this.pt.getDataFolder()).append( // Ruta
                            File.separator).append( // Separador
                            "you_can_delete_this").toString()); // Players

            if (backup.exists()) {
                // Elimino la carpeta si ya existia
                File[] content = backup.listFiles();
                for (File user : content) {
                    user.delete();
                }
                backup.delete();
            }

            file.renameTo(backup);
        }
    }

    public void conversorUUID() {
        //Guardo usuarios de la bbdd         
        List<PlayerPT> plClass = (List<PlayerPT>) ebeanServer.getDatabase().find(PlayerPT.class)
                .findList();

        // Compruebo si esa vacia
        if (plClass == null || plClass.isEmpty()) {
            return;
        }

        for (PlayerPT player : plClass) {
            if (!player.getPlayerUUID().matches(UUID_REGEX)) {                
                player.setPlayerUUID(UUIDFetcher.getIDPlayer(player.getPlayerUUID()).toString());
            }
        }

        ebeanServer.getDatabase().save(plClass);
    }

    public void exportarData(int status) {
        String ruta = new StringBuilder().append(
                pt.getDataFolder()).append( // Ruta
                        File.separator).append( // Separador
                        FILENAME).toString();

        // Estilo
        JsonParser parser = new JsonParser();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JSONObject jo = new JSONObject();

        JSONArray jsPlayers = new JSONArray();
        JSONArray jsSigns = new JSONArray();
        JSONArray jsWorldPlayers = new JSONArray();

        List<PlayerTable> plClass = (List<PlayerTable>) ebeanServer.getDatabase().
                find(PlayerTable.class).findList();

        List<SignTable> stClass = (List<SignTable>) ebeanServer.getDatabase().
                find(SignTable.class).findList();

        List<PlayerWTable> pwClass = null;
        if (status >= EBEAN_MW_CREATED) {
            pwClass = (List<PlayerWTable>) ebeanServer.getDatabase().
                    find(PlayerWTable.class).findList();
        }

        List<TimeTable> ttClass = null;
        if (status >= EBEAN_TIME_CREATED) {
            ttClass = (List<TimeTable>) ebeanServer.getDatabase().
                    find(TimeTable.class).findList();
        }

        for (PlayerTable next : plClass) {
            PlayerPT pltClass = new PlayerPT();
            pltClass.setPlayerUUID(next.getPlayerName());
            pltClass.setPoints(next.getFamePoints());
            pltClass.setLastLogin(next.getUltMod());

            if (status >= EBEAN_TIME_CREATED) {
                for (TimeTable pltime : ttClass) {
                    if (pltime.getPlayerName().equals(pltClass.getPlayerUUID())) {
                        pltClass.setPlayedTime(pltime.getPlayedTime());
                        ttClass.remove(pltime);
                        break;
                    }
                }
            }

            jsPlayers.add(TagsClass.createPlayer(pltClass));
        }
        jo.put("Players", jsPlayers);

        if (status >= EBEAN_MW_CREATED) {
            for (PlayerWTable next : pwClass) {
                WorldPlayerPT wppt = new WorldPlayerPT();
                wppt.setPlayerUUID(next.getPlayerName());
                wppt.setPoints(next.getFamePoints());
                wppt.setWorld(next.getWorld());

                jsWorldPlayers.add(TagsClass.createPlayerW(wppt));
            }
            jo.put("PlayersPerWorld", jsWorldPlayers);
        }

        for (SignTable next : stClass) {
            SignPT spt = new SignPT();
            spt.setName(next.getNombre());
            spt.setModel(next.getModelo());
            spt.setOrientation(next.getOrientacion());
            spt.setBlockface((short) next.getBlockface());
            spt.setWorld(next.getWorld());
            spt.setX(next.getX());
            spt.setY(next.getY());
            spt.setZ(next.getZ());
            jsSigns.add(TagsClass.createSign(spt));
        }
        jo.put("Signs", jsSigns);

        // Escribo el fichero
        JsonElement el = parser.parse(jo.toJSONString());
        UtilsFile.writeFile(ruta, gson.toJson(el));
    }
}
