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

import hubLibrary.meteringcomreader.DataPacket;
import hubLibrary.meteringcomreader.Hub;
import hubLibrary.meteringcomreader.HubConnection;
import hubLibrary.meteringcomreader.HubFlashSession;
import hubLibrary.meteringcomreader.HubSessionDBManager;
import hubLibrary.meteringcomreader.Hubs;
import hubLibrary.meteringcomreader.LoggerFlashSession;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import static hubOperations.RadioSessionReciever.createRadioSessionReciever;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Haros
 */
public class HubControl {

   
    HubConnection hubConn=null;
    Hub hub =null;
    RadioSessionReciever RSRecv=null;

    /**
     * Konstruktor klasy hub wykorzystującej klasę Hub z biblioteki
     * @param hubId  identyfikator koncentratora
     * @param comPortName nazwa portu, do którego podłączony jest koncentrator
     */
    public HubControl(long hubId, String comPortName){
        this.hub= new Hub(hubId,comPortName);
    }
    /**
     * Konstruktor klasy hub wykorzystującej klasę Hub z biblioteki
     * @param hexHubId heksadecymalny identyfikator koncentratora
     * @param comPortName nazwa portu, do którego podłączony jest koncentrator
     */
    public HubControl(String hexHubId, String comPortName){
        this.hub= new Hub(hexHubId,comPortName);
    }
    
    public HubControl() throws MeteringSessionException{
        Hubs hubs = HubSessionDBManager.getHubSessionManager().discoverHubs();
        Iterator<Map.Entry<String, Hub>> it =hubs.entrySet().iterator();
            if (it.hasNext()) {
                Map.Entry<String, Hub> pairs = (Map.Entry<String, Hub>)it.next();
                this.hub = pairs.getValue();

            }
            else{
                throw new MeteringSessionException("nie znaleziono żadnego huba");
            }
    }
    public Hub getHub() {
        return hub;
    }
    

    public void setHub(Hub hub) {
        this.hub = hub;
    }
    
    public HubConnection getHubConn() {
        return hubConn;
    }

    public void setHubConn(HubConnection hubConn) {
        this.hubConn = hubConn;
    }
    
    public void openHubConn() throws MeteringSessionException{
        try{
         hubConn = HubConnection.createHubConnection(hub);
        }catch(Exception e){
             System.out.println("error opening connection "+e.getMessage());
         }
    }
    
    public void closeHubConn(){
        if (hubConn!=null){
             System.out.print("closing hubConn");
                               hubConn.close();
           }
    }
    
    public long[] getRegisteredLoggersList(){
        long[] registeredLoggers= null;
        try{
            registeredLoggers = hubConn.getRegistredLoggers();
        }catch(Exception e){
             System.out.println("error listing loggers "+e.getMessage());
        }
        finally{
            return registeredLoggers;
        }
    }
    
    public void registerNewLogger(long loggerID){
        try{
            long registerLogger = hubConn.registerLogger(loggerID);
            }catch(Exception e){
             System.out.println("error registering loggers "+e.getMessage());
        }
    }
    public void unregisterLogger(long registeredLoggerID){
        try{
             hubConn.unregisterLogger(registeredLoggerID);
            }catch(Exception e){
             System.out.println("error unregistering loggers "+e.getMessage());
        }
    }
    
    public int unregisterAllLoggers(){
        int removalCount =0;
        long[] currentLoggers =getRegisteredLoggersList();
        for (int i=0; i<currentLoggers.length; i++){
            System.out.println("Removing logger:0x"+Long.toHexString(currentLoggers[i]));
            unregisterLogger(currentLoggers[i]);
            removalCount++;
            try{
            Thread.sleep(10000);
            }catch(Exception e){}
        }
        return removalCount;
    }
    
    public void closeAllSesssions(){
        try{
            hubConn.closeAllSessions();
        }catch(Exception e)
        {
            System.out.println("error closing sessions "+e.getMessage());
        }
    }
      
    public long checkLoggerID(){
        long loggerID=-1;
        try{
            loggerID= hubConn.getLoggerId();
        }catch(Exception e){
            System.out.println("error getting LoggerId "+e.getMessage());
        }
        return loggerID;
        
    }
    
    public boolean autoRegisterLogger(){
        long logID = -1;
        try{
            logID = checkLoggerID();
            hubConn.registerLogger(logID);
        }catch(Exception e){
            System.out.println("Error on Logger autoregister "+e.getMessage());
            return false;
        }
        return logID!=-1 ? true : false ;
    }
    
    //TODO: Somehow have radio on idle loop or sth that can be broken when needed...
    public void processDataPacketEncoded (DataPacket pck){ //TODO: all the packet processing!!
        System.out.println("Paczka danych: " +DatatypeConverter.printHexBinary(pck.getOrgData()));
        System.out.println(pck);
        
    }
    public void processDataPacketTemps (DataPacket pck){ //TODO: all the packet processing!!
        System.out.println(pck);
        //System.out.println("Paczka danych: " +DatatypeConverter.printHexBinary(pck.getOrgData()));
        
    }
    
    public void readPacketsLoggerFlash() throws MeteringSessionException{
        DataPacket packet=null;
        try{
            LoggerFlashSession loggerFlashSession = hubConn.createLoggerFlashSession(new Timestamp(0));
            while ((packet = loggerFlashSession.getNextPacket(100000))!=null){
                processDataPacketTemps(packet);
                }
        }finally{
            try{
                if(hubConn!=null) hubConn.closeLoggerFlashSession();
            }catch(Exception e){
                System.out.println("Error closing Logger Flash Session "+e.getMessage());
            }
        }
    }
    
    public void readPacketsHubFlash() throws MeteringSessionException{
        DataPacket packet=null;
        int packetCount =0;
        try{
            HubFlashSession hubFlashSession = hubConn.createHubFlashSession(0xFFFFFFFF);
            System.out.println("Session started");
            while ((packet = hubFlashSession.getPrevPacket())!=null){
                System.out.println("packetrecvd");
                processDataPacketEncoded(packet);
                packetCount++;
                if (packetCount==10000)
                    break;
            }
            System.out.println("nopckg");
        }catch(Exception e){
            System.out.println(e.getMessage());
        
        }finally{
            try{
                hubConn.closeHubFlashSession();
            }catch(Exception e){
                System.out.println("Error closing Hub Flash Session "+e.getMessage());
            }
        }
    }
    
    public void startRecievingInRadioSession() throws MeteringSessionException{
      // try{
           hubConn.createRadioSession(0);
           RSRecv = createRadioSessionReciever(this);
           System.out.println("Started radio session recieving ");
           RSRecv.mainThread();
           
       //}catch(Exception e){System.out.println("Error in radio session "+e.getMessage());}
    }
    
    public void stopRecievingInRadioSession() throws MeteringSessionException{
        if(RSRecv!=null){
            RSRecv.close();
            hubConn.closeRadioSession();
        }
        RSRecv = null;
        System.out.println("Stopped radio session recieving ");
    }
}
