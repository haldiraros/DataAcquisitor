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

package test;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import hubLibrary.meteringcomreader.HubConnection;
import hubLibrary.meteringcomreader.Utils;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionSPException;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Juliusz
 */
public class TestPerf {
    private static ComReadDispatch crd;
    static public class ComReadDispatch implements SerialPortEventListener{

        InputStream inputStream;
        byte[] byteBuf = new byte[62];

        public ComReadDispatch(InputStream inputStream) {
            this.inputStream = inputStream;
        }
        
        
        @Override
        public void serialEvent(SerialPortEvent spe) {
            if(spe.getEventType()!=SerialPortEvent.DATA_AVAILABLE){ 
                return;
            }
            System.out.println("waked up");
            while (true){
                try{
                    TestPerf._readBytes(inputStream, byteBuf, 62);
                    System.out.println(new String(byteBuf, "US-ASCII"));
                } catch (UnsupportedEncodingException ex) {
                    java.util.logging.Logger.getLogger(TestPerf.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MeteringSessionTimeoutException ex) {
                    return;  //if timeout to get next response then exit
                } catch (MeteringSessionException ex) {
                    java.util.logging.Logger.getLogger(TestPerf.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
//    private static final Logger lgr = LoggerFactory.getLogger(TestPerf.class);


    
 static public int _readBytes(InputStream inputStream, byte[]buf, int size) throws MeteringSessionException  {
        int ret=0;
        try {
            ret = inputStream.read(buf, 0, size);
        } catch (IOException ex) {
            throw new MeteringSessionSPException(ex);
        }
        return ret;
    }
  
/*
  static public int _readBytes(InputStream inputStream, byte[]buf, int size) throws MeteringSessionException{
        byte ret[] = new byte[1];
        int len;
        int retryCounter=0;

        
            for(int i=0; i<size; i++){
              retryCounter=0;
              while(retryCounter<2)
                try {
                    len=inputStream.read(ret);
                    if(len==-1) 
                        throw new MeteringSessionException("Serial EOF");
                    else if(len==0 ) {                        
                        if(retryCounter>0){
//                        lgr.debug("Time:"+System.nanoTime()+","+"Thread:"+Thread.currentThread().getName()+" Serial port read timeout in _readBytes size"+size);                
       System.out.print("resztka po timeoucie:"+new String(buf,0, i,  "US-ASCII"));
                        throw new MeteringSessionTimeoutException("Serial port read timeout"+i);
                        }
                        else{
                            retryCounter++;
                            System.out.println("retrying");
                        }
                    }
                    else{
                        buf[i]=ret[0];
                        break;
                    }
                } catch (IOException ex) {
  //                  lgr.debug("Time:"+System.nanoTime()+","+"IOException dedected in _readBytes"+ex);                
                    throw new MeteringSessionSPException(ex);
                }
            }
        

        return size;
    } 
*/
    public static SerialPort initComPort(String portName) throws MeteringSessionException {
        SerialPort serialPort = null;
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            
            serialPort = (SerialPort) portIdentifier.open("HubConnection", Utils.TIMEOUT);
            serialPort.setSerialPortParams(921600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
/*
            serialPort.setSerialPortParams(115200,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_EVEN);
                    */
// http://docs.oracle.com/cd/E17802_01/products/products/javacomm/reference/api/javax/comm/CommPort.html#getInputStream%28%29            
            serialPort.notifyOnOutputEmpty(true);
            serialPort.enableReceiveThreshold(62); 
//            serialPort.enableReceiveThreshold(1); 
//            serialPort.disableReceiveThreshold();
//            serialPort.enableReceiveTimeout(Utils.TIMEOUT); //maksymalny czas oczekiwania na odczyt to receiveThreshold lub enableReceiveTimeout
            int inpBuffSize = serialPort.getInputBufferSize();
        } catch (UnsupportedCommOperationException ex) {
            if (serialPort!=null)
                serialPort.close();
            throw new MeteringSessionException(ex);
        } catch (PortInUseException ex) {
            if (serialPort!=null)
                serialPort.close();
            throw new MeteringSessionException(ex);
        } catch (NoSuchPortException ex) {
            throw new MeteringSessionException(ex);
        }
        
        return serialPort;
    }

    static long printTime(String msg, long startTime){
        long endTime=System.nanoTime();
        System.out.println(msg+((endTime-startTime)/1000L));
        return System.nanoTime();
    }
    
    static byte[] fillTo62(byte[] buf){
        byte[] newBuf=new byte[62];
        for (int i=0; i< buf.length; i++){
            newBuf[i]=buf[i];
        }
        for (int i=buf.length; i<60; i++){
            newBuf[i]=0x20;
        }
        newBuf[60]=0x0d;
        newBuf[61]=0x0a;
        return newBuf;    
    }

    static public void main(String[] args) throws MeteringSessionException, IOException, InterruptedException, TooManyListenersException{
        SerialPort port = initComPort("COM5");
        InputStream inputStream = port.getInputStream();
//        BufferedReader streamReader = new BufferedReader (new InputStreamReader(inputStream, "US-ASCII"), 62);
        
        
        InputStreamReader streamReader =  new InputStreamReader(inputStream, "US-ASCII");
        BufferedWriter streamWriter = new BufferedWriter( new OutputStreamWriter(port.getOutputStream(), "US-ASCII" ), 1024);
        
        OutputStream outputstream=port.getOutputStream();
        
        byte [] binGetId ={0x6a, 0x55, 0x30, 0x31, 0x30, 0x30};
        binGetId=fillTo62(binGetId);
        
        byte [] closeSessions ={0x6a, 0x55, 0x30, 0x32, 0x46, 0x46};
        closeSessions=fillTo62(closeSessions);
        
        byte [] flashHubSession ={0x6a, 0x55, 0x30, 0x32, 0x30, 0x32, 0x46, 0x46, 0x46, 0x46, 0x46, 0x46, 0x46, 0x46}; //od FFFFFFFF
//        byte [] flashHubSession ={0x6a, 0x55, 0x30, 0x32, 0x30, 0x32, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30}; //od 00000000
        flashHubSession=fillTo62(flashHubSession);

        byte [] getPrevPacket ={0x6a, 0x55, 0x33, 0x41, 0x30, 0x32}; //turbo x16
//        byte [] getPrevPacket ={0x6a, 0x55, 0x32, 0x41, 0x30, 0x32}; //turbo x16 nextpcg
//        byte [] getPrevPacket ={0x6a, 0x55, 0x31, 0x41, 0x30, 0x32}; //nie turbo x1 prevpcg
        getPrevPacket=fillTo62(getPrevPacket);
        
        
        long timer;
        
    try{
        char buf[]=new char[62];
        byte byteBuf[]=new byte[62];
        String getId="jU0100 \n";
        outputstream.write(closeSessions);
        outputstream.flush();
        _readBytes(inputStream, byteBuf, 62);
       System.out.print(new String(byteBuf, "US-ASCII"));
       
        timer=System.nanoTime();        
//        streamWriter.write(getId);
        outputstream.write(flashHubSession);
        timer=printTime("Write:\t", timer);
        
        streamWriter.flush();
        timer=printTime("Flush:\t", timer);
//        Thread.sleep(1000);
//timer=printTime(timer);
//        streamReader.read(buf, 0, 62);
//        inputStream.read(byteBuf, 0, 62);
        _readBytes(inputStream, byteBuf, 62);
        timer=printTime("Read resp:\t", timer);        
       System.out.print(new String(byteBuf, "US-ASCII"));

/*
    for(int i=0; i<62; i++){
          inputStream.read();
           //timer=printTime(timer);
        }
*/
       
//get prev package   
 crd = 
        new ComReadDispatch(inputStream);
//port.addEventListener(crd);
//port.notifyOnDataAvailable(true);
        for(int u=0; u< 32768/16; u++) {
//          for(int v=0; v<2; v++)
          {
            outputstream.write(getPrevPacket);
            timer=printTime("get Prev write:\t", timer);

            streamWriter.flush();
            timer=printTime("Get prev flush:\t", timer);
          }
        //        Thread.sleep(1000);
        //timer=printTime(timer);
            
//          for(int v=0; v<2; v++)
            for(int i=0; i<9 *16; i++){
//System.out.println(i);
//                    streamReader.read(buf, 0, 62);
            //       inputStream.read(byteBuf, 0, 62);
        _readBytes(inputStream, byteBuf, 62);                
                   System.out.println(new String(byteBuf, "US-ASCII"));
            //       System.out.println(buf);
            }
            
            timer=printTime("Read all result:\t", timer);       
        }
        
//        Thread.sleep(1000*40);
//        System.out.print(buf);
//          System.out.print(byteBuf);
        }
    finally{
        outputstream.write(closeSessions);
        outputstream.flush();
        port.close();
    }
   }
 
} 