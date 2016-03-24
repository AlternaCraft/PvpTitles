package es.jlh.pvptitles.Misc;

import es.jlh.pvptitles.Files.LangFile.LangType;
import es.jlh.pvptitles.Main.Manager;
import java.lang.reflect.Field;
import org.bukkit.entity.Player;

/**
 *
 * @author AlternaCraft
 */
public enum Localizer {

    ENGLISH(LangType.EN, "en_US"),
    SPANISH(LangType.ES, "es_ES"),
    ARGENTINEAN_SPANISH(LangType.ES, "es_AR"),
    MEXICO_SPANISH(LangType.ES, "es_MX"),
    URUGUAY_SPANISH(LangType.ES, "es_UY"),
    VENEZUELA_SPANISH(LangType.ES, "es_VE"),
    
    DEUTSCH(LangType.CUSTOM_DE, "de_DE"),
    GREEK(LangType.CUSTOM_GR, "el_GR"),
    FRENCH_CA(LangType.CUSTOM_FR, "fr_CA"), 
    FRENCH(LangType.CUSTOM_FR, "fr_FR"),
    JAPANESE(LangType.CUSTOM_JP, "ja_JP"),
    SIMPLIFIED_CHINESE(LangType.CUSTOM_CH, "zh_CN"), 
    TRADITIONAL_CHINESE(LangType.CUSTOM_CH, "zh_TW"),
    RUSSIAN(LangType.CUSTOM_RU, "ru_RU");

    private final LangType type;
    private final String code;

    private Localizer(LangType type, String code) {
        this.type = type;
        this.code = code;
    }

    public LangType getType() {
        return type;
    }

    public String getCode() {
        return code;
    }

    private static Field field = null;

    public static LangType getLocale(Player inPlayer) {
        try {
            Object nms = NMS.castToNMS(inPlayer);

            if (field == null) {
                field = nms.getClass().getDeclaredField("locale");
                field.setAccessible(true);
            }

            Localizer code = getByCode((String) field.get(nms));

            return code.getType();
        } catch (NoSuchFieldException | SecurityException |
                IllegalArgumentException | IllegalAccessException exc) {
            return Manager.messages;
        }
    }

    public static Localizer getByCode(String code) {
        for (Localizer l : values()) {
            if (l.getCode().equalsIgnoreCase(code)) {
                return l;
            }
        }
        return Localizer.ENGLISH;
    }
}
