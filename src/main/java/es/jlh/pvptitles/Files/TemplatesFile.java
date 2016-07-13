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
package es.jlh.pvptitles.Files;

import es.jlh.pvptitles.Main.PvpTitles;
import es.jlh.pvptitles.Misc.StrUtils;
import es.jlh.pvptitles.Misc.UtilsFile;
import java.io.File;
import java.io.IOException;

public class TemplatesFile {

    private static final char CTV = '%';

    public static final String PLUGIN_TAG = CTV + "PLUGIN" + CTV;
    public static final String VERSION_TAG = CTV + "VERSION" + CTV;

    public static final String COMMAND_TAG = CTV + "CMD" + CTV;
    public static final String INFO_COMMAND_TAG = CTV + "INFO" + CTV;

    public static final String TOP_TAG = CTV + "TOP" + CTV;
    public static final String TOP_POS_TAG = CTV + "POS" + CTV;
    public static final String TOP_PLAYER_TAG = CTV + "PLAYER" + CTV;
    public static final String TOP_POINTS_TAG = CTV + "POINTS" + CTV;

    public static final String RANK_TITLE_TAG = CTV + "RANKTITLE" + CTV;
    public static final String RANK_VALUE_TAG = CTV + "RANKVALUE" + CTV;
    public static final String FAME_TITLE_TAG = CTV + "FAMETITLE" + CTV;
    public static final String FAME_VALUE_TAG = CTV + "FAMEVALUE" + CTV;
    public static final String KS_TITLE_TAG = CTV + "KILLSTREAKTITLE" + CTV;
    public static final String KS_VALUE_TAG = CTV + "KILLSTREAKVALUE" + CTV;
    public static final String NEXT_RANK_TAG = CTV + "NEXTRANK" + CTV;
    public static final String VETO_TAG = CTV + "VETO" + CTV;

    private final String DIRECTORY = new StringBuilder().append(
            PvpTitles.getInstance().getDataFolder()).append(
                    File.separator).append(
                    "Templates").append(
                    File.separator).toString();

    public enum FILES {
        TAGS_DESCRIPTION("INFO.txt", ""
                + "** Common tags **\n"
                + " - " + PLUGIN_TAG + ": Plugin name\n"
                + "\n** pvpladder file **\n"
                + " - " + TOP_TAG + ": Number of players\n"
                + " - " + TOP_POS_TAG + ": Player position\n"
                + " - " + TOP_PLAYER_TAG + ": Player name (And world if MW is enabled)\n"
                + " - " + TOP_POINTS_TAG + ": Player points\n"
                + "\n** pvprank file **\n"
                + " - " + RANK_TITLE_TAG + ": Rank tag (Defined in Langs) \n"
                + " - " + RANK_VALUE_TAG + ": Player's rank name\n"
                + " - " + FAME_TITLE_TAG + ": Fame tag (Defined in Langs & config)\n"
                + " - " + FAME_VALUE_TAG + ": Player's fame value\n"
                + " - " + KS_TITLE_TAG + ": Killstreak tag (Defined in Langs)\n"
                + " - " + KS_VALUE_TAG + ": Player's killstreak value\n"
                + " - " + NEXT_RANK_TAG + ": Information of next rank (Defined in Langs)\n"
                + " - " + VETO_TAG + ": Veto message (Defined in Langs)\n"
                + "\n** pvptitles file **\n"
                + " - " + VERSION_TAG + ": Plugin version\n"
                + " - " + COMMAND_TAG + ": Command usage\n"
                + " - " + INFO_COMMAND_TAG + ": Command description"
        ),
        INFO_COMMAND("pvptitles.txt", ""
                + "\n"
                + PLUGIN_TAG + " &ev" + VERSION_TAG + "\n"
                + "  &b" + COMMAND_TAG + " &r[" + INFO_COMMAND_TAG + "]"),
        LADDER_COMMAND("pvpladder.txt", ""
                + "\n"
                + PLUGIN_TAG + "\n"
                + "  &e--------\n"
                + "    &eTop " + TOP_TAG + "\n"
                + "  &e--------\n"
                + "  " + TOP_POS_TAG + ". " + TOP_PLAYER_TAG + " (&b" + TOP_POINTS_TAG + "&r)"),
        RANK_COMMAND("pvprank.txt", ""
                + "\n"
                + PLUGIN_TAG + "\n"
                + "  - &b" + RANK_TITLE_TAG + ": &r" + RANK_VALUE_TAG + "\n"
                + "  - &b" + FAME_TITLE_TAG + ": &r" + FAME_VALUE_TAG + "\n"
                + "  - &b" + KS_TITLE_TAG + ": &r" + KS_VALUE_TAG + "\n"
                + "  - " + NEXT_RANK_TAG + "\n"
                + "  * " + VETO_TAG);

        private final String path;
        private final String content;

        FILES(String path, String content) {
            this.path = path;
            this.content = content;
        }

        public String getPath() {
            return path;
        }

        public String getContent() {
            return content;
        }
    }

    public TemplatesFile() {
        File dir = new File(DIRECTORY);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    private class CMDFile {

        private File cmdFile;

        public CMDFile(String path) {
            this.cmdFile = new File(DIRECTORY + path);
        }

        public boolean exists() {
            return this.cmdFile.exists();
        }

        public void create(String c) throws IOException {
            this.cmdFile.createNewFile();
            UtilsFile.writeFile(this.cmdFile, c);
        }

        public String[] read() {
            return StrUtils.translateColors(UtilsFile.readFile(this.cmdFile))
                    .split(System.getProperty("line.separator"));
        }
    }

    public void load() {
        for (FILES file : FILES.values()) {
            CMDFile f = new CMDFile(file.getPath());
            if (!f.exists()) {
                try {
                    f.create(file.getContent());
                } catch (IOException ex) {
                    PvpTitles.logError("Template " + file.getPath() + " couldn't be created", null);
                }
            }
        }
    }

    public String[] getFileContent(FILES file) {
        return new CMDFile(file.getPath()).read();
    }

}
