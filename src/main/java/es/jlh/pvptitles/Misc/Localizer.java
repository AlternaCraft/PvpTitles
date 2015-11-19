package es.jlh.pvptitles.Misc;

import es.jlh.pvptitles.Libraries.NMS;
import es.jlh.pvptitles.Configs.LangFile.LangType;
import es.jlh.pvptitles.Main.Manager;
import java.lang.reflect.Field;
import org.bukkit.entity.Player;

/**
 *
 * @author AlternaCraft
 */
public enum Localizer {

    // English variants have been merged for sake of simplicity.
    ENGLISH(LangType.EN, "en_US"),
    SPANISH(LangType.ES, "es_ES"),
    ARGENTINEAN_SPANISH(LangType.ES, "es_AR"),
    MEXICO_SPANISH(LangType.ES, "es_MX"),
    URUGUAY_SPANISH(LangType.ES, "es_UY"),
    VENEZUELA_SPANISH(LangType.ES, "es_VE"),
    DEUTSCH(LangType.DE, "de_DE");

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

    private static Field field;

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
