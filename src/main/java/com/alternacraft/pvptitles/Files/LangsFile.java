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
package com.alternacraft.pvptitles.Files;

import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Misc.StrUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Custom class for getting messages
 * <p>
 * Currently, this works with the following custom languages:</p>
 * <ul>
 * <li>Czech (CS)</li>
 * <li>Euskara (EU)</li>
 * <li>Galician (GL)</li>
 * <li>Catalan (CA)</li>
 * <li>Croatian (HR)</li>
 * <li>Korean (KO)</li>
 * <li>Ukrainian (UK)</li>
 * <li>Polish (PL)</li>
 * <li>Slovenian (SL)</li>
 * <li>Serbian (SR)</li>
 * <li>Romanian (RO)</li>
 * <li>Portuguese (PT)</li>
 * <li>German (DE)</li>
 * <li>Greek (GR)</li>
 * <li>French (FR)</li>
 * <li>Japanese (JP)</li>
 * <li>Chinese (CH)</li>
 * <li>Simplified Chinese (CN)</li>
 * <li>Russian (RU)</li>
 * <li>Swedish (SV)</li>
 * </ul>
 */
public enum LangsFile {
    // <editor-fold defaultstate="collapsed" desc="MESSAGES">
    PLUGIN_ENABLED(
            "Plugin activado correctamente",
            "Plugin activated successfully"
    ),
    PLUGIN_DISABLED(
            "Plugin desactivado correctamente",
            "Plugin disabled!"
    ),
    PLUGIN_RELOAD(
            "&6Plugin recargado correctamente",
            "&6Plugin recharged properly"
    ),
    FAME_ADD(
            "&e%tag% sumada correctamente",
            "&e%tag% added correctly"
    ),
    FAME_SEE(
            "&b%tag% del jugador &3&l%player%&b: &3%fame%",
            "&b%tag% player &3&l%player%&b: &3%fame%"
    ),
    FAME_SET(
            "&e%tag% establecida correctamente",
            "&e%tag% set correctly"
    ),
    FAME_MODIFY_ERROR(
            "&4No has podido modificar la %tag% del jugador %player%",
            "&4You could not change the %tag% player %player%"
    ),
    FAME_CHANGE_PLAYER(
            "&eTe han establecido tu %tag% a %fame%, tu rango ahora es %rank%",
            "&eYour %tag% has been established to %fame%, Your rank is now %rank%"
    ),
    FAME_MW_CHANGE_PLAYER(
            "&eTe han establecido tu %tag% a %fame% en el mundo %world%, tu rango ahora es %rank%",
            "&eYour %tag% has been established to %fame% in %world%, Your rank is now %rank%"
    ),
    COMMAND_FORBIDDEN(
            "&4No puedes ejecutar ese comando",
            "&4You can not execute that command"
    ),
    COMMAND_NO_PERMISSIONS(
            "&4No tienes permiso para hacer eso",
            "&4You are not allowed to do that"
    ),
    COMMAND_ARGUMENTS(
            "&4Te faltan/sobran argumentos!",
            "&4Your spare arguments!"
    ),
    COMMAND_RANK_INFO(
            "Para visualizar datos sobre tu fama",
            "Display information about your fame"
    ),
    COMMAND_LADDER_INFO(
            "Ranking de los mejores jugadores PvP",
            "Ranking of the best players in PvP"
    ),
    COMMAND_FAME_INFO(
            "Ver/modificar el rango de un jugador",
            "View/modify the rank of a player"
    ),
    COMMAND_BOARD_INFO(
            "Gestionar los tableros del servidor",
            "Manage server scoreboards"
    ),
    COMMAND_PURGE_INFO(
            "Limpia los usuarios inactivos",
            "Clean inactive users"
    ),
    COMMAND_RELOAD_INFO(
            "Recarga las funciones principales del plugin",
            "Recharge the main config"
    ),
    COMMAND_DATABASE_INFO(
            "Gestiona los datos de la base de datos",
            "Manage data of the database"
    ),
    BOARD_INVENTORY_TITLE(
            "Tablero de puntuaciones",
            "LeaderBoard"
    ),
    BOARD_INVENTORY_ACTION1(
            "&b[INFO] &fTeletransporte",
            "&b[INFO] &fTeleportation"
    ),
    BOARD_INVENTORY_INFO1(
            "Click izquierdo",
            "Left click"
    ),
    BOARD_INVENTORY_ACTION2(
            "&b[INFO] &fBorrar tablero",
            "&b[INFO] &fRemove board"
    ),
    BOARD_INVENTORY_INFO2(
            "Click derecho",
            "Right click"
    ),
    BOARD_INVENTORY_ACTION3_1(
            "Siguiente pagina | Click derecho",
            "Next page | Right click"
    ),
    BOARD_INVENTORY_ACTION3_2(
            "Pagina anterior | Click izquierdo",
            "Previous page | Left click"
    ),
    BOARD_INVENTORY_INFO3(
            "&b[INFO] Pagina %pageNumber%",
            "&b[INFO] Page %pageNumber%"
    ),
    BOARD_CREATED_CORRECTLY(
            "&aTablero %name% registrado correctamente",
            "&aBoard %name% registered correctly"
    ),
    BOARD_DELETED(
            "&aHas eliminado el tablero correctamente",
            "&aYou have removed the board correctly"
    ),
    BOARD_NAME_ALREADY_EXISTS(
            "&4Ese tablero ya existe",
            "&4That board already exists"
    ),
    BOARD_NAME_NOT_EXISTS(
            "&4Ese tablero no existe",
            "&4That board does not exist"
    ),
    BOARD_MODEL_NOT_EXISTS(
            "&4Ese modelo no existe",
            "&4That model does not exist"
    ),
    BOARD_CANT_BE_PLACED(
            "&4No puedes definirlo en una zona con los bloques ocupados",
            "&4You can not create it in an area occupied with other blocks"
    ),
    PURGE_RESULT(
            "&aSe han eliminado %cant% usuario/s",
            "&a%cant% user/s have been removed"
    ),
    DB_REPAIR_RESULT(
            "&aSe han corregido %cant% registro/s",
            "&a%cant% record/s has been fixed"
    ),
    PLAYER_KILLED(
            "&aHas matado a %killed% y has recibido %fameRec% de %tag%",
            "&aYou killed %killed% and have received %fameRec% of %tag%"
    ),
    PLAYER_NEW_RANK(
            "&bFelicidades! Ahora eres: %newRank%",
            "&bCongratulations! You are now: %newRank%"
    ),
    VETO_STARTED(
            "&cHas sido vetado y no conseguiras mas %tag% hasta dentro de %time%",
            "&cYou have been banned and not get more %tag% to within of %time%"
    ),
    VETO_FINISHED(
            "&cVeto finalizado",
            "&cVeto has ended"
    ),
    RANK_INFO_TITLE(
            "Título",
            "Title"
    ),
    RANK_INFO_TAG(
            "%tag%",
            "%tag%"
    ),
    RANK_INFO_KS(
            "Racha",
            "KillStreak"
    ),
    RANK_INFO_NEXTRANK(
            "&bSiguiente rango: &fTe falta %rankup% de %tag% y %timeup% jugados para conseguir %nextRank%",
            "&bRankUP: &fYou need %rankup% of %tag% and %timeup% played to achieve %nextRank%"
    ),
    COMPLETE_TELEPORT_PLAYER(
            "&6Has sido teletransportado correctamente",
            "&6You have been teleported correctly"
    );
    // </editor-fold> 
    
    public static enum LangType {
        ES,
        EN,
        CUSTOM_CS,
        CUSTOM_EU,
        CUSTOM_GL,
        CUSTOM_CA,
        CUSTOM_HR,
        CUSTOM_KO,
        CUSTOM_UK,
        CUSTOM_PL,
        CUSTOM_SL,
        CUSTOM_SR,
        CUSTOM_RO,
        CUSTOM_SV,
        CUSTOM_PT,
        CUSTOM_DE,
        CUSTOM_GR,
        CUSTOM_FR,
        CUSTOM_JP,
        CUSTOM_CH,
        CUSTOM_CN,
        CUSTOM_RU;
    }

    private static final String DIRECTORY = new StringBuilder(PvpTitles.PLUGIN_DIR)
            .append("Langs").append(File.separator).toString();

    private final HashMap<LangType, String> messages = new HashMap();

    private static File backupFile = null;

    private LangsFile(String es, String en) {
        this.messages.put(LangType.ES, es);
        this.messages.put(LangType.EN, en);
    }

    /**
     * Method to get a message with colors translated
     *
     * @param lang LangType
     * @return String
     */
    public String getText(LangType lang) {
        return StrUtils.translateColors(getDefaultText(lang));
    }

    /**
     * Method to get a message without colors translated
     *
     * @param lang LangType
     * @return String
     */
    public String getDefaultText(LangType lang) {
        String value = (this.messages.get(lang) == null)
                ? this.messages.get(LangType.EN) : this.messages.get(lang);

        // File access to get custom message (if exists)
        File langFile = new File(DIRECTORY + "messages_" + lang.name().replace("CUSTOM_", "") + ".yml");
        YamlConfiguration langConf = YamlConfiguration.loadConfiguration(langFile);

        if (langConf != null && langConf.contains(this.name())) {
            value = langConf.getString(this.name());
        }

        return value;
    }

    public static void load() {
        for (LangType langType : LangType.values()) {

            // Black list
            if (langType.name().contains("CUSTOM")) {
                continue;
            }

            File langFile = new File(DIRECTORY + "messages_" + langType.name() + ".yml");

            if (!langFile.exists()) {
                createConfig(langFile, langType, false);
            }

            YamlConfiguration lang = YamlConfiguration.loadConfiguration(langFile);

            if (!compLocales(langFile, lang, langType)) {
                CustomLogger.logError("Error loading " + langType.name() + " locales, "
                        + "a new one has been created.");
            }
        }
    }

    private static void createConfig(File langFile, LangType lang, boolean restore) {
        YamlConfiguration newConfig = new YamlConfiguration();
        YamlConfiguration cBackup = null;
        if (restore) {
            cBackup = YamlConfiguration.loadConfiguration(LangsFile.backupFile);
        }

        newConfig.options().header(
                "########################################\n"
                + "## [LOCALES]Do not edit %variables% ##\n"
                + "########################################"
        );
        newConfig.options().copyHeader(true);

        for (LangsFile idioma : LangsFile.values()) {
            String name = idioma.name();
            String value = idioma.getDefaultText(lang);;
            
            // Set previous value
            if (restore) {
                if (cBackup.contains(name)) {
                    value = cBackup.getString(name);
                }
            }

            newConfig.set(name, value);
        }

        try {
            newConfig.save(langFile);
        } catch (IOException ex) {
            CustomLogger.logError(ex.getMessage());
        }
    }

    private static boolean compLocales(File langFile, YamlConfiguration langConf,
            LangType langType) {
        backupFile = new File(DIRECTORY + "messages_" + langType.name() + "_Backup.yml");

        Boolean resul = true;

        // Check if it is complete
        for (LangsFile lang : LangsFile.values()) {
            if (!langConf.contains(lang.name())) {
                try {
                    langConf.save(backupFile); // Save the original file                   
                    createConfig(langFile, langType, true);
                    resul = false;
                } catch (IOException ex) {
                    CustomLogger.logError(ex.getMessage());
                }
            }
        }

        return resul;
    }
}
