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


package test;

import gnu.io.SerialPort;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import static test.TestPerf.initComPort;
import static test.TestPerf.printTime;

/**
 *
 * @author Juliusz
 */
public class Reader {
    static public void main(String[] args) throws MeteringSessionException, IOException, InterruptedException{
        System.out.println(System.getProperty("java.library.path"));
        byte [] flashHubSession ={0x6a, 0x55, 0x30, 0x32, 0x30, 0x32, 0x46, 0x46, 0x46, 0x46, 0x46, 0x46, 0x46, 0x46};
        SerialPort port = initComPort("COM5");
        InputStream inputStream = port.getInputStream();
        
        long timer=System.nanoTime();          
        inputStream.read(flashHubSession);
        timer=printTime("Read:\t", timer);
        
        inputStream.close();
        port.close();
        
     }
}
