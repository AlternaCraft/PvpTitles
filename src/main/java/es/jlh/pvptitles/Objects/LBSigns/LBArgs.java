package es.jlh.pvptitles.Objects.LBSigns;

import es.jlh.pvptitles.Misc.Ranks;
import es.jlh.pvptitles.Misc.Utils;
import es.jlh.pvptitles.Objects.PlayerFame;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.block.Sign;

/**
 *
 * @author AlternaCraft
 */
public class LBArgs {

    private final HashMap<ArgType, Integer> argsValues = new HashMap();
    private final HashMap<ArgType, Boolean> argsBooleans = new HashMap();

    private enum ArgType {

        PLAYER,
        RANK,
        FAME,
        POS,
        SERVER,
        WORLD;
    }

    public LBArgs() {
        resetValues();
        resetBooleans();
    }    
    
    public final void resetValues() {
        for (ArgType value : ArgType.values()) {
            argsValues.put(value, 0);
        }
    }

    public final void resetBooleans() {
        for (ArgType value : ArgType.values()) {
            argsBooleans.put(value, false);
        }
    }

    public boolean containSomeArg(String smfc) {
        for (ArgType arg : ArgType.values()) {
            if (smfc.toUpperCase().contains("<" + arg.name() + ">")) {
                return true;
            }
        }
        
        return false;
    }
    
    public void checkArgs(Sign s, ArrayList<PlayerFame> pf, String smfc, boolean progresivo) {
        boolean entro = false;

        // Colores para todos los datos        
        smfc = Utils.TranslateColor(smfc);
        
        for (ArgType arg : ArgType.values()) {
            if (smfc.toUpperCase().contains("<" + arg.name() + ">")) {
                
                int value = argsValues.get(arg);
                boolean again = argsBooleans.get(arg);

                value = (again && !progresivo) ? (value - 4) : value;
                int temp = value;
                value += 4;

                for (int i = 0; temp < value && temp < pf.size(); i++) {
                    switch (arg) {
                        case PLAYER:
                            s.setLine(i, smfc.replace("<player>", pf.get(temp).getName()));
                            break;
                        case RANK:
                            // Varios parametros en la misma linea
                            if (entro) {
                                s.setLine(i, s.getLine(i).replace("<rank>",
                                        Ranks.GetRank(pf.get(temp).getFame(), pf.get(temp).getSeconds())));
                            } else {
                                s.setLine(i, smfc.replace("<rank>",
                                        Ranks.GetRank(pf.get(temp).getFame(), pf.get(temp).getSeconds())));
                            }
                            break;
                        case FAME:
                            if (entro) {
                                s.setLine(i, s.getLine(i).replace("<fame>", String.valueOf(pf.get(temp).getFame())));
                            } else {
                                s.setLine(i, smfc.replace("<fame>", String.valueOf(pf.get(temp).getFame())));
                            }
                            break;
                        case POS:
                            if (entro) {
                                s.setLine(i, s.getLine(i).replace("<pos>", String.valueOf(temp + 1)));
                            } else {
                                s.setLine(i, smfc.replace("<pos>", String.valueOf(temp + 1)));
                            }
                            break;
                        case SERVER:
                            if (entro) {
                                s.setLine(i, s.getLine(i).replace("<server>", pf.get(temp).getServerName()));
                            } else {
                                s.setLine(i, smfc.replace("<server>", pf.get(temp).getServerName()));
                            }
                            break;
                        case WORLD:
                            if (entro) {
                                s.setLine(i, s.getLine(i).replace("<world>", pf.get(temp).getWorld()));
                            } else {
                                s.setLine(i, smfc.replace("<world>", pf.get(temp).getWorld()));
                            }
                            break;
                    }

                    temp++;
                }

                entro = true;

                argsValues.put(arg, value);
                argsBooleans.put(arg, true);
            }
        }
    }

    public boolean checkValues(ArrayList<PlayerFame> pf) {
        return (argsValues.get(ArgType.PLAYER) > 0 && pf.size() > argsValues.get(ArgType.PLAYER));
    }
}
