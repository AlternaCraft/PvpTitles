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
package com.alternacraft.pvptitles.Exceptions;

import com.alternacraft.pvptitles.Main.PvpTitles;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class CustomException extends Exception {

    protected final String REPORT = "If you don't know the error cause, please, report it.\n"
            + "http://dev.bukkit.org/bukkit-plugins/pvptitles/create-ticket/";

    protected static final short SIMPLIFIED = 0;
    protected static final short ESSENTIAL = 1;
    protected static final short FULL = 2;

    protected Map<String, Object> data = new LinkedHashMap();
    protected String custom_error = null;

    public CustomException(String message) {
        super(message);
    }

    public CustomException(String message, Map<String, Object> data) {
        this(message);
        this.data = data;
    }

    public CustomException(String message, String custom_error) {
        this(message);
        this.custom_error = custom_error;
    }

    public String getCustomMessage() {
        int n = PvpTitles.getInstance().getManager().params.getErrorFormat();

        switch (n) {
            case SIMPLIFIED:
                return getHeader();
            case ESSENTIAL:
                return new StringBuilder(getHeader())
                        .append(getBody())
                        .append("\n").toString();
            case FULL:
                return new StringBuilder(getHeader())
                        .append(getExtraData())
                        .append(getBody())
                        .append(getPossibleReasons())
                        .append(getReportMessage()).toString();
            default:
                return "";
        }
    }

    /* CUSTOM MESSAGE */
    protected abstract String getHeader();

    protected String getBody() {
        return new StringBuilder()
                .append("\n\nStackTrace:")
                .append("\n-----------")
                .append(getSource()).toString();
    }

    protected abstract String getPossibleReasons();

    protected String getExtraData() {
        String extradata = "";

        if (!this.data.isEmpty()) {
            extradata = "\n\nMore information:";
            extradata += "\n-----------------";
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                extradata += new StringBuilder().append("\n- ").append(key)
                        .append(": ").append(value).toString();
            }
        } else if (this.custom_error != null) {
            extradata = "\nMore information: " + this.custom_error;
        }

        return extradata;
    }

    protected String getReportMessage() {
        return new StringBuilder()
                .append("\n-------------------------------------------------------------\n")
                .append(this.REPORT)
                .append("\n-------------------------------------------------------------").toString();
    }

    /* UTILS */
    protected String getSource() {
        String source = "";

        for (int i = 0; i < this.getStackTrace().length; i++) {
            String str = this.getStackTrace()[i].toString();
            if (str.contains(PvpTitles.getInstance().getDescription().getName().toLowerCase())) {
                source += "\n" + str;
            } else {
                break;
            }
        }

        return source;
    }

}
