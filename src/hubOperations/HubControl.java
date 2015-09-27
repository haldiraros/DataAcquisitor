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

import hubGui.i18n.Resources;
import hubGui.logging.LogTyps;
import hubLibrary.meteringcomreader.DataPacket;
import hubLibrary.meteringcomreader.DataPacketDTO;
import hubLibrary.meteringcomreader.Hub;
import hubLibrary.meteringcomreader.HubConnection;
import hubLibrary.meteringcomreader.HubFlashSession;
import hubLibrary.meteringcomreader.HubSessionDBManager;
import hubLibrary.meteringcomreader.Hubs;
import hubLibrary.meteringcomreader.LoggerFlashSession;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import static hubOperations.RadioSessionReciever.createRadioSessionReciever;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import project.data.Datagram;
import project.data.Measurement;
import project.data.Session;

/**
 *
 * @author Haros
 */
public class HubControl {

    HubConnection hubConn = null;
    Hub hub = null;
    RadioSessionReciever RSRecv = null;

    Session dbSession = null;

    public Session getDbSession() {
        return dbSession;
    }

    public void setDbSession(Session dbSession) {
        this.dbSession = dbSession;
    }

    /**
     * Konstruktor klasy hub wykorzystującej klasę Hub z biblioteki
     *
     * @param hubId identyfikator koncentratora
     * @param comPortName nazwa portu, do którego podłączony jest koncentrator
     */
    public HubControl(long hubId, String comPortName) {
        this.hub = new Hub(hubId, comPortName);
    }

    /**
     * Konstruktor klasy hub wykorzystującej klasę Hub z biblioteki
     *
     * @param hexHubId heksadecymalny identyfikator koncentratora
     * @param comPortName nazwa portu, do którego podłączony jest koncentrator
     */
    public HubControl(String hexHubId, String comPortName) {
        this.hub = new Hub(hexHubId, comPortName);
    }

    public HubControl() throws MeteringSessionException {
        Hubs hubs = HubSessionDBManager.getHubSessionManager().discoverHubs();
        Iterator<Map.Entry<String, Hub>> it = hubs.entrySet().iterator();
        if (it.hasNext()) {
            Map.Entry<String, Hub> pairs = (Map.Entry<String, Hub>) it.next();
            this.hub = pairs.getValue();

        } else {
            throw new MeteringSessionException("nie znaleziono żadnego huba");
        }
    }

    public Hub getHub() {
        return hub;
    }

    public String getHubId() {
        return hub.getHubHexId().substring(4); //TODO: Skasować pierwsze 4 bity
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

    public void openHubConn() throws MeteringSessionException {
        try {
            hubConn = HubConnection.createHubConnection(hub);
        } catch (Exception e) {
            System.out.println("error opening connection " + e.getMessage());
        }
    }

    public void closeHubConn() {
        if (hubConn != null) {
            System.out.print("closing hubConn");
            hubConn.close();
        }
    }
    public int getHubFirmVer() throws MeteringSessionException{
        return hubConn.getHubFirmawareVersion();
    }
    
    public int getHubHardVer() throws MeteringSessionException{
        return hubConn.getHubHardwareVersion();
    }
    
    public long[] getRegisteredLoggersList() {
        long[] registeredLoggers = null;
        try {
            registeredLoggers = hubConn.getRegistredLoggers();
        } catch (Exception e) {
            System.out.println("error listing loggers " + e.getMessage());
        } finally {
            return registeredLoggers;
        }
    }

    public long registerNewLogger(long loggerID) throws MeteringSessionException {
        long out = -1;
        out = hubConn.registerLogger(loggerID);
        return out;
    }

    public void unregisterLogger(long registeredLoggerID) throws MeteringSessionException {
        hubConn.unregisterLogger(registeredLoggerID);

    }

    public int unregisterAllLoggers() {
        int removalCount = 0;
        long[] currentLoggers = getRegisteredLoggersList();
        for (int i = 0; i < currentLoggers.length; i++) {
            System.out.println("Removing logger:0x" + Long.toHexString(currentLoggers[i]));
            try {
                unregisterLogger(currentLoggers[i]);
            } catch (MeteringSessionException ex) {
                ;
            }
            removalCount++;
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
        return removalCount;
    }

    public void closeAllSessions() {
        try {
            hubConn.closeAllSessions();
        } catch (Exception e) {
            System.out.println("error closing sessions " + e.getMessage());
        }
    }

    public long checkLoggerID() throws MeteringSessionException {
        long loggerID = -1;
        loggerID = hubConn.getLoggerId();
        return loggerID;

    }

    public long autoRegisterLogger() throws MeteringSessionException {
        long logID = -1;
        logID = checkLoggerID();
        //if(logID)
        hubConn.registerLogger(logID);
        return logID;
    }

    public void processDataPacketEncoded(DataPacket pck) throws Exception {
        //hubGui.logging.Logger.write(Resources.getFormatString("msg.hubControl.dataPacket", DatatypeConverter.printHexBinary(pck.getOrgData()))); //TODO: Remove later      
        String time = String.format("%0#8X", (long) (new Date().getTime() / 1000));
        dbSession.addDatagram(new Datagram(DatatypeConverter.printHexBinary(pck.getOrgData()), getHubId(), time));
    }

    public void processDataPacketTemps(DataPacket pck) throws Exception {       
        DataPacketDTO test = pck.generateDTO();
        String logID = pck.getLoggerHexId();
        logID = logID.substring(4);  //usunąć pierwsze 4 znaki
        String time = String.format("%0#8X", (long) ((test.getMeasurmentTimeStart().getTime()) / 1000));
        dbSession.addMeasurement(new Measurement(logID, getHubId(), time, test.getTemperatures(), test.getMeasurmentPeriod()));

    }

    public void readPacketsLoggerFlash() throws MeteringSessionException {
        DataPacket packet = null;

        LoggerFlashSession loggerFlashSession = hubConn.createLoggerFlashSession(new Timestamp(0));

        while ((packet = loggerFlashSession.getNextPacket(100000)) != null) {
            try {
                processDataPacketTemps(packet);
            } catch (Exception ex) {
                Logger.getLogger(HubControl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        hubConn.closeLoggerFlashSession();
    }

    public void readPacketsHubFlash() throws MeteringSessionException {
        DataPacket packet = null;
        int packetCount = 0;
        try {
            HubFlashSession hubFlashSession = hubConn.createHubFlashSession(0xFFFFFFFF);
            System.out.println("Session started");
            while ((packet = hubFlashSession.getPrevPacket()) != null) {
                System.out.println("packetrecvd");
                processDataPacketEncoded(packet);
                packetCount++;
                if (packetCount == 10000) {
                    break;
                }
            }
            System.out.println("nopckg");
        } catch (MeteringSessionException e) {
            System.out.println(e.getMessage());
            throw e;

        } catch (Exception ex) {
            hubGui.logging.Logger.write(Resources.getString("msg.hubControl.errorOnPacketProcessing"), LogTyps.ERROR);
        } finally {
            try {
                hubConn.closeHubFlashSession();
            } catch (Exception e) {
                System.out.println("Error closing Hub Flash Session " + e.getMessage());
            }
        }
    }

    public void startRecievingInRadioSession() throws MeteringSessionException {
        // try{
        hubConn.createRadioSession(0);
        RSRecv = createRadioSessionReciever(this);
        System.out.println("Started radio session recieving ");
        RSRecv.mainThread();

        //}catch(Exception e){System.out.println("Error in radio session "+e.getMessage());}
    }

    public void stopRecievingInRadioSession() throws MeteringSessionException {
        if (RSRecv != null) {
            RSRecv.close();
            hubConn.closeRadioSession();
        }
        RSRecv = null;
        System.out.println("Stopped radio session recieving ");
    }

    public void closeAll() {
        try {
            stopRecievingInRadioSession();
        } catch (MeteringSessionException ex) {
            Logger.getLogger(HubControl.class.getName()).log(Level.SEVERE, null, ex);
        }
        closeAllSessions();
        closeHubConn();
    }

    public void restartAll() throws MeteringSessionException {
        boolean flag = false;
        if (RSRecv != null) {
            flag = !flag;

            try {
                stopRecievingInRadioSession();
            } catch (MeteringSessionException ex) {
                Logger.getLogger(HubControl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        closeAllSessions();
        closeHubConn();
        openHubConn();
        if (flag) {
            startRecievingInRadioSession();
        }

    }
}
