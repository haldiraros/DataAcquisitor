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
 * Polecenie nieznane, nie ma tym urządzeniu takiego rozkazu, kod 7
 * @author Juliusz Jezierski
 */
public class MeteringSessionUnknowCommand extends MeteringSessionException{
    public MeteringSessionUnknowCommand(){
        this (I18nHelper.getI18nMessage("UnknowCommand"));                    
    }
        public MeteringSessionUnknowCommand(String msg){
        super(msg);
    }
        public MeteringSessionUnknowCommand(Throwable ex){
        super(ex);
    }
    
}
