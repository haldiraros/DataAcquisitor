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

package hubLibrary.meteringcomreader.callback;



import hubLibrary.meteringcomreader.Utils;
import hubLibrary.meteringcomreader.HubFlashSessionDBInserter;
import hubLibrary.meteringcomreader.HubSessionDBManager;
import hubLibrary.meteringcomreader.HubConnection;
import hubLibrary.meteringcomreader.ComReadDispatch;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import java.util.Date;
import java.sql.Timestamp;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Juliusz
 */
public class HubSessionService {
      /**
     * Utworzenie loggera systemowego
     */
    private static final org.slf4j.Logger lgr = LoggerFactory.getLogger(ComReadDispatch.class);


    public static void downloadMeasurmentsFromHub(String hubNo, Date from) throws MeteringSessionException {
        try{
        Timestamp time = new Timestamp(from.getTime());
        HubSessionDBManager.getHubSessionManager().downloadMeasurmentsFromHub(hubNo, time);
        HubConnection hc = HubSessionDBManager.getHubSessionManager().getHubsSessions().getHubConnection(hubNo);
        HubFlashSessionDBInserter inserter= HubFlashSessionDBInserter.createHubFlashSessionDBInserter(hc, time);
        inserter.mainThread();
        }catch (Throwable ex){
            throw new MeteringSessionException(ex.getMessage());
        }
        
    }

    public static int  downloadMeasurmentsFromLogger(String hubNo, Date from) throws MeteringSessionException {
//        try{
        Timestamp time;
        if (from != null)
            time = new Timestamp(from.getTime());
        else
            time=null;
        int newMeasuments = HubSessionDBManager.getHubSessionManager().downloadMeasurmentsFromLogger(hubNo, time);
/*        
        HubConnection hc = HubSessionManager.getHubsSessions().getHubConnection(hubNo);
        LoggerFlashSessionDBInserter inserter= LoggerFlashSessionDBInserter.createLoggerFlashSessionDBInserter(hc, time);
        inserter.mainThread();
        */
//        }catch (Throwable ex){
//            throw new MeteringSessionException(ex);
//        }
        return newMeasuments;
    }



    public static void intervalHubFlashMemoryMode(String hubNo, 
            Date startTime, Date endTime, 
            boolean enable) throws MeteringSessionException {
        try{
        HubConnection hc = HubSessionDBManager.getHubSessionManager().getHubsSessions().getHubConnection(hubNo);
        if (enable){
           Timestamp startTimestamp = new Timestamp(startTime.getTime());
           Timestamp endTimestamp = new Timestamp(endTime.getTime());
           hc.enableIntervalHubFlashMemMode(startTimestamp, endTimestamp);
        }
        else
            hc.disableIntervalHubFlashMemMode();
        }catch (Throwable ex){
            throw new MeteringSessionException(ex.getMessage());
        }

    }

    public static void overwriteHubFlashMemoryMode(String hubNo, boolean enable) throws MeteringSessionException  {
        try{
        HubConnection hc = HubSessionDBManager.getHubSessionManager().getHubsSessions().getHubConnection(hubNo);
        if (enable)
            hc.enableOverrideHubFlashMemMode();
        else
            hc.disableOverrideHubFlashMemMode();
        }catch (Throwable ex){
            throw new MeteringSessionException(ex.getMessage());
        }

    }

    public static void registerMeasurer(String hubNo, String measurerNo)  throws MeteringSessionException {
        try{
        HubConnection hc = HubSessionDBManager.getHubSessionManager().getHubsSessions().getHubConnection(hubNo);
        hc.registerLogger(Utils.hexId2long(measurerNo));
        }catch (Throwable ex){
            throw new MeteringSessionException(ex.getMessage());
        }
        
    }

    public static void unregisterMeasurer(String hubNo, String measurerNo)  throws MeteringSessionException {
        try{
        HubConnection hc = HubSessionDBManager.getHubSessionManager().getHubsSessions().getHubConnection(hubNo);
        hc.unregisterLogger(Utils.hexId2long(measurerNo));
        }catch (Throwable ex){
            throw new MeteringSessionException(ex.getMessage());
        }
        
    }

    public static void measurerRadio(String hubNo, String measurerNo,  boolean enable)  throws MeteringSessionException {
        try{
        HubConnection hc = HubSessionDBManager.getHubSessionManager().getHubsSessions().getHubConnection(hubNo);
        if (enable){
            hc.enableLoggerRadio(Utils.hexId2long(measurerNo));
        }           
        else
            ;
//TODO: disable
         }catch (Throwable ex){
            throw new MeteringSessionException(ex.getMessage());
        }
        
    }

}
