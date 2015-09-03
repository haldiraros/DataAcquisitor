/*
 * Copyright (C) 2015 Juliusz Jezierski
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

package hubLibrary.meteringcomreader.exceptions;

/**
    * Bład wewnętrzny koncentratora, 
 * bliżej nie zidentyfikowana przyczyna, uzyty zawsze w przypadku nie wybrania 
	odpowiedniej opcji switch->case, kod 5
 * @author Juliusz Jezierski
 */
public class MeteringSessionHubInternalError extends MeteringSessionException{
    public MeteringSessionHubInternalError(){
        this (I18nHelper.getI18nMessage("HubInternalError"));                
    }
    public MeteringSessionHubInternalError(String msg){
        super(msg);
    }
        public MeteringSessionHubInternalError(Throwable ex){
        super(ex);
    }
}
