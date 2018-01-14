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
package com.alternacraft.pvptitles.Managers.BoardsAPI;

import com.alternacraft.pvptitles.Misc.PlayerFame;
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
     * 
     * @param jugadores short
     * 
     * @return Boolean
     */
    public abstract boolean isMaterializable(short jugadores);
    
    /**
     * Método para crear un board
     * @param pf List
     */
    public abstract void materialize(List<PlayerFame> pf);
    
    /**
     * Método para borrar un board
     * 
     * @param jugadores short
     */
    public abstract void dematerialize(short jugadores);
    
    /**
     * Devuelve la información del board
     * 
     * @return BoardData
     */
    public abstract BoardData getData();
    
    /**
     * Devuelve el modelo del board
     * 
     * @return BoardModel
     */
    public abstract BoardModel getModel();
    
    /**
     * Devuelve el controlador del modelo
     * 
     * @return ModelController
     */
    public abstract ModelController getModelController();
    
}
