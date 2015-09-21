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

import gnu.io.SerialPort;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionTimeoutException;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionSPException;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionSerialEOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reprezentuje ekspedytor informacji odbieranych z połączenia do koncentratora.
 * Pakiety danych odbierane ze sesji radiowej umieszczane są przez metodę serialEvent
 * w kolejce rsData,
 * natomiast inne odpowiedzi z koncentratora umieszczane są przez metodę serialEvent
 * w kolejce resp.
 * @author Juliusz Jezierski
 */
public class ComReadDispatch implements SerialPortEventListener{
    /**
     * Utworzenie loggera systemowego
     */
    private static final Logger lgr = LoggerFactory.getLogger(ComReadDispatch.class);
    
    
    /**
     * Reprezentuje strumień, służący do odbierania danych z połączenia 
     * z koncentratorem.
     */
    protected InputStream inputStream;
    
    /**
     * Reprezentuje kolejkę, w której metoda serialEvent umieszcza odebrane 
     * z koncentratora pakiety danych odebrane w sesji radiowej.
     */
    protected BlockingQueue<DataPacket> rsData=new LinkedBlockingQueue<DataPacket>(1000);
    
    /**
     * Reprezentuje kolejkę, w której metoda serialEvent umieszcza odpowiedzi odebrane 
     * z koncentratora z wyjątkiem danych z sesji radiowej.
     */
    protected BlockingQueue<ComResp> resp=new LinkedBlockingQueue<ComResp>(1000);
    
    /**
     * Służy do przekazania wyjątku zgłaszanego w metodzie serialEvent
     * przy odbieraniu danych z sesji radiowej.
     */
    protected MeteringSessionException rsException=null;
    
    /**
     * Służy do przekazania wyjątku zgłaszanego w metodzie serialEvent
     * przy odbieraniu odpowiedzi (z wyjątkiem danych odbieranych w sesji radiowej).
     */
    protected MeteringSessionException resException=null;
    protected SerialPort sp;
    
    
    /**
     * Konstruuje obiekt ekspedytora inicjując pole {@link #inputStream}.
     * @param in parametr inicjujący pole {@link #inputStream}
     */
/*
    public ComReadDispatch(InputStream in){
        this.inputStream=in;
    }
*/
    
    public ComReadDispatch(InputStream in, SerialPort sp) {
        this.sp=sp;
        this.inputStream=in;
    }    

    /**
     * Implementacja metody uruchamianej w przypadku wykrycia zdarzenia na strumieniu
     * {@link #inputStream}, obsługiwane jest tylko zdarzenie DATA_AVAILABLE,
     * zgłaszane w momencie pojawienia się nowych danych w strumieniu.
     * @param spe obiekt opisujący zdarzenie uaktywniające tę metodę
     */
    @Override
    public void serialEvent(SerialPortEvent spe) {
lgr.debug("Time:"+System.nanoTime()+", serialEvent: "+ spe.toString()+","+spe.getEventType()+", thread: "+Thread.currentThread().getName());           
        if(spe.getEventType()!=SerialPortEvent.DATA_AVAILABLE){ 
            return;
        }
       
        int loopNo=0;
        int res;
        byte[]data=null;
        int size;
        int len;
        int frameSize;
        while (true){
            loopNo++;
lgr.debug("Time:"+System.nanoTime()+" serialEvent loopNo:"+loopNo);
            try {
                try{
                    res = _receiveRes();
                
                } catch (MeteringSessionTimeoutException ex) {
                    return;  //if timeout to get next response then exit
                }
                catch (MeteringSessionSerialEOFException ex) {
                    return;
                   
                }
                if (res == Utils.radioSessionRes) 
                    try {
                        data =_receiveData(1);
                        frameSize = 0xFF & data[0];
//                        if (frameSize==DataPacket.LEN){                      
                            data = _receiveData(frameSize); //ramki o zmiennej długości
                            lgr.debug("Time:"+System.nanoTime()+", received new DP ");                            
                            DataPacket dp = new DataPacket(data, frameSize);                            
                            rsData.offer(dp);
                            lgr.debug("Time:"+System.nanoTime()+", new DP inserted into queue");                            
/*
                    }
                        else{
                            data =_receiveData(frameSize); //unknown frame format
                            lgr.debug("Time:"+System.nanoTime()+", unknown frame format, len:"+frameSize);                          
                        }
*/                
                    } catch (MeteringSessionTimeoutException ex) {
                        return;  //if timeout to get next data then exit
                    } catch (MeteringSessionException ex) {
                        lgr.debug("Time:"+System.nanoTime()+","+" exeption dedected in serialEvent"+ex);                                   
                        setRSException(ex);
                        return;
                    }
                else {
                    size=ComResp.getResDataSize(res);
                    lgr.debug("Size for:0x"+Integer.toHexString(res)+ " : "+Integer.toString(size) );
                    if((size&0x0FFF)==0x0FFF){ //next byte detemines data size
                        len=((int)Utils.bytes2long(_receiveData(1), 1))
                                *(1+size>>>12); // older 4 bits = len multiplier
                        data=_receiveData(len);
                    }
                    else if(size>0){
                        data=_receiveData(size);
                    }
                    else{
                        data=null;
                    }
                    resp.offer(new ComResp(res, data));
                }

            } catch (MeteringSessionException ex) {
                //setResException(ex); TODO: usunąć po testach
                resp.add(new ComResp(ex));
                return;
            }
        }
    }
    
    /**
     * Pobiera kolejny pakiet danych przesłanych w sesji radiowej z kolejki {@link #rsData}.
     * @return pakiet danych przesłany asynchronicznie w sesji radiowej
     * @throws MeteringSessionException w przypadku wykrycia wyjątku zarejestrowanego
     * w polu {@link #rsException}
     */
    public DataPacket getNextRSPacket() throws MeteringSessionException{
       MeteringSessionException e= getRSException();
       if (e!=null){
lgr.debug("Time:"+System.nanoTime()+","+"getNextRSPacket exeption dedected in getNextRSPacket "+e);           
           throw e;
       }
       DataPacket dp;
        try {
            dp = rsData.take();
        } catch (InterruptedException ex) {
            throw new MeteringSessionException("Interrupted while waiting for data packet");
        }
       return dp;
   }
   
    /**
     * Pobiera kolejną odpowiedź z kolejki {@link #resp}.
     * @return odpowiedź koncentratora na wysłane do niego wcześniej polecenie
     * @throws MeteringSessionTimeoutException w przypadku przekroczenia czasu
     * oczekiwania na odpowiedź z kolejki.
     * @throws MeteringSessionException w przypadku wykrycia wyjątku zarejestrowanego
     * w polu {@link #resException}
     */
    public ComResp getNextResp() throws MeteringSessionException{
/* TODO: usunąć po testach         
       MeteringSessionException e= getResException();
       if (e!=null){
lgr.debug("Time:"+System.nanoTime()+","+"exeption dedected in getNextResp "+e);           
           throw e;
       }       
       */
       ComResp ret=null;
        try {
            ret=resp.poll(Utils.TIMEOUT*12, TimeUnit.MILLISECONDS);
            if (ret==null){
                lgr.debug("Time:"+System.nanoTime()+","+Thread.currentThread().getName()+" throws exception  in getNextResp "+"Timeout during waiting for response");           
                throw new MeteringSessionTimeoutException("Timeout during waiting for response");
            }
            else if (ret.getExp()!=null)
                throw ret.getExp();
        } catch (InterruptedException ex) {
            //
        }
       return ret;
   }
    
    /**
     * Odczytuje ze strumienia {@link #inputStream} <CODE>size</CODE> bajtów 
     * i umieszcza je w tablicy bajtów <CODE>buf</CODE>.
     * @param buf tablica, w której metoda umieszcza odczytane dane
     * @param size liczba bajtów do odczytu ze strumienia {@link #inputStream}
     * @return liczba odczytanych bajtów
     * @throws MeteringSessionTimeoutException w przypadku przekroczenia czasu
     * oczekiwania {@link Utils#TIMEOUT}.
     * @throws MeteringSessionException w przypadku wystąpienia błędów
     * we-wy na strumieniu {@link #inputStream}
     */
/*    
    protected int _readBytes(byte[]buf, int size) throws MeteringSessionException{

        int len=0;

        try{
                try {
                    sp.enableReceiveThreshold(size);
                    len=this.inputStream.read(buf, 0, size);
                    if(len==-1) 
                        throw new MeteringSessionException("Serial EOF");
                    if(len==0) {
                        lgr.debug("Time:"+System.nanoTime()+","+"Thread:"+Thread.currentThread().getName()+" Serial port read timeout in _readBytes size"+size);                
                        throw new MeteringSessionTimeoutException("Serial port read timeout");
                    }            
                } catch (IOException ex) {
                    lgr.debug("Time:"+System.nanoTime()+","+"IOException dedected in _readBytes"+ex);                
                    throw new MeteringSessionSPException(ex);
                } catch (UnsupportedCommOperationException ex) {
                        lgr.debug("Time:"+System.nanoTime()+","+"Thread:"+Thread.currentThread().getName()+" Serial port read timeout in _readBytes size"+size);                
                        throw new MeteringSessionException(ex);
            }
        }
        finally{
            lgr.debug("Time:"+System.nanoTime()+","+" _readBytes size:"+len);                
              if (lgr.isDebugEnabled()){
                   StringBuilder sb = new StringBuilder(1000);
                   sb.append("Time:");
                   sb.append(System.nanoTime());
                   sb.append(", ");                 
                   for (int i=0; i<len; i++){
                    sb.append(String.format("%0#2X", buf[i]));
                    sb.append(',');          
                   }
                   lgr.debug(sb.toString());                
              }            
        }

        return len;
    }
*/    
      protected int _readBytes(byte[]buf, int size) throws MeteringSessionException{
        byte ret[] = new byte[1];
        int len;

        try{
            for(int i=0; i<size; i++){           
                try {
                    len=this.inputStream.read(ret);
                    if(len==-1) 
                        throw new MeteringSessionSerialEOFException("Serial EOF");
                    if(len==0) {
                        lgr.debug("Time:"+System.nanoTime()+","+"Thread:"+Thread.currentThread().getName()+" Serial port read timeout in _readBytes size"+size);                
                        throw new MeteringSessionTimeoutException("Serial port read timeout");
                    }
                    buf[i]=ret[0];
                } catch (IOException ex) {
                    lgr.debug("Time:"+System.nanoTime()+","+"IOException dedected in _readBytes"+ex);                
                    throw new MeteringSessionSPException(ex);
                }
            }
        }
        finally{
              lgr.debug("Time:"+System.nanoTime()+","+" _readBytes size:"+size);                
              if (lgr.isDebugEnabled()){
                   StringBuilder sb = new StringBuilder(1000);
                   sb.append("Time:");
                   sb.append(System.nanoTime());
                   sb.append(", ");                 
                   for (int i=0; i<size; i++){
                    sb.append(String.format("%0#2X", buf[i]));
                    sb.append(',');          
                   }
                   lgr.debug(sb.toString());     
              }
        }

        return size;
    } 

   /**
    * Setter dla pola {@link #rsException}.
    * @param e ustawiany wyjątek
    */
   synchronized protected void setRSException(MeteringSessionException e){
       this.rsException=e;
   }
   
   /**
    * Getter dla dla pola {@link #rsException}.
    * @return wyjątek
    */
   synchronized protected MeteringSessionException getRSException(){
       MeteringSessionException e = this.rsException;
       this.rsException=null;
       return e;
   }
   
   /**
    * Setter dla pola {@link #resException}.
    * @param e ustawiany wyjątek
    */
   synchronized protected void setResException(MeteringSessionException e){
       this.resException=e;
   }  
   
   
   /**
    * Getter dla  pola {@link #resException}.
    * @return ustawiany wyjątek
    */
   synchronized protected MeteringSessionException getResException(){
       MeteringSessionException e = this.resException;
       this.resException=null;
       return e;
   }
   
   /**
    * Pobiera <code>size</code> bajtów ze strumienia {@link #inputStream}
    * i umieszcza w tablicy wynikowej, metoda używana do odczytu danych 
    * z odpowiedzi koncentratora.
    * @param size
    * @return dane z odpowiedzi koncentratora
    * @throws MeteringSessionException
    */
   protected byte[] _receiveData(int size) throws MeteringSessionException{
        byte[] ret=new byte[size];
        _readBytes(ret, size);
        return ret;
    }
    
   /**
    * Pobiera pierwsze 2 bajty odpowiedzi koncentratora 
    * ze strumienia {@link #inputStream}.
    * @return pierwsze 2 bajty odpowiedzi, czyli kod odpowiedzi koncentratora
    * @throws MeteringSessionException
    */
   protected int _receiveRes()throws MeteringSessionException{
        byte[] ret = new byte[2];
        _readBytes(ret, 2);
        return (int)Utils.bytes2long(ret, (byte)2);
    }
    
}
