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
package com.alternacraft.pvptitles.Misc;

import com.alternacraft.pvptitles.Files.LangsFile.LangType;
import com.alternacraft.pvptitles.Main.Manager;
import java.lang.reflect.Field;
import org.bukkit.entity.Player;

/**
 * 
 * @see NMS
 */
public enum Localizer {

    ENGLISH(LangType.EN, "en_US"),    
    SPANISH(LangType.ES, "es_ES"),
    ARGENTINEAN_SPANISH(LangType.ES, "es_AR"),
    MEXICO_SPANISH(LangType.ES, "es_MX"),
    URUGUAY_SPANISH(LangType.ES, "es_UY"),
    VENEZUELA_SPANISH(LangType.ES, "es_VE"),    
    CZECH(LangType.CUSTOM_CS, "cs_CZ"),
    EUSKARA(LangType.CUSTOM_EU, "eu_ES"),
    GALICIAN(LangType.CUSTOM_GL, "gl_ES"),
    CATALAN(LangType.CUSTOM_CA, "ca_ES"),
    CROATIAN(LangType.CUSTOM_HR, "hr_HR"),
    KOREAN(LangType.CUSTOM_KO, "ko_KR"),
    UKRAINIAN(LangType.CUSTOM_UK, "uk_UA"),
    POLISH(LangType.CUSTOM_PL, "pl_PL"),
    SLOVENIAN(LangType.CUSTOM_SL, "sl_SI"),
    SERBIAN(LangType.CUSTOM_SR, "sr_SP"),
    ROMANIAN(LangType.CUSTOM_RO, "ro_RO"),    
    SWEDISH(LangType.CUSTOM_SV, "sv_SE"),
    PORTUGUESE_BR(LangType.CUSTOM_PT, "pt_BR"),
    PORTUGUESE_PT(LangType.CUSTOM_PT, "pt_PT"),
    DEUTSCH(LangType.CUSTOM_DE, "de_DE"),
    GREEK(LangType.CUSTOM_GR, "el_GR"),
    FRENCH_CA(LangType.CUSTOM_FR, "fr_CA"), 
    FRENCH(LangType.CUSTOM_FR, "fr_FR"),
    JAPANESE(LangType.CUSTOM_JP, "ja_JP"),
    SIMPLIFIED_CHINESE(LangType.CUSTOM_CN, "zh_CN"),    
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
