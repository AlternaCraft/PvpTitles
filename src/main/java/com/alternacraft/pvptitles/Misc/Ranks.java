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
package com.alternacraft.pvptitles.Misc;

import com.alternacraft.pvptitles.Exceptions.RanksException;
import com.alternacraft.pvptitles.Main.Manager;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author AlternaCraft
 */
public class Ranks {

    private static int nextRankFame;
    private static int nextRankTime;
    private static String nextRankTitle;

    /**
     * Método para recibir el rango segun los puntos que tenga el jugador
     *
     * @param fame Puntos pvp
     * @param seconds Cantidad de dias
     * @return String con el nombre del titulo
     * @throws com.alternacraft.pvptitles.Exceptions.RanksException
     */
    public static String getRank(int fame, int seconds) throws RanksException {
        String rank = "";

        LinkedList<String> rankList = Manager.rankList();
        LinkedList<Integer> reqFame = Manager.reqFame();
        LinkedList<Integer> reqTime = Manager.reqTime();

        /*
         Caso puntual para comprobar si los puntos son mayores que todos 
         los de la lista (Además evito que lance un NullPointerEx el bucle de debajo)
         */
        if (fame >= reqFame.getLast() && seconds >= reqTime.getLast()) {
            nextRankFame = 0;
            nextRankTime = 0;
            rank = rankList.getLast();
        } else {
            // Desde el primero hasta el penúltimo
            for (int i = 0; i < reqFame.size() - 1; i++) {
                // Voy comprobando si esta entre los puntos que va obteniendo
                if (fame >= reqFame.get(i) && fame < reqFame.get(i + 1)) {
                    for (int j = i; j >= 0; j--) {
                        if (seconds >= reqTime.get(j)) {
                            nextRankFame = reqFame.get(j + 1) - fame;
                            nextRankTitle = rankList.get(j + 1);
                            nextRankTime = reqTime.get(j + 1) - seconds;

                            rank = rankList.get(j);

                            break;
                        }
                    }
                }
            }
        }

        if (rank.isEmpty()) {
            Map<String, Object> data = new LinkedHashMap<>();

            data.put("Fame", fame);
            data.put("Seconds", seconds);
            data.put("Available ranks", rankList.size());
            data.put("Fame required", reqFame);
            data.put("Time required", reqTime);

            throw new RanksException("getting rank", data);
        }

        return rank;
    }

    /**
     * Método para recibir los puntos restantes para conseguir el nuevo titulo
     *
     * @return Entero con los puntos
     */
    public static int fameToRankUp() {
        return (nextRankFame < 0) ? 0 : nextRankFame;
    }

    /**
     * Método para recibir el numero de minutos restantes para conseguir el
     * titulo
     *
     * @return Entero con los dias
     */
    public static int nextRankTime() {
        return (nextRankTime < 0) ? 0 : nextRankTime;
    }

    /**
     * Método para recibir el nuevo titulo que va a conseguir el jugador
     *
     * @return String con el titulo
     */
    public static String nextRankTitle() {
        return (nextRankTitle == null) ? "" : nextRankTitle;
    }
}
