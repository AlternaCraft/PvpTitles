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
package es.jlh.pvptitles.Backend.Exceptions;

import es.jlh.pvptitles.Main.Handlers.DBHandler;
import java.util.HashMap;
import java.util.Map;

/**If you don't know the error cause, please, report it.
 *
 * @author AlternaCraft
 */
public class DBException extends Exception {

    public static final String UNKNOWN_ERROR = "Unknown error";

    private final String REPORT = "If you don't know the error cause, please, report it.\n"
            + "http://dev.bukkit.org/bukkit-plugins/pvptitles/create-ticket/";

    private TYPE type = null;
    private HashMap<String, Object> data = new HashMap();
    private String custom_error = null;

    public enum TYPE {
        PLAYER_CONNECTION,
        PLAYER_FAME_SAVING,
        PLAYER_FAME_LOADING,
        PLAYER_TIME_SAVING,
        PLAYER_TIME_LOADING,
        PLAYERS_TOP,
        
        BOARD_SAVING,
        BOARD_REMOVING,
        BOARD_UPDATING,
        BOARD_SEARCHING
    }

    public DBException(String message, TYPE type) {
        super(message);
        this.type = type;
    }

    public DBException(String message, TYPE type, String custom_error) {
        super(message);
        this.type = type;
        this.custom_error = custom_error;
    }

    public DBException(String message, TYPE type, HashMap<String, Object> data) {
        super(message);
        this.type = type;
        this.data = data;
    }

    public TYPE getType() {
        return type;
    }

    public String getCustomMessage() {
        String finalmessage = "=== " + DBHandler.tipo.toString() + " ERROR ===";
        String extradata = "";

        if (!this.data.isEmpty()) {
            extradata = "\n\nMore information:\n";
            extradata += "-----------------\n";
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                extradata += new StringBuilder().append("- ").append(key)
                        .append(": ").append(value).append("\n").toString();
            }
        } else if (this.custom_error != null) {
            extradata = "\nMore information: " + this.custom_error;
        }

        finalmessage = new StringBuilder(finalmessage).append("\n")
                .append("On ").append(getFilteredString(this.type.toString())).append(" gets \"").append(this.getMessage()).append("\"")
                .append(extradata)
                .append("\n-------------------------------------------------------------\n")
                .append(this.REPORT)
                .append("\n-------------------------------------------------------------").toString();

        return "\n\n" + finalmessage + "\n";
    }
    
    private String getFilteredString(String str) {
        return "\"" + str.replaceAll("_", " ") + "\"";
    }
}
