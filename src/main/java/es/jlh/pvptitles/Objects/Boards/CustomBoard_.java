package es.jlh.pvptitles.Objects.Boards;

import es.jlh.pvptitles.Objects.PlayerFame;
import java.util.List;

/**
 *
 * @author AlternaCraft
 */
public interface CustomBoard_ {
    public boolean isMaterializable(short jugadores);
    public void materialize(List<PlayerFame> pf);
    public void dematerialize(short jugadores);
}
