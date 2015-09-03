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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.logging.Level;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Juliusz
 */
public class SessionStreamInserter implements SessionInserter, Runnable{
     private static final Logger lgr = LoggerFactory.getLogger(SessionStreamInserter.class);
     protected boolean  shouldRun=true;
     protected Thread thread=null;
     protected HubConnection hc=null;
     protected ObjectOutputStream oos=null;

    public SessionStreamInserter(HubConnection hc, ObjectOutputStream oos) {
        this.hc=hc;
        this.oos=oos;
    }
             
    @Override
    public int mainThread() throws MeteringSessionException {
        setThread(new Thread(this, "radioSessionDBInserter for hub: "+hc.hub.getHubHexId()));
        getThread().start();
        return 0;
    }

    
    @Override
    public void run() {
        DataPacket dp;
        try {
            while (isShouldRun()) {
                dp = hc.getRadioSession().getNextPacket();
                try {
                    oos.writeObject(dp.generateDTO()); //write DataPacketDTO instance
                    oos.flush();
                } catch (IOException ex) {
                    lgr.warn("stream close problem:", ex);
                }
                lgr.info("Time:"+System.nanoTime()+","+dp);
            } 
        }catch (MeteringSessionException tout) {
            lgr.debug("Exception while processing new packet: "+tout.getMessage());            
        }
        finally{
            setThread(null);
            //TODO: close remaining resoureses
        }
lgr.debug("Time:"+System.nanoTime()+","+"Thread stoped: "+Thread.currentThread().getName());
    }
    
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
   @Override
    public void close() throws MeteringSessionException{
        setShouldRun(false);
        if (getThread()!=null)
            getThread().interrupt();
        try {
            oos.close();
        } catch (IOException ex) {
            throw new MeteringSessionException(ex);
        }
    }
}
