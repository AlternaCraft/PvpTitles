package es.jlh.pvptitles.Misc;

import es.jlh.pvptitles.Main.Manager;
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
     */
    public static String getRank(int fame, int seconds) {
        String rank = "";

        Map<Integer, String> rankList = Manager.rankList();
        Map<Integer, Integer> reqFame = Manager.reqFame();
        Map<Integer, Integer> reqTime = Manager.reqTime();

        /*
         Caso puntual para comprobar si los puntos son mayores que todos 
         los de la lista
         */
        if (fame >= reqFame.get(reqFame.values().size() - 1) && seconds
                >= reqTime.get(reqTime.values().size() - 1)) {
            nextRankFame = 999999;
            nextRankTime = 999999;
            return rankList.get(reqFame.values().size() - 1);
        }

        for (int i = 0; i < reqFame.size(); i++) {
            // Voy comprobando si esta entre los puntos que va obteniendo
            if (fame >= reqFame.get(i) && fame < reqFame.get(i + 1)) {
                for (int j = i; j >= 0; j--) {
                    if (seconds >= reqTime.get(j)) {
                        nextRankFame = reqFame.get(j + 1) - fame;
                        nextRankTitle = Utils.translateColor(rankList.get(j + 1));
                        nextRankTime = reqTime.get(j + 1) - seconds;
                        return Utils.translateColor(rankList.get(j));
                    }
                }
            }
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
