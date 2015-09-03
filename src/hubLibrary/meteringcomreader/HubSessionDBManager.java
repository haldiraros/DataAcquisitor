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


package hubLibrary.meteringcomreader;

import hubLibrary.meteringcomreader.exceptions.MeteringSessionSPException;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import org.apache.log4j.PropertyConfigurator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Juliusz
 */
public class HubSessionDBManager extends HubSessionManager {
    /**
     * Utworzenie loggera systemowego
     */
    private static final Logger lgr = LoggerFactory.getLogger(HubSessionDBManager.class);




    protected HubSessionDBManager() throws MeteringSessionException {
        super();        
    }
    

            
     public static HubSessionDBManager getHubSessionManager(){
         if (hbs==null)
             try {
             hbs=new HubSessionDBManager();
         } catch (MeteringSessionException ex) {
             throw new RuntimeException(ex);
         }
         return (HubSessionDBManager)hbs;
     }


    @Override
    public void run() {
        while(isRunHubSessionManager()){
            
            try{
                Hubs hubs = discoverHubs();
                startHubSessionAndRS(hubs);
                hubLibrary.meteringcomreader.SessionDBInserterHelper.registerHubs(hubs);
                try {
                    Thread.sleep(1000*20);
                } catch (InterruptedException ex) {
                    //ignore it;
                }
            } catch(MeteringSessionSPException ex){
                    lgr.warn(null, ex);
            } catch(MeteringSessionException ex){
                    lgr.warn(null, ex);
            }
        }
      
    }

    
    @Override
    protected  void startHubSessionAndRS(String hubNo) throws MeteringSessionException{
        HubConnection hc = connectHubAndStartRS(hubNo, 61); //TODO 1)przekazać parametr do wywołania(?), 2)zmienić timeout
        RadioSessionDBInserter sessionInserter = RadioSessionDBInserter.createRadioSessionDBInserter(hc);        
        sessionInserter.mainThread(); 
        radioInserters.addInserter(hubNo, sessionInserter);
    }
        
    public static void main(String args[]) throws MeteringSessionException, InterruptedException{
        
     PropertyConfigurator.configure(HubSessionDBManager.class.getResource("log4j.properties"));
        
        
        HubSessionManager hbs= HubSessionDBManager.getHubSessionManager();
        hbs.startHubSessionManager();
        hbs.addShutdownHook();
//        Thread.sleep(1000*60);

//        HubSessionManager.downloadMeasurmentsFromLogger("4D4503000000", null);
        
//        Thread.sleep(1000*60*300);
        

    }
}
