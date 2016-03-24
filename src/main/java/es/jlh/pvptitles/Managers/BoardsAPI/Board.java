package es.jlh.pvptitles.Managers.BoardsAPI;

import es.jlh.pvptitles.Objects.PlayerFame;
import java.util.List;

/**
 *
 * <b>Limitaciones del objeto</b>
 * <ul>
 *  <li>
 *      El último bloque es el de las variables, y en el mismo, sólo se 
 *      puede usar la primera fila de cada columna.
 *  </li>
 *  <li>No se puede combinar normal + progresivo</li>
 * </ul>
 * 
 * @author AlternaCraft
 */
public abstract class Board {

    protected BoardData info = null;
    protected BoardModel model = null;
    protected ModelController modelController = null;

    public Board(BoardData info, BoardModel bm, ModelController model) {
        this.info = info;
        this.model = bm;
        this.modelController = model;
    }

    /**
     * Método para comprobar si se puede crear un board
     * @param jugadores
     * @return 
     */
    public abstract boolean isMaterializable(short jugadores);
    
    /**
     * Método para crear un board
     * @param pf 
     */
    public abstract void materialize(List<PlayerFame> pf);
    
    /**
     * Método para borrar un board
     * @param jugadores 
     */
    public abstract void dematerialize(short jugadores);
    
    /**
     * Devuelve la información del board
     * @return 
     */
    public abstract BoardData getData();
    
    /**
     * Devuelve el modelo del board
     * @return 
     */
    public abstract BoardModel getModel();
    
    /**
     * Devuelve el controlador del modelo
     * @return 
     */
    public abstract ModelController getModelController();
    
}
