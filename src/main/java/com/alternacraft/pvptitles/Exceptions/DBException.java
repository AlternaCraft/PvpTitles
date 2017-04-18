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
package com.alternacraft.pvptitles.Exceptions;

import com.alternacraft.pvptitles.Main.DBLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * If you don't know the error cause, please, report it.
 */
public class DBException extends CustomException {

    private static final String PREFIX = "00x";
    
    // <editor-fold defaultstate="collapsed" desc="ERRORS">
    public static final String UNKNOWN_ERROR = "Unknown error";
    public static final String PLAYER_CONNECTION_ERROR = "Error checking if player is registered";
    public static final String PLAYER_TIME_ERROR = "Error loading player time";
    public static final String MULTIWORLD_ERROR = "Error checking with multiworld";
    public static final String TOP_PLAYERS_ERROR = "Error getting top players";
    public static final String SAVING_BOARD_ERROR = "Error saving board";
    public static final String SEARCHING_BOARD_ERROR = "Error searching board";
    public static final String REMOVING_BOARD_ERROR = "Error removing board";
    public static final String UPDATING_BOARD_ERROR = "Error updating board";
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="INTERNAL STUFF">
    private DB_METHOD type = null;

    public enum DB_METHOD {
        PLAYER_CONNECTION,
        PLAYER_FAME_SAVING,
        PLAYER_FAME_LOADING,
        PLAYER_TIME_SAVING,
        PLAYER_TIME_LOADING,
        PLAYERS_TOP,
        BOARD_SAVING,
        BOARD_REMOVING,
        BOARD_UPDATING,
        BOARD_SEARCHING,
        SERVER_NAME,
        PURGE_DATA,
        DB_EXPORT,
        DB_IMPORT,
        REPAIR
    }

    private enum POSSIBLE_ERRORS {
        NOT_FOUND("00", "Couldn't find a reason for the error..."),
        DB_CONNECTION("01", "The server has lost the MySQL connection");

        private String error_num = "-1";
        private String error_str = null;

        private POSSIBLE_ERRORS(String num, String msg) {
            this.error_num = num;
            this.error_str = msg;
        }

        public String getText() {
            return this.error_str + " (" + PREFIX + this.error_num + ")";
        }
    }
    // </editor-fold>
    
    public DBException(String message, DB_METHOD type) {
        super(message);
        this.type = type;
    }

    public DBException(String message, DB_METHOD type, String custom_error) {
        super(message, custom_error);
        this.type = type;
    }

    public DBException(String message, DB_METHOD type, HashMap<String, Object> data) {
        super(message, data);
        this.type = type;
    }

    public DB_METHOD getType() {
        return type;
    }

    // <editor-fold defaultstate="collapsed" desc="ERROR ELEMENTS">
    @Override
    protected List getHeader() {
        return new ArrayList() {
            {
                this.add(new StringBuilder().append(R).append("(").append(DBLoader.tipo.toString())
                        .append(" ERROR) On ").append(getFilteredString(type.toString()))
                        .append(" gets \"").append(getMessage()).append("\"").toString());
            }
        };
    }
    
    @Override
    protected List getPossibleReasons() {
        return new ArrayList<String>() {
            {
                this.add("          " + G + "====== " + V + "POSSIBLE REASONS" + G + " ======");
                this.addAll(getPossibleErrors());
            }
        };
    }
    // </editor-fold>

    private List getPossibleErrors() {
        List<String> possible_errors = new ArrayList();

        for (Map.Entry<String, Object> entry : this.data.entrySet()) {
            String k = entry.getKey();
            Object v = entry.getValue();

            if (k.contains("MySQL") && k.contains("connection")) {
                if (!(boolean) v) {
                    possible_errors.add(new StringBuilder("- ")
                                .append(POSSIBLE_ERRORS.DB_CONNECTION.getText()).toString());
                }
            }
        }
        
        if (this.custom_error != null) {
            if (this.custom_error.contains("connection closed") 
                    || this.custom_error.contains("Communications link failure")) {
                possible_errors.add(new StringBuilder("- ")
                                    .append(POSSIBLE_ERRORS.DB_CONNECTION.getText()).toString());
            }
        }

        return (possible_errors.isEmpty()) ? 
                new ArrayList() {{ this.add(POSSIBLE_ERRORS.NOT_FOUND.getText()); }}
                : possible_errors;
    }
    
    private String getFilteredString(String str) {
        return "\"" + str.replaceAll("_", " ") + "\"";
    }
}
