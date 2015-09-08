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
package hub;

import hubLibrary.meteringcomreader.Hub;
import hubLibrary.meteringcomreader.HubConnection;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;

/**
 *
 * @author Haros
 */
public class hub {

   
    HubConnection hubConn=null;
    Hub hub =null;

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
    
    public void readPacketsLoggerFlash(){
        
    }
    
    public void readPacketsHubFlash(){
        
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
    
    //TODO: Session starters, or rather start and read right away aside from radio...
    //TODO: Somehow have radio on idle loop or sth that can be broken when needed...
    
}
