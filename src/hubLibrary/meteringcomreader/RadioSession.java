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


import hubLibrary.meteringcomreader.exceptions.MeteringSessionTimeoutException;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reprezentuje sesję odczytu danych z loggerów przesyłanych radiowo.
 * @author Juliusz Jezierski
 */
public class RadioSession extends MeteringSession implements Runnable{
    /**
     * Utworzenie loggera systemowego
     */
    private static final Logger lgr = LoggerFactory.getLogger(RadioSession.class);
    
    /**
     * Maksymalny czas bezczynności wyrażony w minutach, 
     * po którym koncentrator kończy sesję radiową.
     */
    protected int timeout;
    /**
     * Kontroluje działania wątku {@link #pinger}.
     */
    protected boolean run=true;
    /**
     * Wątek, który okresowo zamyka i ponownie otwiera sesję radiową,
     * w celu wydłużenia maksymalnego czasu bezczynności,
     * po którym koncentrator kończy sesję radiową.
     */
    protected Thread pinger = null;
    
    /**
     * Tworzy sesję radiową, ustalając maksymalny czas bezczynności na <code>timeout</code>
     * sekund, po którym koncentrator zamyka sesję.
     * @param hc obiekt połączenia z koncentratorem, w ramach którego jest tworzona
     * sesja
     * @param timeout maksymalny czas bezczynności wyrażony w minutach, 
     * po którym koncentrator kończy sesję radiową
     * @throws MeteringSessionException 
     */
    public RadioSession(HubConnection hc, int timeout) throws MeteringSessionException{
            super(hc);
            this.timeout=timeout;
            startRadioSession();
//            pinger=new Thread(this,"RadioSessionPinger for hub:"+hc.hub.getHubHexId()); //TODO: enable pinger
//            pinger.start();
            
    }
    
    /**
     * Startuje sesję radiową z maksymalnym czasem bezczynności
     * ustawionym na {@link #timeout}.
     * @throws MeteringSessionException 
     */
    protected void startRadioSession() throws MeteringSessionException{
            hc.sendCommand(Utils.startRadioSessionReq, (long) timeout, (byte)1);
            hc.receiveAck(Utils.startRadioSessionRes);    
    }

    /**
     * Kończy sesję radiową
     * @throws MeteringSessionException 
     */
    private void stopRadioSession() throws MeteringSessionException {
            hc.sendCommand(Utils.closeRadioSessionReq);
            hc.receiveAck(Utils.closeRadioSessionRes);
    }
    
    /**
     * Pobiera kolejny pakiet danych z ekspedytora {@link ComReadDispatch}.
     * @return kolejny pakiet danych pobrany z ekspedytora 
     * @throws MeteringSessionException 
     */
    @Override
    public DataPacket getNextPacket() throws MeteringSessionException{
        return hc.crd.getNextRSPacket();
       
    }

    /**
     * Niezaimplementowana.
     * @return UnsupportedOperationException
     */
    @Override
    public DataPacket regetPrevPacket() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Zamyka bieżącą sesję radiową.
     * @throws MeteringSessionException 
     */
    @Override
    public void close() throws MeteringSessionException{
        setRun(false);
        if (getPinger()!=null)
            getPinger().interrupt();
        try{
            stopRadioSession();
        }
        catch (MeteringSessionTimeoutException ex){
            //ignore it
        }
    }

    /**
     * Implementacja metody run wątku, który okresowo zamyka i ponownie otwiera sesję radiową,
     * w celu wydłużenia maksymalnego czasu bezczynności,
     * po którym koncentrator kończy sesję radiową.     */
    @Override
    public void run() {
        while(isRun()){
            try {
                Thread.sleep(timeout*1000*60-1000*25);
            } catch (InterruptedException ex) {
                //ignore it
            }
            if (isRun()){
                try {
                    stopRadioSession();
                } catch (MeteringSessionException ex) {
                    lgr.warn(null, ex);
                }
                try {
                    startRadioSession();
                } catch (MeteringSessionException ex) {
                    lgr.warn(null, ex);
                }                
            }
        }
        
        
        lgr.debug("Time:"+System.nanoTime()+","+"Thread stoped: "+Thread.currentThread().getName());
    }

    /**
     * @return the pinger
     */
    public Thread getPinger() {
        return pinger;
    }

    /**
     * @param pinger the pinger to set
     */
    public void setPinger(Thread pinger) {
        this.pinger = pinger;
    }

    /**
     * @return the run
     */
    synchronized public boolean isRun() {
        return run;
    }

    /**
     * @param run the run to set
     */
    synchronized public void setRun(boolean run) {
        this.run = run;
    }

}
