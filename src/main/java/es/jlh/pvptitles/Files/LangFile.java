package es.jlh.pvptitles.Files;

import es.jlh.pvptitles.Main.PvpTitles;
import es.jlh.pvptitles.Misc.Utils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author AlternaCraft
 */
public enum LangFile {

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
    FAME_EDIT_PLAYER(
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
            "View/modify the range of a player"
    ),
    COMMAND_BOARD_INFO(
            "Interactuar con los tableros registrados en el server",
            "Interact with boards registered in the server"
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
            "&aSe han eliminado %cant% registros",
            "&aHave been removed %cant% users"
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
    RANK_INFO(
            "&bSiguiente rango: &fTe falta %rankup% de %tag% y %timeup% jugados para conseguir %nextRank%",
            "&bRankUP: &fYou need %rankup% of %tag% and %timeup% played to achieve %nextRank%"
    ),
    COMPLETE_TELEPORT_PLAYER(
            "&6Has sido teletransportado correctamente",
            "&6You have been teleported correctly"
    );

    public static enum LangType {

        // Default languages
        ES,
        EN,
        // Other languages not include in the plugin
        CUSTOM_DE,
        CUSTOM_GR,
        CUSTOM_FR,
        CUSTOM_JP,
        CUSTOM_CH,
        CUSTOM_RU;
    }

    private static final String DIRECTORY = new StringBuilder().append(
            "plugins").append(
                    File.separator).append(
                    "PvpTitles").append(
                    File.separator).append(
                    "Langs").append(
                    File.separator).toString();

    private final HashMap<LangType, String> messages = new HashMap();

    private LangFile(String es, String en) {
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
        return Utils.translateColor(getDefaultText(lang));
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

            // Lista negra
            if (langType.name().contains("CUSTOM")) {
                continue;
            }

            File langFile = new File(DIRECTORY + "messages_" + langType.name() + ".yml");

            if (!langFile.exists()) {
                createConfig(langFile, langType);
            }

            YamlConfiguration lang = YamlConfiguration.loadConfiguration(langFile);

            if (!compLocales(langFile, lang, langType)) {
                PvpTitles.logError("Error loading " + langType.name() + " locales, "
                        + "a new one has been created.", null);
            }
        }
    }

    private static void createConfig(File langFile, LangType lang) {
        YamlConfiguration newConfig = new YamlConfiguration();

        newConfig.options().header(
                "########################################\n"
                + "## [LOCALES]Do not edit %variables% ##\n"
                + "########################################"
        );
        newConfig.options().copyHeader(true);

        for (LangFile idioma : LangFile.values()) {
            String name = idioma.name();
            String value = idioma.getDefaultText(lang);
            newConfig.set(name, value);
        }

        try {
            newConfig.save(langFile);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static boolean compLocales(File langFile, YamlConfiguration langConf,
            LangType langType) {
        File backupFile = new File(DIRECTORY + "messages_" + langType.name() + "_Backup.yml");

        Boolean resul = true;

        // Check if it is complete
        for (LangFile lang : LangFile.values()) {
            if (!langConf.contains(lang.name())) {
                try {
                    langConf.save(backupFile); // Save the original file                   
                    createConfig(langFile, langType);
                    resul = false;
                } catch (IOException ex) {
                }
            }
        }

        return resul;
    }
}
