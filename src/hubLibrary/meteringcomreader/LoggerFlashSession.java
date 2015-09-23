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
import java.sql.Timestamp;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionCRCException;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionDeviceBusyException;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionFlashLoggerTransException;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionNoLoggerOnHub;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionNoMoreDataException;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionOperationAlreadyInProgressException;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Juliusz
 */
public class LoggerFlashSession  extends MeteringSession{
    private static final Logger lgr = LoggerFactory.getLogger(LoggerFlashSession.class);
    static boolean skipLastPage = false;
    protected long pageCount;
    protected long pageCounter=0;

    protected long loggerId;
    
    protected byte[] packets=null;
    protected int bytesCounter=0;
    protected int bitCounter=0;
    protected boolean resultReaded=false;
    protected int packetSize;
    protected long time;
    protected long period;
    int[] temperatures;
    protected int recordsPerPage;
    

    
    public LoggerFlashSession(HubConnection hc, Timestamp time) throws MeteringSessionException {
        super(hc);
        byte[] ret;

        
//        try{close();} 
//       catch(MeteringSessionException e){}
        
        try{

            //hc.sendCommand(Utils.getIdLoggerFlashSessionReq);
            try{
                hc.sendCommand(Utils.getLoggerIdReq);        
                ret=hc.receiveAck(Utils.getIdLoggerFlashSessionRes);
            }catch(MeteringSessionDeviceBusyException e){
                hc.sendCommand(Utils.getLoggerIdReq);        
                ret=hc.receiveAck(Utils.getIdLoggerFlashSessionRes); //single retry  
            }
            loggerId=Utils.bytes2long(ret, 4);
            lgr.debug("getIdLoggerFlashSessionRes="+Long.toHexString(loggerId));
        }
        catch(MeteringSessionTimeoutException e){
            throw new MeteringSessionNoLoggerOnHub();
        }
/*        
        hc.sendCommand(Utils.getLoggerHardwareVerReq);
        ret=hc.receiveAck(Utils.getLoggerHardwareVerRes);
        int logHardVer=(int)Utils.bytes2long(ret, 2);
        lgr.debug("getLoggerHardwareVerRes="+Integer.toHexString(logHardVer));        

        hc.sendCommand(Utils.getLoggerFirmwareVerReq);
        ret=hc.receiveAck(Utils.getLoggerFirmwareVerRes);
        int logFirmVer=(int)Utils.bytes2long(ret, 2);
        lgr.debug("getLoggerHardwareVerRes="+Integer.toHexString(logFirmVer));        
*/
        hc.sendCommand(Utils.readPeriodRecodTimeFlashSessionReq);
        ret=hc.receiveAck(Utils.readPeriodRecodTimeFlashSessionRes);
        int periodSeconds=(int)Utils.bytes2long(ret, 2);
        lgr.debug("periodSeconds="+Integer.toString(periodSeconds));
        
        hc.sendCommand(Utils.readFirstRecodTimeFlashSessionReq);
        ret=hc.receiveAck(Utils.readFirstRecodTimeFlashSessionRes);        
        long startTime=Utils.bytes2long(ret, 4);
        Timestamp startTimestamp=Utils.time2Timestamp(startTime);
        lgr.debug("startTime="+startTimestamp);
        
        hc.sendCommand(Utils.readLastRecodTimeFlashSessionReq);
        ret=hc.receiveAck(Utils.readLastRecodTimeFlashSessionRes);        
        long endTime=Utils.bytes2long(ret, 4);
        Timestamp endTimestamp=Utils.time2Timestamp(endTime);
        lgr.debug("endTime="+endTimestamp);
        
        hc.sendCommand(Utils.countRecordsPerPageLoggerFlashSessionReq);
        ret=hc.receiveAck(Utils.countRecordsPerPageLoggerFlashSessionReq);        
        recordsPerPage=(int)Utils.bytes2long(ret, 1);
        lgr.debug("recordsPerPage="+Integer.toString(recordsPerPage));
        
        pageCount=(endTime-startTime)/periodSeconds/recordsPerPage+1;
        
        
        long startReadTime;
        int startPage;
        if (time!=null){
            startReadTime = Utils.timestamp2int(time);
            if (startReadTime>=startTime)
                startPage = (int)(startReadTime-startTime)/periodSeconds/recordsPerPage;
            else startPage=0;
        }
        else
            startPage=0; 

//         startPage=0; //TODO: usunąć

        lgr.debug("pageCount="+Long.toString(pageCount));
        lgr.debug("startPage="+Long.toString(startPage));
        lgr.debug("startReadTimeTimestamp="+time);
        
               
        byte[] pageBytes=Utils.long2bytes(startPage, 2);
        
        
        hc.sendCommand(Utils.startLoggerFlashSessionReq, pageBytes);
        ret=hc.receiveAck(Utils.startLoggerFlashRes);                
        long logId=Utils.bytes2long(ret, 4);
        if (this.loggerId!=logId)
            throw new MeteringSessionFlashLoggerTransException();
        packetSize=((int)Utils.bytes2long(ret, 4, 1)+1)*128; //wycięte CRC
        lgr.debug("packetSize="+Integer.toString(packetSize));
        ComResp.setResSize(Utils.getNextLoggerFlashSessionRes, packetSize);
        ComResp.setResSize(Utils.regetPrevLoggerFlashSessionRes, packetSize);
        
        
        temperatures=new int[recordsPerPage];  
        
        
    }
        
    public int getDataRecordingPeriod(){
        return 0;
    }
    
    public void  setDataRecordingPeriod(int period){
        
    }
    

 
    @Override
    public void close() throws MeteringSessionException {
        try{
            hc.sendCommand(Utils.closeLoggerFlashSessionReq);
            hc.receiveAck(Utils.closeLoggerFlashSessionRes);    
           }catch (MeteringSessionOperationAlreadyInProgressException e){
               ; // TODO: obsługa buga Darka
           }        

    }

    @Override
    public DataPacket getNextPacket() throws MeteringSessionException {
        return getPacket(Utils.getNextLoggerFlashSessionReq, Utils.getNextLoggerFlashSessionRes);
    }

    @Override
    public DataPacket regetPrevPacket() throws MeteringSessionException{
        hc.sendCommand(Utils.getIdLoggerFlashSessionReq);
        byte[] ret=hc.receiveAck(Utils.getIdLoggerFlashSessionRes);
        int logId=(int)Utils.bytes2long(ret, 4);
        if (this.loggerId!=logId)
            throw new MeteringSessionFlashLoggerTransException();
        lgr.debug("getIdLoggerFlashSessionRes="+Integer.toString(logId));
        return getPacket(Utils.regetPrevLoggerFlashSessionReq, Utils.regetPrevLoggerFlashSessionRes);
    }
    
        
    protected DataPacket getPacket(int requestCommand, int responseCommand) throws MeteringSessionException{
        lgr.debug("pageCounter="+Long.toString(pageCounter));
        lgr.debug("pageCount="+Long.toString(pageCount));
      /* TODO: start: latka z pomijaniem ostatniej strony */
//        if (pageCounter==pageCount)
//            return null;
        /* TODO: end: latka z pomijaniem ostatniej strony */

        pageCounter++;
        
        DataPacket dp=null;
        if (resultReaded)
            throw new MeteringSessionException("All data already readed in logger flash session");
        try{
            hc.sendCommand(requestCommand);
            packets = hc.receiveAck(responseCommand);
        } 
        catch (MeteringSessionNoMoreDataException e){
            resultReaded=true;
            return null;                            
        }
        
//        if (pageCounter==11)
//            lgr.error("end of test");
        if (pageCounter<pageCount) //łatka na brak CRC na ostatniej stronie
            checkCRC(packets);
        
        bytesCounter=0;
        time=Utils.bytes2long(packets, 4);
        bytesCounter+=4;
        period=Utils.bytes2long(packets, bytesCounter, 2);
        bytesCounter+=2;
        bitCounter=bytesCounter*8;

        dp=new DataPacket(0x4D4500000000L | loggerId, time, period);
        int temp;
        int tempCount=0;
        
        for (int i=0; i<recordsPerPage; i++){
            temp=getWord(bitCounter, 12); //wyliczyć temp
            if (temp==0xFFF //TODO: kompatybilność wsteczna
                    || temp==0x800){  //koniec strony wypełniony 0x800
                break;
            }            
            bitCounter+=12;
            if ((temp&0x0800)==0x0800)  //if sign bit in 12bits number is set
                temp=temp |0xFFFFF800;
            temperatures[tempCount]=temp;                        
            tempCount++; 
        }
        if (tempCount==0)
            return null;
        dp.setTemperatures(temperatures, tempCount);
        
        return dp;
    }

    
    public int getWord(int bitPos, int bits){
        int bitNo=8-(bitPos%8); //ile zostało
        int byteNo=bitPos/8; //skąd startujemy
        int mask= (0xFFFFFFFF>>>(32-bits));
        int res=((((int)packets[byteNo])&0xFF)>>>(8-bitNo)) & mask;
        int wordLen=bitNo;
        byteNo++;
        int shiftR=bitNo;
        while (wordLen<bits){
            mask= (0xFFFFFFFF>>>(32-bits)) >>> wordLen;
            res=res | (((((int)packets[byteNo])&0xFF) & mask)<<shiftR);
            shiftR+=8;
            byteNo++;
            wordLen+=8;      
        }
       return res;
    }

    private void checkCRC(byte[] packets) throws MeteringSessionCRCException {
        CRC16DN502 checksum = new CRC16DN502();
        for (int i=0; i<packets.length-2; i++)
            checksum.update(packets[i]);
        
        int computedCRC = checksum.getChecksum();
        
        int olderByte=((int)packets[packets.length-2])&0xFF;
        int youngestByte=((int)packets[packets.length-1])&0xFF;
        int readedCRC = (olderByte<<8) | youngestByte;
        if (computedCRC!=readedCRC){
            lgr.debug("CRC Error, computed: 0x"+Integer.toHexString(computedCRC) + ", readed: 0x"+Integer.toHexString(readedCRC));
            throw new MeteringSessionCRCException();
        }
    }


}
