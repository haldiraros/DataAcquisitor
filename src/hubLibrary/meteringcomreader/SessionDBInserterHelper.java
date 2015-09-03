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

import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import java.sql.*;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import hubLibrary.meteringcomreader.callback.DBChangeNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Pomocnicza klasa do wykonywania operacji na bazie danych.
 * Jej abstrakcyjność ma uniemożliwić tworzenie jej wystąpień.
 * @author Juliusz Jezierski
 */
abstract public class SessionDBInserterHelper {
    /**
     * Utworzenie loggera systemowego
     */
    private static final Logger lgr = LoggerFactory.getLogger(SessionDBInserterHelper.class);
    
    static private PreparedStatement registerHubPS=null;
    static private PreparedStatement unregisterHubPS=null;
    static private PreparedStatement unregisterAllHubsPS=null;

    /**
     * Tekst polecenia SQL służącego do rejestrowania koncentratora w bazie danych.
     */
    protected static String registerHubSQL="{call metering_hub_session.register_hub(?,?,?)}";            
    /**
     * Tekst polecenia SQL służącego do wyrejestrowania koncentratora z bazy danych.
     */
    protected static String unregisterHubSQL="{call metering_hub_session.unregister_hub(?,?)}";
    /**
     * Tekst polecenia SQL służącego do wyrejestrowania wszystkich koncentratorów z bazy danych.
     */
    protected static String unregisterAllHubsSQL="{call metering_hub_session.unregister_all_hubs(?)}";
    
//    protected static Connection conn;


    /**
     * Rejestruje w bazie danych koncentratory opisane w kontenerze <code>hubs</code>.
     * @param hubs kontener rejestrowanych koncentratorów. 
     * @throws MeteringSessionException zgłaszany w przypadku błędu operacji na bazie danych
     */
    static public void registerHubs(Hubs hubs) throws MeteringSessionException{            
        for (Map.Entry<String, Hub> pairs : hubs.entrySet()){
            Hub hub = pairs.getValue();
            registerHub(hub);
        }
    }
      

    /**
     * Rejestruje w bazie danych koncentrator <code>hub</code>.
     * @param hub rejestrowany w bazie danych koncentrator.
     * @throws MeteringSessionException 
     */
   static public void registerHub(Hub hub) throws MeteringSessionException{
       Connection conn = null;
        try{
            conn = DBUtils.createDBConnection();
            DBChangeNotification.registerForCallback(hub, conn);
            registerHubPS=conn.prepareCall(registerHubSQL);
            String hubid=hub.getHubHexId();
            registerHubPS.setString(1, hubid);
            registerHubPS.setString(2, hub.getComPortName());
            registerHubPS.setString(3, "connected");            
            registerHubPS.execute();
            conn.commit();
            registerHubPS.close();
            registerHubPS=null;
            boolean reverseConnStatus = DBChangeNotification.testReverseConnection(hub, conn);
            if (!reverseConnStatus)
                lgr.warn("Reverse connection NOT established");
            else
                lgr.debug("Reverse connection established");
        }
        catch (SQLException ex) {
            int errCode=ex.getErrorCode();
            if (errCode==1){ //ORA-00001: unique constraint (METER.MEASUSER_SESSION_PK) violated
                ; //ignore it
            }
            else {
                throw new MeteringSessionException(ex);
            }    
        }
        finally{
            try {
                if(conn!=null)
                    conn.close();
            } catch (SQLException ex) {
                lgr.warn(null, ex);
            }
        }
    }
            
    
    /**
     * Wyrejestrowuje z bazy danych dany koncentrator <code>hub</code>.
     * @param hubid wyrejestrowywany koncentrator.
     * @throws MeteringSessionException  zgłaszany w przypadku błędu operacji na bazie danych
     */
    static public void unregisterHub(Hub hub)throws MeteringSessionException{
         String hubid=hub.getHubHexId();
         Connection conn = null;
         try{
            conn = DBUtils.createDBConnection();

            unregisterHubPS=conn.prepareCall(unregisterHubSQL);
            unregisterHubPS.setString(1, hubid);
            unregisterHubPS.setString(2, hubid);
            unregisterHubPS.execute();
            conn.commit();
            unregisterHubPS.close();
            unregisterHubPS=null;
            DBChangeNotification.unregisterForCallback(hub, conn);
            lgr.debug("Hub 0x"+hubid+" unregistered.");
         }
        catch (SQLException ex) {
             throw new MeteringSessionException(ex);
         }
         finally{
            try {
                if(conn!=null)
                    conn.close();
            } catch (SQLException ex) {
                lgr.warn(null, ex);
            }
        }
    }
    
}
