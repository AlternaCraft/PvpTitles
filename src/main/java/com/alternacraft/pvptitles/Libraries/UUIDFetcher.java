/*
 * Copyright (c) 2015 Nate Mortensen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.alternacraft.pvptitles.Libraries;

import com.alternacraft.pvptitles.Main.CustomLogger;
import com.google.common.collect.ImmutableList;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class UUIDFetcher implements Callable<Map<String, UUID>> {

    private static final Map<String, UUID> CACHE = new HashMap<>();

    private static final double PROFILES_PER_REQUEST = 100;
    private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
    private final JSONParser jsonParser = new JSONParser();
    private final List<String> names;
    private final boolean rateLimiting;

    public UUIDFetcher(List<String> names, boolean rateLimiting) {
        this.names = ImmutableList.copyOf(names);
        this.rateLimiting = rateLimiting;
    }

    public UUIDFetcher(List<String> names) {
        this(names, true);
    }

    //<editor-fold defaultstate="collapsed" desc="INNER CODE">
    @Override
    public Map<String, UUID> call() {
        Map<String, UUID> uuidMap = new HashMap<>();
        int requests = (int) Math.ceil(names.size() / PROFILES_PER_REQUEST);
        for (int i = 0; i < requests; i++) {
            try {
                HttpURLConnection connection = createConnection();
                String body = JSONArray.toJSONString(names.subList(i * 100, Math.min((i + 1) * 100, names.size())));
                writeBody(connection, body);
                JSONArray array = (JSONArray) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
                array.forEach(profile -> {
                    JSONObject jsonProfile = (JSONObject) profile;
                    String id = (String) jsonProfile.get("id");
                    String name = (String) jsonProfile.get("name");
                    UUID uuid = UUIDFetcher.getUUID(id);
                    uuidMap.put(name, uuid);
                    CACHE.put(name, uuid);
                });
                if (rateLimiting && i != requests - 1) {
                    Thread.sleep(100L);
                }
            } catch (Exception ex) {
                CustomLogger.logError(ex.getMessage());
            }
        }
        return uuidMap;
    }

    private static void writeBody(HttpURLConnection connection, String body) throws Exception {
        try (OutputStream stream = connection.getOutputStream()) {
            stream.write(body.getBytes());
            stream.flush();
        }
    }

    private static HttpURLConnection createConnection() throws Exception {
        URL url = new URL(PROFILE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }

    private static UUID getUUID(String id) {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) 
                + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) 
                + "-" + id.substring(20, 32));
    }

    private static UUID getUUIDOf(String name) {
        // Optimizacion
        if (CACHE.containsKey(name)) {
            return CACHE.get(name);
        }

        Map<String, UUID> uuids = new UUIDFetcher(Arrays.asList(name)).call();

        if (uuids.containsKey(name)) {
            return uuids.get(name);
        } else {
            return null;
        }
    }
    //</editor-fold>

    /**
     * Método para obtener un UUID según el tipo de servidor
     * 
     * <ul>
     *  <li>Si el server está en modo online utiliza lo obtenido por la API</li>
     *  <li>Si el server está en modo offline utiliza el método getOfflinePlayer
     *      <ul>
     *          <li>
     *              Si el jugador Juan está online y el jugador juan no, este método
     *              no funciona correctamente al intentar asignarle el valor a juan.
     *              (En caso de que ambos no hayan entrado antes al server)
     *          </li>
     *      </ul>
     *  </li>
     * </ul>
     *
     * @param name Nombre del jugador
     * @return UUID
     */
    public static UUID getUUIDPlayer(String name) {
        UUID uuid;

        // Comprobacion case sensitive, si ha entrado alguna vez
        OfflinePlayer[] offplayers = Bukkit.getServer().getOfflinePlayers();
        for (OfflinePlayer offplayer : offplayers) {
            if (offplayer == null) continue;
            if (offplayer.getName().equals(name)) {
                return offplayer.getUniqueId();                
            }
        }
        
        if (Bukkit.getServer().getOnlineMode()) {
            // Puede no haber entrado
            uuid = UUIDFetcher.getUUIDOf(name);
        } else {
            // Metodo guarro
            uuid = Bukkit.getServer().getOfflinePlayer(name).getUniqueId();
        }

        return uuid;
    }
}
