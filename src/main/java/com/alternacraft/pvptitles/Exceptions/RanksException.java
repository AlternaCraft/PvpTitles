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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RanksException extends CustomException {

    private static final String PREFIX = "01x";

    // <editor-fold defaultstate="collapsed" desc="INTERNAL STUFF">
    private enum POSSIBLE_ERRORS {
        NOT_FOUND("00", "Couldn't find a reason for the error..."),
        CORRUPTED_DATA("01", "It seems you have corrupted values in the database."
                + " Please, use /pvpdatabase repair.");

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

    public RanksException(String message) {
        super(message);
    }

    public RanksException(String message, Map<String, Object> data) {
        super(message, data);
    }

    public RanksException(String message, String custom_error) {
        super(message, custom_error);
    }

    @Override
    protected List getHeader() {
        return new ArrayList() {
            {
                this.add(getMessage());
            }
        };
    }

    @Override
    protected List getPossibleReasons() {
        return new ArrayList<String>() {
            {
                this.add("          ====== " + V + "POSSIBLE REASONS" + G + " ======");
                this.addAll(getPossibleErrors());
            }
        };
    }
    // </editor-fold>

    private List getPossibleErrors() {
        List<String> possible_errors = new ArrayList();

        data.entrySet().forEach(entry -> {
            String k = entry.getKey();
            Object v = entry.getValue();
            
            if (k.equals("Seconds") || k.equals("Fame")) {
                boolean lowerThanZero = false;
                if (k.equals("Seconds")) lowerThanZero = (long)v < 0;
                if (k.equals("Fame")) lowerThanZero = (int)v < 0;
                if (lowerThanZero) {
                    if (!possible_errors.contains(POSSIBLE_ERRORS.CORRUPTED_DATA.getText())) {
                        possible_errors.add(new StringBuilder("- ")
                                .append(POSSIBLE_ERRORS.CORRUPTED_DATA.getText()).toString());
                    }
                }
            }
        });

        return (possible_errors.isEmpty()) ? 
                new ArrayList() {{ this.add(POSSIBLE_ERRORS.NOT_FOUND.getText()); }}
                : possible_errors;
    }
}
