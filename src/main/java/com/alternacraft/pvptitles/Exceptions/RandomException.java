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

import java.util.Map;

public class RandomException extends CustomException {

    private static final String PREFIX = "00x";
    
    public RandomException(String message) {
        super(message);
    }
    
    public RandomException(String message, Map<String, Object> data) {
        super(message, data);
    }

    public RandomException(String message, String custom_error) {
        super(message, custom_error);
    }

    @Override
    protected String getHeader() {
        return new StringBuilder()
                .append("Error ")
                .append(this.getMessage()).toString();
    }

    @Override
    protected String getPossibleReasons() {
        return "";
    }

}
