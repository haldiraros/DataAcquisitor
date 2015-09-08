/*
 * Copyright (C) 2015 Haros
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
package hubOperations;

import gnu.io.SerialPortEvent;
import hubLibrary.meteringcomreader.LoggerFlashSession;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Haros
 */
public class Tester {
    private static final Logger lgr = LoggerFactory.getLogger(Tester.class);
    
    public static void main(String[] args) throws MeteringSessionException{
        
        System.out.println("SerialPortEvent.DATA_AVAILABLE="+SerialPortEvent.DATA_AVAILABLE);
        System.out.println("SerialPortEvent.OUTPUT_BUFFER_EMPTY ="+SerialPortEvent.OUTPUT_BUFFER_EMPTY);
        
    }
    
}
