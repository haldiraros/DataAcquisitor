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
import org.apache.log4j.PropertyConfigurator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Juliusz
 */
abstract public class HubSessionManager implements Runnable {
    /**
     * Utworzenie loggera systemowego
     */
    private static final Logger lgr = LoggerFactory.getLogger(HubSessionManager.class);


     protected Hubs discoveredHubs;
     protected HubsSessions hubsSessions = new HubsSessions(10);
    
     static protected HubSessionManager hbs=null;

     
    public  void registerTestHub(Hub hub){
        HubConnection hc = HubConnection.createEmptyHubConnection(hub);
        getHubsSessions().put(hub.getHubHexId(), hc);
    }
    
    public  void unregisterTestHub(Hub hub){
        getHubsSessions().remove(hub.getHubHexId());
    }
            
    public  boolean isRegisteredForDCN(long regId) {
        HubsSessions hubsSess = getHubsSessions();

        Set<Map.Entry<String, HubConnection>> connectionSet= hubsSess.entrySet();
        Iterator<Map.Entry<String, HubConnection>> it = connectionSet.iterator();
        while(it.hasNext()){
            Map.Entry<String, HubConnection> pair= it.next();
            HubConnection hc= pair.getValue();
            long registredId = hc.getHub().getDCR().getRegId();
            if (regId==registredId)
                return true;
        }        
        return false;        
    }
    
    protected boolean runHubSessionManager=true;
    protected Thread hubSessionManagerThread=null;
    
         protected SessionInserters radioInserters=new SessionInserters(10);
//     protected SessionInserters flashInserters=new SessionInserters(10);


    protected HubSessionManager() throws MeteringSessionException {
        if (hbs==null)
            HubSessionManager.hbs=this;
        else
            throw new MeteringSessionException("Hub Session Manager already exists");
    }
    

            
     public void startHubSessionManager() throws MeteringSessionException    {
        Thread hbst= new Thread(hbs, "HubSessionDBManagerThread");
        hbs.setHubSessionManagerThread(hbst);
        hbst.start();
    }


     public void stopHubSessionManager() throws MeteringSessionException
    {
        if (hbs!=null){
            hbs.setRunHubSessionManager(false);
            hbs.getHubSessionManagerThread().interrupt();
            try {
                hbs.getHubSessionManagerThread().join();
            } catch (InterruptedException ex) {
                //ignore it
            }
            closeAllConnections();
            closeAllInserters();
            Set<Map.Entry<String, Hub>> hubsSet= discoveredHubs.entrySet();
            Iterator<Entry<String, Hub>> it = hubsSet.iterator();
            while(it.hasNext()){
                Entry<String, Hub> pair= it.next();
                Hub hub= pair.getValue();
                SessionDBInserterHelper.unregisterHub(hub);
            }            
            hbs=null;
        }
    }


    
     public Hubs discoverHubs(){
        discoveredHubs=HubConnection.discoverHubs(getHubsSessions());
        return getDiscoveredHubs();
    }
    
     public HubConnection connectHubAndStartRS(String hubNo, int timeout) throws MeteringSessionException{        
        Hub hub = getDiscoveredHubs().getHub(hubNo);
        HubConnection hc = HubConnection.createHubConnection(hub);
        getHubsSessions().put(hubNo, hc);
        hc.createRadioSession(timeout); 
        return hc;
    }
    
     public void closeHubSession(Hub hub){
        try {
            String hubNo = hub.getHubHexId();
            SessionInserter inserter = radioInserters.getInserter(hubNo);
            if (inserter!=null)
                inserter.close();
            radioInserters.removeInserter(hubNo);
            HubConnection hc = getHubsSessions().getHubConnection(hubNo);
            if (hc!=null)
                hc.close();
            getHubsSessions().remove(hubNo);
            SessionDBInserterHelper.unregisterHub(hub);
        } catch (MeteringSessionException ex) {
            //ignore it
        }
    }
    
     public void downloadMeasurmentsFromHub(String hubNo, Timestamp from) throws MeteringSessionException{
        HubConnection hc = getHubsSessions().getHubConnection(hubNo);
        hc.createHubFlashSession(from);
    }

     public int downloadMeasurmentsFromLogger(String hubNo, Timestamp from) throws MeteringSessionException{
        HubConnection hc = getHubsSessions().getHubConnection(hubNo);
        int newMeasurments = 0;
        hc.closeRadioSession();
        try{
            LoggerFlashSessionDBInserter loggerFlashSessionDBInserter = LoggerFlashSessionDBInserter.createLoggerFlashSessionDBInserter(hc, from);
            newMeasurments = loggerFlashSessionDBInserter.mainThread();                    
        }
        finally{
            hc.closeLoggerFlashSession();
            hc.createRadioSession(61); //TODO zmienić timeout
        }
        return newMeasurments;
    }
    /**
     * @return the discoveredHubs
     */
    public  Hubs getDiscoveredHubs() {
        return discoveredHubs;
    }

    /**
     * @return the hubsSessions
     */
    public  HubsSessions getHubsSessions() {
        return hubsSessions;
    }

    public  void closeAllConnections() {
        Set<Map.Entry<String, HubConnection>> connectionSet= getHubsSessions().entrySet();
        Iterator<Entry<String, HubConnection>> it = connectionSet.iterator();
        while(it.hasNext()){
            Entry<String, HubConnection> pair= it.next();
            HubConnection hc= pair.getValue();
            hc.close();
            it.remove();
//            getHubsSessions().remove(pair.getKey());
        }
lgr.debug("Time:"+System.nanoTime()+","+"is hubsSessionsMap empty "+getHubsSessions().isEmpty());
    }

    protected  void closeAllInserters() {
        Set<Map.Entry<String, SessionInserter>> insertersSet= radioInserters.entrySet();
        Iterator<Map.Entry<String, SessionInserter>> it = insertersSet.iterator();
        while(it.hasNext()){
            Map.Entry<String, SessionInserter> pair= it.next();
            SessionInserter ins= pair.getValue();
            try {
                ins.close();
            } catch (MeteringSessionException ex) {
                lgr.warn(null, ex);
            }
            it.remove();
//            getHubsSessions().remove(pair.getKey());
        }
 lgr.debug("Time:"+System.nanoTime()+","+"is radioInserters empty "+radioInserters.isEmpty());
    }    

    /**
     * @return the hubSessionManagerThread
     */
    public Thread getHubSessionManagerThread() {
        return hubSessionManagerThread;
    }

    /**
     * @param hubSessionManagerThread the hubSessionManagerThread to set
     */
    public void setHubSessionManagerThread(Thread hubSessionManagerThread) {
        this.hubSessionManagerThread = hubSessionManagerThread;
    }

    /**
     * @return the runHubSessionManager
     */
    synchronized public boolean isRunHubSessionManager() {
        return runHubSessionManager;
    }

    /**
     * @param runHubSessionManager the runHubSessionManager to set
     */
    synchronized public void setRunHubSessionManager(boolean runHubSessionManager) {
        this.runHubSessionManager = runHubSessionManager;
    }
    
    abstract protected  void startHubSessionAndRS(String hubNo) throws MeteringSessionException;

    
    protected  void startHubSessionAndRS(Hubs hubs) throws MeteringSessionException{
        Set<Map.Entry<String, Hub>> connectionSet= hubs.entrySet();
        Iterator<Entry<String, Hub>> it = connectionSet.iterator();
        while(it.hasNext()){
            Entry<String, Hub> pair= it.next();
            Hub hub= pair.getValue();
            startHubSessionAndRS(hub.getHubHexId());
        }
    }
    
   public   void addShutdownHook(){
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {try {
                    lgr.info("stopping hubSessionManager");
                    stopHubSessionManager();
                } catch (MeteringSessionException ex) {
                    lgr.warn(null, ex);
                }
              }
            });       
   }

}
