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
package com.alternacraft.pvptitles.Exceptions;

import com.alternacraft.pvptitles.Main.PvpTitles;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * Custom exception.
 * This add some extras:
 * <ul>
 *  <li>Structured messages. There are three types:
 *      <ul>
 *          <li>Simplified. Just for indicating an error</li>
 *          <li>Essential. Essential information for finding a reason</li>
 *          <li>Full. Complete error</li>
 *      </ul>
 *  </li>
 *  <li>How to report</li>
 *  <li>Functionality for finding a possible reason of the error</li>
 * </ul>
 *
 * @author AlternaCraft
 */
public abstract class CustomException extends Exception {

    protected static final PvpTitles PLUGIN = PvpTitles.getInstance();

    protected static final String NAME = PLUGIN.getDescription().getName();
    protected static final String VERSION = PLUGIN.getDescription().getVersion();

    protected static final ChatColor A = ChatColor.YELLOW;
    protected static final ChatColor V = ChatColor.GREEN;
    protected static final ChatColor G = ChatColor.GRAY;
    protected static final ChatColor R = ChatColor.RED;
    protected static final ChatColor L = ChatColor.RESET;

    protected final List REPORT = new ArrayList(2) {
        {
            this.add(A + "If you don't know the error cause please, report it.");
            this.add(A + "https://dev.bukkit.org/projects/pvptitles/issues/create");
        }
    };

    protected static final short SIMPLIFIED = 0;
    protected static final short ESSENTIAL = 1;
    protected static final short FULL = 2;

    protected Map<String, Object> data = new LinkedHashMap();
    protected String custom_error = null;

    // <editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">    
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
    // </editor-fold>

    public Object[] getCustomStackTrace() {
        int n = PLUGIN.getManager().params.getErrorFormat();
        List result = new ArrayList();

        switch (n) {
            case SIMPLIFIED:
                result.addAll(getHeader());
                break;
            case ESSENTIAL:
                result.addAll(getHeader());
                result.addAll(getBody());
                break;
            case FULL:
                result.add("-------------------------------------------------------------");
                result.addAll(getHeader());
                result.add("-------------------------------------------------------------");
                result.addAll(getExtraData());
                result.addAll(getBody());
                result.addAll(getPossibleReasons());
                result.addAll(getReportMessage());
        }

        return result.toArray();
    }

    // <editor-fold defaultstate="collapsed" desc="ABSTRACT METHODS">
    protected abstract List getHeader();

    protected abstract List getPossibleReasons();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="DEFAULT TEMPLATES">
    protected List getBody() {
        return new ArrayList<String>() {
            {
                this.add("          " + G + "====== " + V + "STACK TRACE" + G + " ======");
                this.addAll(getSource());
                this.add("          " + G + "====== " + V + "DUMP" + G + " ======");
                this.addAll(getPluginInformation());
            }
        };
    }

    protected List getExtraData() {
        return new ArrayList<String>() {
            {
                this.add("          " + G + "====== " + V + "MORE INFORMATION" + G + " ======");
                if (!data.isEmpty()) {
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        this.add(new StringBuilder().append("- ").append(key)
                                .append(": ").append(value).toString());
                    }
                } else if (custom_error != null) {
                    this.add(custom_error);
                }
            }
        };
    }

    protected List getReportMessage() {
        return new ArrayList<String>() {
            {
                this.add("-------------------------------------------------------------");
                this.addAll(REPORT);
                this.add("-------------------------------------------------------------");
            }
        };
    }
    // </editor-fold>

    /* UTILS */
    protected List getSource() {
        return new ArrayList() {
            {
                for (StackTraceElement stackTrace : getStackTrace()) {
                    String str = stackTrace.toString();
                    if (str.contains(NAME.toLowerCase())) {                        
                        this.add(
                          new StringBuilder()
                                .append(stackTrace.getClassName()
                                        .replace("com.alternacraft.pvptitles.", ""))
                                .append("(")
                                  .append(stackTrace.getMethodName())
                                  .append(" -> ")
                                  .append(stackTrace.getLineNumber())
                                .append(")").toString()
                        );
                    }
                }
            }
        };
    }

    protected List getPluginInformation() {
        return new ArrayList<String>() {
            {
                this.add(G + "Plugin name: " + L + NAME);
                this.add(G + "Plugin version: " + L + VERSION);
                this.add(G + "Bukkit version: " + L + Bukkit.getBukkitVersion());
            }
        };
    }
}
