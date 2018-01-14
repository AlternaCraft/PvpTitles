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
package com.alternacraft.pvptitles.Backend;

import com.alternacraft.pvptitles.Hooks.VaultHook;
import com.alternacraft.pvptitles.Libraries.UUIDFetcher;
import com.alternacraft.pvptitles.Main.DBLoader.DBType;
import com.alternacraft.pvptitles.Main.Manager;
import static com.alternacraft.pvptitles.Main.PvpTitles.getInstance;
import com.alternacraft.pvptitles.Misc.Formulas.EvaluableExpression;
import com.alternacraft.pvptitles.Misc.Formulas.Expression;
import com.alternacraft.pvptitles.Misc.StrUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class ConfigDataStore {

    //<editor-fold defaultstate="collapsed" desc="VARIABLES">    
    /* METRICS && UPDATES */
    // Atributo para guardar si se van a usar las metricas
    private boolean metrics = true;
    // Atributo para guardar si se va a actualizar o no automaticamente
    private boolean update = false;
    // Atributo para guardar si se va a avisar de las actualizaciones o no
    private boolean alert = true;
    // Atributo para mostar las integraciones con plugins de terceros
    private boolean displayIntegrations = true;
    // Atributo para guardar el tipo de formato para el error
    private short errorFormat = 2;
    // Atributo para almacenar la base de datos por defecto
    private DBType defaultDB = DBType.SQLITE;

    /* PVPTITLES BRIDGE */
    private boolean PvpTitles_Bridge = false;
    private boolean use_ssl = false;
    // Datos de la conexion a MySQL
    private String host = "localhost";
    private short port = 3306;
    private String db = "database";
    private String user = "user";
    private String pass = "pass";
    // Server ID
    private short multiS = -1;
    // Server Name
    private String nameS = "";

    /* MULTIWORLD */
    private boolean mw_enabled = false;

    // Atributo con los mundos
    private final List<String> affected_worlds = new ArrayList();
    // Atributo para mostrar o no los titulos en el chat de los mundos escritos
    private boolean title = false;
    // Atributo para permitir ganar puntos de fama a los jugadores en los mundos escritos
    private boolean points = false;
    // Atributo para mostrar los jugadores en la tabla de puntuaciones
    private boolean leaderboard = false;

    /* LEADERBOARD */
    // Tiempo de actualizacion de los carteles (Minutos)
    private short LBRefresh = 0;

    /* RANK CHECKER */
    // Tiempo para comprobar si tiene un nuevo rango
    private short rankChecker = 0;

    /* PURGE */
    // Lista de jugadores que NO seran borrados por el comando
    private final List<String> noPurgePlayers = new ArrayList();
    // Tiempo necesario para que un registro sea entendido como inactivo
    private short purgeTime = 0;

    /* ANTIFARM */
    // Maximo de bajas para el sistema antifarm
    private short maxKills = 0;
    // Tiempo para volver a matar una vez superado el limite de bajas
    private short vetoTime = 0;
    // Tiempo para limpiar las bajas realizadas a un jugador
    private short cleanerTime = 0;
    // Prevenir la obtención de fama de un jugador o de todos
    private boolean preventFromEvery = true;
    // Comprueba AFK
    private boolean checkAFK = false;
    // Tiempo AFK
    private short AFKTime = 0;

    /* POINTS */
    // Multipliers
    public static final String[] MP_TYPES = {"RMoney", "RPoints", "RTime", "Points", "Time"};
    private Map<String, Map<String, Double>> multipliers = new HashMap();
    // Killstreak
    private final List<String> resetOptions = new ArrayList();
    private boolean addDeathOnlyByPlayer = true;
    private boolean resetOnPlayerLeaving = true;
    // Awarded points
    private boolean enableRPWhenKilling = true;
    private boolean enableLPWhenDying = false;
    private boolean LPWhenDyingJustByPlayers = false;
    // Formulas
    // Default variables to formulas
    private final Map<String, Double> variables = new HashMap();
    private Expression receivedFormula = null;
    private double receivedMod = 0.25D;
    private Expression lostFormula = null;
    private double lostMod = 0.25D;

    /* CHAT */
    // Mostrar el titulo del jugador en el chat
    private boolean displayInChat = true;
    // Mostrar el titulo del jugador sobre su cabeza
    private boolean displayLikeHolo = false;
    // Formato para el titulo como holograma
    private String holotagformat = null;
    // Modificador de la altura del holograma
    private short holoHeightMod = 1;

    // Nombre de los puntos
    private String tag = null;
    // Formato genérico del título
    private String format = null;
    // Tag para intercambiar por el titulo
    private String prefix = null;
    // Cantidad de jugadores que aparecen en el ranking
    private short top = 0;

    /**
     * Constructor de la clase
     */
    public ConfigDataStore() {
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="SETTERS">    
    public void setPvpTitles_Bridge(boolean PvpTitles_Bridge) {
        this.PvpTitles_Bridge = PvpTitles_Bridge;
    }

    public void setUse_ssl(boolean use_ssl) {
        this.use_ssl = use_ssl;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(short port) {
        this.port = port;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setMultiS(short multiS) {
        this.multiS = multiS;
    }

    public void setNameS(String nameS) {
        if ("".equals(nameS) || "Custom".equals(nameS)) {
            nameS = getInstance().getServer().getServerName();
        }
        this.nameS = nameS;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setTop(short top) {
        if (top > 0) {
            this.top = top;
        }
    }

    public void setLBRefresh(short LBRefresh) {
        if (LBRefresh > 0) {
            this.LBRefresh = LBRefresh;
        }
    }

    public void setRankChecker(short rankChecker) {
        if (rankChecker > 0) {
            this.rankChecker = rankChecker;
        }
    }

    public void setNoPurgePlayers(List<String> no_pp) {
        no_pp.forEach(name -> {
            UUID uuid = UUIDFetcher.getUUIDPlayer(name);
            if (uuid != null && !this.noPurgePlayers.contains(uuid.toString())) {
                this.noPurgePlayers.add(uuid.toString());
            }
        });
    }

    public void setPurgeTime(short timeP) {
        if (timeP > 0) {
            this.purgeTime = timeP;
        }
    }

    public void setMaxKills(short kills) {
        if (kills > 0) {
            this.maxKills = kills;
        }
    }

    public void setVetoTime(short timeV) {
        if (timeV > 0) {
            this.vetoTime = timeV;
        }
    }

    public void setPreventFromEvery(boolean preventFromEvery) {
        this.preventFromEvery = preventFromEvery;
    }

    public void setCleanerTime(short timeL) {
        if (timeL > 0) {
            this.cleanerTime = timeL;
        }
    }

    public void setCheckAFK(boolean checkAFK) {
        this.checkAFK = checkAFK;
    }

    public void setAFKTime(short AFKTime) {
        this.AFKTime = AFKTime;
    }

    public void setEnableRPWhenKilling(boolean enableRPWhenKilling) {
        this.enableRPWhenKilling = enableRPWhenKilling;
    }

    public void setEnableLPWhenDying(boolean enableLPWhenDying) {
        this.enableLPWhenDying = enableLPWhenDying;
    }

    public void setLPWhenDyingJustByPlayers(boolean LPWhenDyingJustByPlayers) {
        this.LPWhenDyingJustByPlayers = LPWhenDyingJustByPlayers;
    }

    public void addVariableToFormula(String var, double val) {
        this.variables.put(var.toLowerCase(), val);
    }

    public void setMultipliers(Map<String, Map<String, Double>> multipliers) {
        this.multipliers = multipliers;
    }

    public boolean setMultiplier(String type, String name, double value) {
        if (value <= 0) {
            return false;
        }
        if (!this.multipliers.containsKey(type)) {
            this.multipliers.put(type, new HashMap());
        }
        this.multipliers.get(type).put(name, value);
        return true;
    }

    public void setResetOptions(List<String> resetOptions) {
        resetOptions.forEach(option -> {
            if (!this.resetOptions.contains(option)) {
                this.resetOptions.add(option);
            }
        });
    }

    public void setAddDeathOnlyByPlayer(boolean addDeathOnlyByPlayer) {
        this.addDeathOnlyByPlayer = addDeathOnlyByPlayer;
    }

    public void setResetOnPlayerLeaving(boolean resetOnPlayerLeaving) {
        this.resetOnPlayerLeaving = resetOnPlayerLeaving;
    }

    public void setReceivedFormula(String formula) throws RuntimeException {
        this.receivedFormula = new EvaluableExpression(formula.toLowerCase(), variables).parse();
    }

    public void setReceivedMod(double receivedMod) {
        this.receivedMod = Math.abs(receivedMod);
    }

    public void setLostFormula(String formula) throws RuntimeException {
        this.lostFormula = new EvaluableExpression(formula.toLowerCase(), variables).parse();
    }

    public void setLostMod(double lostMod) {
        this.lostMod = Math.abs(lostMod);
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public void setAlert(boolean alert) {
        this.alert = alert;
    }

    public void setDisplayIntegrations(boolean displayIntegrations) {
        this.displayIntegrations = displayIntegrations;
    }

    public void setMetrics(boolean metrics) {
        this.metrics = metrics;
    }

    public void setErrorFormat(short errorFormat) {
        this.errorFormat = errorFormat;
    }

    public void setDefaultDB(DBType defaultDB) {
        this.defaultDB = defaultDB;
    }

    public void setAffectedWorlds(List<String> worlds) {
        worlds.forEach(w -> {
            if (!this.affected_worlds.contains(w.toLowerCase())) {
                this.affected_worlds.add(w.toLowerCase());
            }
        });
    }

    public void setTitle(boolean title) {
        this.title = title;
    }

    public void displayInChat(boolean chatTitle) {
        this.displayInChat = chatTitle;
    }

    public void displayLikeHolo(boolean holoTitle) {
        this.displayLikeHolo = holoTitle;
    }

    public void setHolotagformat(String holotagformat) {
        this.holotagformat = holotagformat;
    }

    public void setHoloHeightMod(short holoheightmod) {
        this.holoHeightMod = (holoheightmod >= 0) ? holoheightmod : 0;
    }

    public void setMw_enabled(boolean mw_enabled) {
        this.mw_enabled = mw_enabled;
    }

    public void setLeaderboard(boolean leaderboard) {
        this.leaderboard = leaderboard;
    }

    public void setPoints(boolean points) {
        this.points = points;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="GETTERS">
    public boolean isPvpTitles_Bridge() {
        return PvpTitles_Bridge;
    }

    public boolean isUse_ssl() {
        return use_ssl;
    }

    public String getHost() {
        return host;
    }

    public short getPort() {
        return port;
    }

    public boolean isDisplayIntegrations() {
        return displayIntegrations;
    }

    public String getDb() {
        return db;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public short getMultiS() {
        return multiS;
    }

    public String getNameS() {
        return nameS;
    }

    public String getTag() {
        return this.tag;
    }

    public short getTop() {
        return this.top;
    }

    public short getLBRefresh() {
        return LBRefresh;
    }

    public short getRankChecker() {
        return rankChecker;
    }

    public short getMaxKills() {
        return maxKills;
    }

    public short getPurgeTime() {
        return purgeTime;
    }

    public short getVetoTime() {
        return vetoTime;
    }

    public boolean isPreventFromEvery() {
        return preventFromEvery;
    }

    public short getCleanerTime() {
        return cleanerTime;
    }

    public boolean isCheckAFK() {
        return checkAFK;
    }

    public short getAFKTime() {
        return AFKTime;
    }

    public boolean isEnableRPWhenKilling() {
        return enableRPWhenKilling;
    }

    public boolean isEnableLPWhenDying() {
        return enableLPWhenDying;
    }

    public boolean isLPWhenDyingJustByPlayers() {
        return LPWhenDyingJustByPlayers;
    }

    public Map<String, Map<String, Double>> getMultipliers() {
        return multipliers;
    }

    public double getMultiplier(String type, OfflinePlayer op) {
        if (!op.isOnline()) {
            return 1D;
        }

        String all_perm = "pvptitles.mp.*.";
        String rewards_perm = "pvptitles.mp.rewards.";
        String defaults_perm = "pvptitles.mp.defaults.";

        Player pl = op.getPlayer();

        Map<String, Map<String, Double>> mults = Manager.getInstance().params.getMultipliers();
        Map<String, Double> perms = mults.get(type);
        for (Map.Entry<String, Double> entry : perms.entrySet()) {
            String key = entry.getKey().toLowerCase();
            Double value = entry.getValue();

            String perm = "pvptitles.mp." + type.toLowerCase() + "." + key;
            boolean global = VaultHook.hasPermission(all_perm + key, pl);

            if (!global) {
                boolean rw = perm.matches("pvptitles\\.mp\\.r.*\\..*");
                if (rw) {
                    global = VaultHook.hasPermission(rewards_perm + key, pl);
                } else {
                    global = VaultHook.hasPermission(defaults_perm + key, pl);
                }
            }

            if (!global) {
                global = VaultHook.hasPermission(perm + key, pl);
            }

            if (global) {
                return value;
            }
        }

        return 1D;
    }

    public boolean hasResetOption(String option) {
        return this.resetOptions.contains(option);
    }

    public boolean isAddDeathOnlyByPlayer() {
        return addDeathOnlyByPlayer;
    }

    public boolean isResetOnPlayerLeaving() {
        return resetOnPlayerLeaving;
    }

    public double getReceivedResult() {
        return receivedFormula.eval();
    }

    public double getReceivedMod() {
        return receivedMod;
    }

    public double getLostResult() {
        return lostFormula.eval();
    }

    public double getLostMod() {
        return lostMod;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getFormat() {
        return format;
    }
    
    public String parseFormat(String title) {
        return StrUtils.translateColors(this.format).replace("%T", title);
    }

    public boolean isMw_enabled() {
        return mw_enabled;
    }

    public boolean isTitleShown() {
        return title;
    }

    public boolean isPoints() {
        return points;
    }

    public boolean showOnLeaderBoard() {
        return leaderboard;
    }

    public List<String> getAffectedWorlds() {
        return affected_worlds;
    }

    public List<String> getNoPurge() {
        return noPurgePlayers;
    }

    public boolean isUpdate() {
        return update;
    }

    public boolean isAlert() {
        return alert;
    }

    public boolean isMetrics() {
        return metrics;
    }

    public short getErrorFormat() {
        return errorFormat;
    }

    public DBType getDefaultDB() {
        return defaultDB;
    }

    public boolean displayInChat() {
        return displayInChat;
    }

    public boolean displayLikeHolo() {
        return displayLikeHolo;
    }

    public String getHolotagformat() {
        return holotagformat;
    }

    public short getHoloHeightMod() {
        return holoHeightMod;
    }
    //</editor-fold>
}
