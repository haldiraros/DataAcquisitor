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
import hubLibrary.meteringcomreader.HubConnection;
import hubLibrary.meteringcomreader.MeteringSession;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Haros
 */
public class RadioSessionReciever implements Runnable{

    /**
     * Utworzenie loggera systemowego
     */
    private static final Logger lgr = LoggerFactory.getLogger(RadioSessionReciever.class);
    
     protected boolean  shouldRun=true;
     protected Thread thread=null;
     protected HubControl hubContr = null;
     protected MeteringSession metSess = null;
     
    public RadioSessionReciever(HubControl hCon) throws MeteringSessionException {
        this.hubContr=hCon;
    }
    
    
    static public RadioSessionReciever createRadioSessionReciever(HubControl hCon) throws MeteringSessionException {
        RadioSessionReciever RSRcv = new RadioSessionReciever(hCon);
        RSRcv.metSess = RSRcv.hubContr.getHubConn().getRadioSession();
        return RSRcv;
    }

    public int mainThread() throws MeteringSessionException {
        setThread(new Thread(this, "RadioSessionReciever for hub: "+hubContr.getHub().getHubHexId()));
        getThread().start();
        return 0;
    }

    @Override
    public void run() {
    lgr.debug("Time:"+System.nanoTime()+","+"Thread started: "+Thread.currentThread().getName());
        DataPacket dp;
        try {
            while (isShouldRun()) {
                dp = metSess.getNextPacket();
                hubContr.processDataPacketEncoded(dp);
                lgr.info("Time:"+System.nanoTime()+","+dp);
            } 
        }catch (MeteringSessionException tout) {
            lgr.debug("Exception while processing new packet: "+tout.getMessage());            
        } catch (Exception ex) {
            hubGui.logging.Logger.write(Resources.getString("msg.radioSessionReceiver.errorOnPacketProcessing"), LogTyps.ERROR);
        }
        finally{
            setThread(null);
            //TODO: close remaining resoureses
        }
    lgr.debug("Time:"+System.nanoTime()+","+"Thread stoped: "+Thread.currentThread().getName());
    }
    
    public void close() throws MeteringSessionException{
        setShouldRun(false);
        if (getThread()!=null)
            getThread().interrupt();
    }

    /**
     * @return the shouldRun
     */
    synchronized protected boolean isShouldRun() {
        return shouldRun;
    }

    /**
     * @param shouldRun the shouldRun to set
     */
    synchronized protected void setShouldRun(boolean shouldRun) {
        this.shouldRun = shouldRun;
    }

    /**
     * @return the thread
     */
    synchronized public Thread getThread() {
        return thread;
    }

    /**
     * @param thread the thread to set
     */
    synchronized public void setThread(Thread thread) {
        this.thread = thread;
    }

}
