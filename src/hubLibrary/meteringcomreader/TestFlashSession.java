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
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionOperationAlreadyInProgressException;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author Juliusz
 */
public class TestFlashSession {
        public static void main(String args[]) throws MeteringSessionException, InterruptedException, Exception{
            PropertyConfigurator.configure(TestFlashSession.class.getResource("log4j.properties"));
     
            /* TODO start latka do pominiecia ostatniej strony pamieci loggera*/
            /* zmienić w projekcie główna klasę! */
            if (args.length>0 && "-skipLastPage".equals(args[0]))
                LoggerFlashSession.skipLastPage=true;
            else
                LoggerFlashSession.skipLastPage=false;
            System.out.println("skipLastPage="+Boolean.toString(LoggerFlashSession.skipLastPage));            
            /* TODO end latka do pominiecia ostatniej strony pamieci loggera*/
            
             HubConnection hc=null;
            try{
            Hubs hubs = HubSessionDBManager.getHubSessionManager().discoverHubs();
            Hub hub=null;
            
            Iterator<Map.Entry<String, Hub>> it =hubs.entrySet().iterator();
            if (it.hasNext()) {
                Map.Entry<String, Hub> pairs = (Map.Entry<String, Hub>)it.next();
                hub = pairs.getValue();

            }
            else{
                throw new MeteringSessionException("nie znaleziono żadnego huba");
            }
             hc=HubConnection.createHubConnection(hub);
            /*
            HubFlashSession hubSession = hc.createHubFlashSession(new Timestamp(0));
            DataPacket packet;
            while ((packet = hubSession.getNextPacket())!=null){
            ;
            }
            hubSession.close();
             */

            hc.closeAllSessions();//test czy przypadkiem nie jest niezamknięta sesja
            //
            LoggerFlashSession loggerFlashSession = hc.createLoggerFlashSession(new Timestamp(0));
            DataPacket packet=null;
            
            while ((packet = loggerFlashSession.getNextPacket(100000))!=null){
            System.out.println(packet + "bla bla");
            }
            
/*            
            LoggerFlashSessionDBInserter loggerFlashSessionDBInserter = LoggerFlashSessionDBInserter.createLoggerFlashSessionDBInserter(hc, null);
            loggerFlashSessionDBInserter.mainThread();
*/            
            
//            HubSessionManager.downloadMeasurmentsFromLogger(hub.getHubHexId(), null);
            
            }
            finally{
                if (hc!=null)
                    hc.close();
            }
            
            
        }
    
}
