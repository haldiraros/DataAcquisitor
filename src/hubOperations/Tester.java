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
import hubLibrary.meteringcomreader.TestFlashSession;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Haros
 */
public class Tester {
    private static final Logger lgr = LoggerFactory.getLogger(Tester.class);
    
    public static void main(String[] args) throws MeteringSessionException,InterruptedException, Exception{
        PropertyConfigurator.configure(Tester.class.getResource("log4j.properties"));
        
        System.out.println("SerialPortEvent.DATA_AVAILABLE="+SerialPortEvent.DATA_AVAILABLE);
        System.out.println("SerialPortEvent.OUTPUT_BUFFER_EMPTY ="+SerialPortEvent.OUTPUT_BUFFER_EMPTY);
        
        HubControl hubC = new HubControl(0, "COM5");
        hubC.openHubConn();
        
        long[] listLoggers1= hubC.getRegisteredLoggersList();
        if(listLoggers1!=null)
            System.out.println("Regstred loggers count:"+listLoggers1.length);
        for (int i=0; i<listLoggers1.length; i++){
            System.out.println("Regstred logger:0x"+Long.toHexString(listLoggers1[i])); 
        }
         /* Logger registering unregistering test */
        /*
        //hubC.unregisterAllLoggers();
        //System.out.println(Long.toHexString(hubC.checkLoggerID()));
        //hubC.autoRegisterLogger();
        long[] listLoggers2= hubC.getRegisteredLoggersList();
        if(listLoggers2!=null)
            System.out.println("Regstred loggers count:"+listLoggers2.length);
        for (int i=0; i<listLoggers2.length; i++){
            System.out.println("Regstred logger:0x"+Long.toHexString(listLoggers2[i])); 
        }
        */
        
        //hubC.readPacketsHubFlash();
        //hubC.readPacketsLoggerFlash();
        
        hubC.closeHubConn();
        
    }
    
}
