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

import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;

/**
 * Wyjątek zgłaszany w przypadku niepowodzenia wykonania operacji
 * wejścia-wyjścia na strumieniu  służącemu do odbierania danych 
 * z połączenia z koncentratorem (SP - serial port).
 * @author Juliusz Jezierski
 */
public class MeteringSessionSPException extends MeteringSessionException{
    

    public MeteringSessionSPException(Throwable ex) {
        super(ex);
    }

    public MeteringSessionSPException(String msg) {
        super(msg);
    }


    
}
