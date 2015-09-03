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

import java.sql.SQLException;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import java.sql.Timestamp;
import java.util.HashSet;
import static hubLibrary.meteringcomreader.SessionDBInserter.insertMeasurmentSQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Juliusz
 */
public class LoggerFlashSessionDBInserter extends SessionDBInserter{
    /**
     * Utworzenie loggera systemowego
     */
    private static final Logger lgr = LoggerFactory.getLogger(LoggerFlashSessionDBInserter.class);
    

    Timestamp start;
    public LoggerFlashSessionDBInserter(HubConnection hc, Timestamp start) throws MeteringSessionException {
        super(hc);
        this.start=start;
    }

    static public LoggerFlashSessionDBInserter createLoggerFlashSessionDBInserter(HubConnection hc, Timestamp start) throws MeteringSessionException {
        LoggerFlashSessionDBInserter sessDBInsert = new LoggerFlashSessionDBInserter(hc, start);
        sessDBInsert.metSess = hc.createLoggerFlashSession(start);
        return sessDBInsert;
    }
        
    @Override
    protected void upsertLoggerStatus(DataPacket dp) throws MeteringSessionException {
        ; //do not insert logger info
    }

    @Override
    public int  mainThread() throws MeteringSessionException {
        DataPacket dp;
        int measurmentsCount=0;
//            if (!getLoggerId(this.metSess.)){  //nie ma loggera w bazie danych
//                conn.commit();
//                return ;
//            }
        try {
            while ((dp = metSess.getNextPacket(3))!=null) {
                        measurmentsCount+=loadPacket(dp);
                        lgr.debug("Time:"+System.nanoTime()+","+dp);
                }
           lgr.info("Time:"+System.nanoTime()+", new measurments inserted"+measurmentsCount);                        
        } finally {
            try {close();} catch (MeteringSessionException e) {/*ignore it*/}
        }
        return measurmentsCount;
    }
    
 }
