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

import hubLibrary.meteringcomreader.exceptions.MeteringSessionSPException;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import gnu.io.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reprezentuje połączenie do koncentratora.
 * @author Juliusz Jezierski
 */
public class HubConnection implements Runnable{
    
    /**
     * Utworzenie loggera systemowego
     */
    private static final Logger lgr = LoggerFactory.getLogger(HubConnection.class);

    
    /**
     * Strumień danych wysyłanych do koncentratora.
     */
    protected OutputStream outputStream;
    /**
     * Strumień danych odbieranych z koncentratora.
     */
    protected InputStream inputStream;
    /** 
     * Port szeregowy, do którego podłączony jest koncentrator.
     */
    protected SerialPort serialPort = null;
    /**
     * Zmienna synchronizująca dostęp do strumienia {@link #outputStream}
     */
    protected boolean canSendCommand =  true;
    /**
     * Obiekt opisujący podłączony koncentrator.
     */
    protected Hub hub;
    /**
     * Obiekt ekspedytora, służący do segregowania danych przesyłanych
     * przez koncentrator.
     */
    protected ComReadDispatch crd;
    /**
     * Obiekt reprezentujący otwartą sesję radiową.
     */
    private RadioSession radioSession;
    /**
     * Obiekt reprezentujący sesję odczytu pamięci flash koncentratora.
     */
    private HubFlashSession hubFlashSession;
    /**
     * Obiekt reprezentujący sesję odczytu pamięci flash loggera.
     */
    private LoggerFlashSession loggerFlashSession;
    /**
     * Kontroluje wątek sprawdzający bicie serca koncentratora.
     */
    protected boolean runHearbeat = true;
    /**
     * Wątek sprawdzający bicie serca koncentratora.
     */
    protected Thread heartBeatThread = null;

    /**
     * Inicjuje port szeregowy o identyfikatorze <code>portIdentifier</code>
     * @param portIdentifier identyfikator inicjowanego portu
     * @return obiekt reprezentujący zainicjowany port
     * @throws MeteringSessionException 
     */
    public static SerialPort initComPort(CommPortIdentifier portIdentifier) throws MeteringSessionException {
        SerialPort serialPort = null;
        try {
            
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
            serialPort.enableReceiveThreshold(1); 
//            serialPort.disableReceiveThreshold();
            serialPort.enableReceiveTimeout(Utils.TIMEOUT); //maksymalny czas oczekiwania na odczyt to receiveThreshold lub enableReceiveTimeout
            int inpBuffSize = serialPort.getInputBufferSize();
            lgr.info("Input BufferSize="+inpBuffSize);
        } catch (UnsupportedCommOperationException ex) {
            if (serialPort!=null)
                serialPort.close();
            throw new MeteringSessionException(ex);
        } catch (PortInUseException ex) {
            if (serialPort!=null)
                serialPort.close();
            throw new MeteringSessionException(ex);
        }
        
        return serialPort;
    }

    /**
     * "Sprywatyzowanie" jedynego konstruktora wymusza
     * użycie metody {@link #createHubConnection(#Hub)} w celu utworzenia
     * obiektu tej klasy.
     */
    private  HubConnection(){
    }
    
    /**
     * Tworzy puuste połączenie do koncentratora na podstawie <code>hub</code> 
     * na potrzby testowania połaczenia zwrotnego.
     * @param hub opis koncentratora 
     * @return połączenie do koncentratora
     */    
    public static HubConnection createEmptyHubConnection(Hub hub){
        HubConnection hc = new HubConnection();
        hc.setHub(hub);
        return hc;
    }

    /**
     * Tworzy połączenie do koncentratora na podstawie <code>hub</code>.
     * @param hub opis koncentratora 
     * @return połączenie do koncentratora
     * @throws MeteringSessionException zgłaszany w przypadku nie znalezienia
     * portu szeregowego wskazanego w <code>hub</code>, zgłoszenia wyjątku
     * przy wykonywaniu operacji we-wy na strumieniach portu szeregowego
     * lub niemożności zarejestrowania ekspedytora na strumieniu wejściowym
     * koncentratora.
     */
    public static HubConnection createHubConnection(Hub hub) throws MeteringSessionException {
        HubConnection hc = new HubConnection();
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(hub.getComPortName());
            SerialPort serialPort = HubConnection.initComPort(portIdentifier);
            
            hc.setInputStream(new HexInputStream(serialPort.getInputStream()));            

            hc.setOutputStream( new HexOutputStream(  new BufferedOutputStream( serialPort.getOutputStream() ) ) );
            
            hc.setSerialPort(serialPort);
            hc.setHub(hub);

            ComReadDispatch crd = new ComReadDispatch(hc.getInputStream(), hc.getSerialPort());
            hc.setCrd(crd);
            serialPort.addEventListener(crd);
            serialPort.notifyOnDataAvailable(true); 
            
            hc.setHeartBeatThread(new Thread(hc, "HeartBeatThread for hub 0x"+hub.getHubHexId()));
//            hc.getHeartBeatThread().start(); //TODO: enable heartBeat

        } catch (TooManyListenersException ex) {
            throw new MeteringSessionException(ex);
        } catch (IOException ex) {
            throw new MeteringSessionException(ex);
        } catch (NoSuchPortException ex) {
            throw new MeteringSessionException(ex);
        }
        return hc;
    }

    /**
     * Wykrywa zainstalowane koncentratory, pomijając te które są wykorzystywane
     * przez sesje zawarte w <code>hs</code>.
     * @param hs zbiór sesji wykorzystujących koncentratory
     * @return opisy zainstalowanych koncentratorów
     */
    public static Hubs discoverHubs(HubsSessions hs) {

        Hubs hubs = new Hubs(10);
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        OutputStream outputStream = null;
        InputStream inputStream = null;
        long hubId;
        SerialPort serialPort = null;


        byte[] buf = new byte[4];

        while (portList.hasMoreElements()) {
            CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();

            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                String comPort = portId.getName();
                lgr.info("Time:"+System.nanoTime()+","+"Found port " + comPort);
                if (hs.isPortUsed(comPort))
                    continue;
//TODO: usunąć!          
/*                
                if ("COM5".equals(portId.getName())) {
                    continue;
                }
*/

                try {
                    if (portId.isCurrentlyOwned()) {
                        throw new MeteringSessionException("Serial port currently in use");
                    }
                    serialPort = HubConnection.initComPort(portId);
                    inputStream = (new HexInputStream(serialPort.getInputStream()));            
                    outputStream = ( new HexOutputStream(  new BufferedOutputStream( serialPort.getOutputStream() ) ) );
//                    outputStream = serialPort.getOutputStream();
//                    inputStream = serialPort.getInputStream();
                    Utils.sendCommand(outputStream, Utils.closeAllSessionReq);
                    try {
                        Thread.sleep(Utils.COM_DISCOVERY_TIMEOUT);
                    } catch (InterruptedException ex) {
                        //ignore
                    }

                    Utils.cleanInputStream(serialPort.getInputStream()); 

                    Utils.sendCommand(outputStream, Utils.hubIdentifictionReq);
                    if (inputStream.read() 
                            != (Utils.hubIdentifictionAck & 0x00FF)) {
                        continue;
                    }
                    if (inputStream.read() != ((Utils.hubIdentifictionAck >>> 8) & 0x00FF)) {
                        continue;
                    }
                    Utils.readBytes(inputStream, buf, 4);
                    hubId = Utils.bytes2long(buf, (byte) 4);                    

                    Utils.readBytes(inputStream, buf, 2);
                    int hubFirmVer= (int)Utils.bytes2long(buf, (byte) 2);
                    
                    hubs.put(Hub.convertHubId2Hex(hubId), new Hub(hubId, comPort));
                    lgr.info("Time:"+System.nanoTime()+","+"hub found:" + portId.getName()+"hub firm ver"+hubFirmVer);
                } catch (MeteringSessionException ex) {

                    // lgr.warn(null, ex);

                } catch (IOException e) {
                    lgr.debug("Time:"+System.nanoTime()+","+e.getMessage());
                }
                finally {
                    HubConnection.closePort(inputStream, outputStream, serialPort);
                    serialPort = null;
                    inputStream = null;
                    outputStream = null;
                }
            }
        }
        return hubs;
    }

    /** 
     * Zamyka port szeregowy i związane z nim strumienie.
     * @param inputStream zamykany strumień wejściowy
     * @param outputStream zamykany strumień wyjściowy
     * @param serialPort zamykany port
     */
    static public void closePort(InputStream inputStream, OutputStream outputStream, SerialPort serialPort) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException ex) {
            lgr.warn(null, ex);
            }
        }

        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException ex) {
            lgr.warn(null, ex);
        }
        if (serialPort != null) {
            serialPort.close();
        }
    }

    /**
     * Zamyka połączenie z koncentratorem, kończąc wszystkie sesje
     * i zatrzymując wątek sprawdzający bicie serca koncentratora.
     */
    public void close() {
        if (heartBeatThread!=null){
            setRunHearbeat(false);
            heartBeatThread.interrupt();
        }
        
        try {
            if (radioSession!=null)
                radioSession.close();
        } catch (MeteringSessionException ex) {
            lgr.warn(null, ex);
        }
        try {
            if (this.hubFlashSession!=null) 
                hubFlashSession.close();
        } catch (MeteringSessionException ex) {
            lgr.warn(null, ex);
        }
        try {
            if (this.loggerFlashSession!=null) 
                loggerFlashSession.close();
        } catch (MeteringSessionException ex) {
            lgr.warn(null, ex);
        }
        serialPort.removeEventListener();
        HubConnection.closePort(inputStream, outputStream, serialPort);
        serialPort = null;
        inputStream = null;
        outputStream = null;
    }

    /** 
     * Tworzy sesję odczytu pamięci flash koncentratora od czasu <code>time</code>
     * @param time  czas, od którego zarejestrowane dane w koncentratorze mają
     * być odczytane w tworzonej sesji
     * @return obiekt sesji 
     * @throws MeteringSessionException zgłaszany w przypadku błędu komunikacji
     * z koncentratorem 
     */
    public HubFlashSession createHubFlashSession(Timestamp time) throws MeteringSessionException {
        hubFlashSession = new HubFlashSession(this, time);
        return hubFlashSession;
    }

     public HubFlashSession createHubFlashSession(long time) throws MeteringSessionException {
        hubFlashSession = new HubFlashSession(this, time);
        return hubFlashSession;
    }
    /**
     * Zamyka sesję odczytu pamięci flash koncentratora.
     * @throws MeteringSessionException zgłaszany w przypadku niepowodzenia
     * zamknięcia sesji
     */
    public void closeHubFlashSession() throws MeteringSessionException {
      if(hubFlashSession!=null)
        try{
            hubFlashSession.close();
        }
        finally{
            hubFlashSession = null;
        }
    }

    /**
     * Zamyka wszystkie sesję koncentratora.
     * @throws MeteringSessionException 
     */
    public void closeAllSessions() throws MeteringSessionException {
            sendCommand(Utils.closeAllSessionReq);
            receiveAck(Utils.closeAllSessionRes);
               
    }    
    /**
     * Tworzy sesję radiową, ustalając maksymalny czas bezczynności na <code>timeout</code>
     * sekund, po którym koncentrator zamyka sesję.
     * @param timeout  maksymalny czas bezczynności w sekundach, po którym koncentrator zamyka sesję.
     * @return obiekt sesji radiowej
     * @throws MeteringSessionException zgłaszany w przypadku błędu komunikacji
     * z koncentratorem
     */
    public RadioSession createRadioSession(int timeout) throws MeteringSessionException {
        radioSession = new RadioSession(this, timeout);
        return radioSession;
    }

    /**
     * Odczytuje z koncentratora, czy będzie on włączony po odinstalowaniu.
     * @return true, jeżeli koncentrator będzie włączony po odinstalowaniu
     * @throws MeteringSessionException zgłaszany w przypadku błędu komunikacji
     * z koncentratorem
     */
    public boolean isHubPoweredAfterSession() throws MeteringSessionException {
        sendCommand(Utils.isHubPoweredAfterSessionReq);
        byte[] data = receiveAck(Utils.isHubPoweredAfterSessionRes);
        if (data[0] == 0x00) {
            return false;
        }
        return true;
    }

    /**
     * Ustala czy koncentrator ma być włączony po jego odinstalowaniu. 
     * @param shouldPowered true, jeżeli koncentrator ma być włączony po odinstalowaniu
     * @throws MeteringSessionException zgłaszany w przypadku błędu komunikacji
     * z koncentratorem
     */
    public void setHubPoweredAfterSession(boolean shouldPowered) throws MeteringSessionException {
        if (shouldPowered) {
            sendCommand(Utils.setHubPoweredAfterSessionTrueReq);
            receiveAck(Utils.setHubPoweredAfterSessionTrueRes);
        } else {
            sendCommand(Utils.setHubPoweredAfterSessionFalseReq);
            receiveAck(Utils.setHubPoweredAfterSessionFalseRes);
        }
    }

    /**
     * Pobiera z koncentratora okres rejestracji danych dla trybu interwałowego i
     * ustawia je w parametrach <code>startInteval</code> i <code>stopInteval</code>.
     * @param startInterval czas rozpoczęcia rejestracji danych
     * @param stopInterval czas zakończenia rejestracji danych
     * @throws MeteringSessionException  zgłaszany w przypadku błędu komunikacji
     * z koncentratorem
     */
    public void getPeriodIntervalHubFlashMemMode(Timestamp startInterval, Timestamp stopInterval) throws MeteringSessionException {
        long time;
        sendCommand(Utils.getPeriodIntervalHubFlashMemModeReq);
        byte[] data = receiveAck(Utils.getPeriodIntervalHubFlashMemModeRes);
        if (data != null) {
            time = Utils.bytes2long(data, 4);
            Utils.setTimestamp(startInterval, time);
            time = Utils.bytes2long(data, 3, 4);
            Utils.setTimestamp(stopInterval, time);
        }
    }

    public int getHubFirmawareVersion() throws MeteringSessionException{
        sendCommand(Utils.hubFirmwareVerReq);
        byte[] data = receiveAck(Utils.hubFirmwareVerRes);
        int ver= (int)Utils.bytes2long(data, 2);
        return ver;
    }
    
    public int getHubHardwareVersion() throws MeteringSessionException{
        sendCommand(Utils.hubHardwareVerReq);
        byte[] data = receiveAck(Utils.hubHardwareVerRes);
        int ver= (int)Utils.bytes2long(data, 2);
        return ver;
    }
    
    /**
     * Pobiera informacje o trybie rejestracji danych w pamięci flash koncentratora.
     * @return tryb rejestracji danych w pamięci flash koncentratora, bit b0 - tryb nadpisywania,
     * bit b1 - tryb interwałowy
     * @throws MeteringSessionException  zgłaszany w przypadku błędu komunikacji
     * z koncentratorem
     */
    public int getHubFlashMemMode() throws MeteringSessionException {
        sendCommand(Utils.getHubFlashMemModeReq);
        byte[] data = receiveAck(Utils.getHubFlashMemModeRes);
        return data[0];
    }

    /** 
     * Pobiera z koncentratora listę zarejestrowanych w nim loggerów.
     * Dane wysyłane przez zarejestrowane w koncentratorze loggery są zapisywane 
     * w pamięci flash koncentratora.
     * @return tablica zarejestrowanych w koncentratorze loggerów
     * @throws MeteringSessionException zgłaszany w przypadku błędu komunikacji
     * z koncentratorem
     */
    public long[] getRegistredLoggers() throws MeteringSessionException {
        sendCommand(Utils.getRegistredLoggersReq);
        byte[] data = receiveAck(Utils.getRegistredLoggersRes);
        if (data==null)
            return new long[0];
        int loggersCount = data.length/4;
        long loggers[] = new long[loggersCount];
        for (int i = 0; i < loggersCount; i += 1) {
            loggers[i] = Utils.bytes2long(data, i, 4);
        }
        return loggers;
    }

    /**
     * Włącza w koncentratorze tryb nadpisywania jego pamięci flash.
     * @throws MeteringSessionException zgłaszany w przypadku błędu komunikacji
     * z koncentratorem
     */
    public void enableOverrideHubFlashMemMode() throws MeteringSessionException {
        sendCommand(Utils.enableOverwriteHubFlashMemModeReq);
        receiveAck(Utils.enableOverwriteHubFlashMemModeAck);
    }

    /**
     * Włącza w koncentratorze tryb interwałowy jego pamięci flash, okres
     * jest opisany za pomocą <code>start</code> i <code>stop</code>.
     * @param start początek okresu
     * @param stop koniec okresu
     * @throws MeteringSessionException zgłaszany w przypadku błędu komunikacji
     * z koncentratorem
     */
    public void enableIntervalHubFlashMemMode(Timestamp start, Timestamp stop) throws MeteringSessionException {
        byte[] intervals = new byte[8];
        long startInt = Utils.timestamp2int(start);
        long stopInt = Utils.timestamp2int(stop);
        Utils.long2bytes(intervals, 0, startInt, 4);
        Utils.long2bytes(intervals, 4, stopInt, 4);
        sendCommand(Utils.enableIntervalHubFlashMemModeReq, intervals);
        receiveAck(Utils.enableIntervalHubFlashMemModeAck);
    }

    /**
     * Wyłącza w koncentratorze tryb nadpisywania jego pamięci flash.
     * @throws MeteringSessionException  zgłaszany w przypadku błędu komunikacji
     * z koncentratorem
     */
    public void disableOverrideHubFlashMemMode() throws MeteringSessionException {
        sendCommand(Utils.disableOverwriteHubFlashMemModeReq);
        receiveAck(Utils.disableOverwriteHubFlashMemModeAck);
    }

    /**
     * Wyłącza w koncentratorze tryb interwałowy jego pamięci flash
     * @throws MeteringSessionException zgłaszany w przypadku błędu komunikacji
     * z koncentratorem
     */
    public void disableIntervalHubFlashMemMode() throws MeteringSessionException {
        byte[] intervals = new byte[8];

        sendCommand(Utils.disableIntervalHubFlashMemModeReq, intervals);
        receiveAck(Utils.disableIntervalHubFlashMemModeAck);
    }

    /**
     * Rejestruje w koncentratorze logger o identyfikatorze <CODE>loggerId</CODE>.
     * @param loggerId identyfikator rejestrowanego loggera
     * @return identyfikator zarejestrowanego loggera
     * @throws MeteringSessionException zgłaszany w przypadku błędu komunikacji
     * z koncentratorem
     */
    public long registerLogger(long loggerId) throws MeteringSessionException {
        sendCommand(Utils.registerLoggerReq, loggerId, (byte) 4);
        byte[] data = receiveAck(Utils.registerLoggerAck);
        return Utils.bytes2long(data, 4);
    }

    /**
     * Wyrejestrowuje z koncentratora logger o identyfikatorze <CODE>loggerId</CODE>.
     * @param loggerId identyfikator wyrejestrowywanego loggera
     * @return identyfikator wyrejestrowanego loggera
     * @throws MeteringSessionException zgłaszany w przypadku błędu komunikacji
     * z koncentratorem
     */
    public long unregisterLogger(long loggerId) throws MeteringSessionException {
        sendCommand(Utils.unregisterLoggerReq, loggerId, (byte) 4);
        byte[] data = receiveAck(Utils.unregisterLoggerAck);
        return Utils.bytes2long(data, 4);
    }

    /**
     * Pobiera z koncentratora wyrażony w procentach poziom naładowania jego baterii.
     * @return wyrażony w procentach poziom naładowanie baterii koncentratora
     * @throws MeteringSessionException zgłaszany w przypadku błędu komunikacji
     * z koncentratorem
     */
    public int getChargeHubBatteryLevel() throws MeteringSessionException {
        sendCommand(Utils.getChargeHubBatteryLevelReq);
        byte[] data = receiveAck(Utils.getChargeHubBatteryLevelRes);
        return data[0];
    }

    /**
     * Pobiera aktualny czas z RTC koncentratora.
     * @return aktualny czas z RTC koncentratora
     * @throws MeteringSessionException  zgłaszany w przypadku błędu komunikacji 
     * z koncentratorem
     */
    public Timestamp getHubTime() throws MeteringSessionException {
        sendCommand(Utils.getHubTimeReq);
        byte[] data = receiveAck(Utils.getHubTimeReq);
        long timeInt = Utils.bytes2long(data, 4);
        Timestamp time = Utils.time2Timestamp(timeInt);
        return time;
    }

    /**
     * Ustawia czas RTC koncentratora.
     * @param t ustawiany czas koncentratora
     * @throws MeteringSessionException  zgłaszany w przypadku błędu komunikacji 
     * z koncentratorem
     */
    public void setHubTime(Timestamp t) throws MeteringSessionException {
        long timeInt = Utils.timestamp2int(t);
        byte[] data = Utils.long2bytes(timeInt, 4);
        sendCommand(Utils.setHubTimeReq, data);
        receiveAck(Utils.setHubTimeReq);
    }

    /**
     * Wysyła polecenie <code>command</code> do koncentratora.
     * @param command wysyłane do koncentratora polecenie
     * @throws MeteringSessionException  zgłaszany w przypadku błędu komunikacji 
     * z koncentratorem
     */
    void sendCommand(int command) throws MeteringSessionException {
        sendCommand(command, null);
    }

    /**
     * Wysyła polecenie <code>command</code> wraz parametrami umieszczonymi
     * w tablicy <code>data</code> do koncentratora.
     * @param command wysyłane do koncentratora polecenie
     * @param data parametry polecenia
     * @throws MeteringSessionException  zgłaszany w przypadku błędu komunikacji 
     * z koncentratorem
     */
    synchronized void sendCommand(int command, byte[] data) throws MeteringSessionException {
        lgr.debug("Time:"+System.nanoTime()+","+"wait sendCommand 0x"+String.format("%4x", command)+ 
        " thread: "+Thread.currentThread().getName());        
        while(!canSendCommand){
            try {
                wait(1000);
            } catch (InterruptedException e) {}
            if(!canSendCommand){
                throw new MeteringSessionException("Timeout during wait to send command:"+String.format("%4x", command));
            }
                
        }
        lgr.debug("Time:"+System.nanoTime()+","+"start sendCommand 0x"+String.format("%4x", command)+ 
        " thread: "+Thread.currentThread().getName()+Utils.bytes2HexStr(data)); 
        
        canSendCommand = false;
        if (Thread.currentThread().isInterrupted()) 
           return;
        Utils.sendCommand(outputStream, command, data);
    }

    /**
     * Wysyła do koncentratora polecenie <code>command</code> wraz parametrami umieszczonymi
     * w <code>size</code> najmłodszych bajtach <code>parameter</code> .
     * @param command wysyłane polecenie
     * @param parameter parametry polecenia
     * @param size objętość parametrów polecenia
     * @throws MeteringSessionException  zgłaszany w przypadku błędu komunikacji 
     * z koncentratorem
     */
    void sendCommand(int command, long parameter, byte size) throws MeteringSessionException {
        byte buf[] = Utils.long2bytes(parameter, size);
        sendCommand(command, buf);
    }

    /**
     * Czyści strumień wejściowy koncentratora.
     * @throws MeteringSessionException  zgłaszany w przypadku błędu komunikacji 
     * z koncentratorem
     */
    public void cleanInputStream() throws MeteringSessionSPException {
        Utils.cleanInputStream(inputStream);
    }

    /**
     * Pobiera z ekspedytora odpowiedź koncentratora.
     * @param ack spodziewana odpowiedź
     * @return pobrana odpowiedź 
     * @throws MeteringSessionException zgłaszany w przypadku błędu komunikacji 
     * z koncentratorem
     */
    synchronized byte[] receiveAck(int ack) throws MeteringSessionException {
        byte[] ret = null;
        try{
            ComResp rs = crd.getNextResp();
            rs.receiveAck(ack); //TODO:P sprawdzić testowanie
            ret = rs.receiveData();
        }
        finally{
            canSendCommand=true;
            notifyAll();
        }
        lgr.debug("Time:"+System.nanoTime()+","+"done receiveAck 0x"+String.format("%4x", ack)+ 
        " thread: "+Thread.currentThread().getName());         
        return ret;

    }

    /**
     * Getter dla obiektu {@link #outputStream}.
     * @return obiekt outputStream
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Setter dla obiektu {@link #outputStream}.
     * @param outputStream obiekt dla outputStream.
     */
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * Getter dla obiektu {@link #inputStream}.
     * @return obiekt inputStream
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Setter dla obiektu {@link #inputStream}.
     * @param inputStream obiekt dla inputStream
     */
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Getter dla obiektu {@link #serialPort}.
     * @return obiekt serialPort
     */
    public SerialPort getSerialPort() {
        return serialPort;
    }

    /**
     * Setter dla obiektu {@link #serialPort}.
     * @param serialPort obiekt dla serialPort 
     */
    public void setSerialPort(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    /**
     * Getter dla obiektu {@link #hub}
     * @return obiekt hub
     */
    public Hub getHub() {
        return hub;
    }

    /**
     * Setter dla obiektu {@link #hub}
     * @param hub obiekt dla hub 
     */
    public void setHub(Hub hub) {
        this.hub = hub;
    }

    
    /**
     * Getter dla obiektu {@link #crd}.
     * @return obiekt crd
     */
    public ComReadDispatch getCrd() {
        return crd;
    }

    /**
     * Setter dla obiektu {@link #crd}.
     * @param crd obiekt dla crd
     */
    public void setCrd(ComReadDispatch crd) {
        this.crd = crd;
    }

    /**
     * Getter dla obiektu {@link #radioSession}.
     * @return obiekt radioSession
     */
    public RadioSession getRadioSession() {
        return radioSession;
    }



    /**
     * Implementacja wątku bicia serca koncentratora.
     */
    @Override
    public void run() {
        
        try{
            while (isRunHearbeat()){
                try {
                        Thread.sleep(1000*9);
                }
                catch (InterruptedException ex) {
                        //ignore
                } 
                if (!Thread.interrupted()){
                    sendCommand(Utils.hubIdentifictionReq);
//                try{
                    byte[] res = receiveAck(Utils.hubIdentifictionAck);
                    lgr.debug("Time:"+System.nanoTime()+","+Utils.bytes2long(res, 4));
//                }
//                catch (MeteringSessionTimeoutException e){
                    //ignore it
//                }
                }
               
            }
        }
     catch (MeteringSessionException ex){
               lgr.debug("Time:"+System.nanoTime()+","+"Thread:"+Thread.currentThread().getName()+ex);         
               heartBeatThread=null;
               HubSessionDBManager.getHubSessionManager().closeHubSession(hub);
     }
     finally{
        lgr.debug("Time:"+System.nanoTime()+","+"Thread:"+Thread.currentThread().getName()+" stopped.");            
     }
        
    }

    /**
     * Getter dla pola {@link #runHearbeat}
     * @return wartość runHearbeat
     */
    public boolean isRunHearbeat() {
        return runHearbeat;
    }

    /**
     * Setter dla pola {@link #runHearbeat}
     * @param runHearbeat wartość dla {@link #runHearbeat}
     */
    public void setRunHearbeat(boolean runHearbeat) {
        this.runHearbeat = runHearbeat;
    }

    /**
     * Getter dla pola {@link #heartBeatThread}
     * @return obiekt heartBeatThread
     */
    public Thread getHeartBeatThread() {
        return heartBeatThread;
    }

    /**
     * Setter dla pola {@link #heartBeatThread}
     * @param heartBeatThread obiekt dla heartBeatThread
     */
    public void setHeartBeatThread(Thread heartBeatThread) {
        this.heartBeatThread = heartBeatThread;
    }

    /**
     * Tworzy sesję odczytu danych z pamięci flash loggera, 
     * odczyt jest rozpoczynany od czasu <code>start</code>.
     * @param start czas, od którego są odczytywane dane z pamięci flash koncentratora
     * @return utworzona sesja
     * @throws MeteringSessionException zgłaszany w przypadku błędu komunikacji
     * z koncentratorem
     */
    public LoggerFlashSession createLoggerFlashSession(Timestamp start) throws MeteringSessionException {
        loggerFlashSession = new LoggerFlashSession(this, start);
        return loggerFlashSession;    
    }
    
    /**
     * Zamyka sesję odczytu danych z pamięci flash loggera.
     * @throws MeteringSessionException zgłaszany w przypadku błędu komunikacji
     * z koncentratorem
     */
    public void closeLoggerFlashSession() throws MeteringSessionException {
      if(loggerFlashSession!=null)
        try{
            loggerFlashSession.close();
        }
        finally{
            loggerFlashSession = null;
        }
    }
    
    /**
     * Pobiera za pośrednictwem koncentratora identyfikator loggera,
     * który jest aktualnie zainstalowany na koncentratorze.
     * @return identyfikator loggera, który jest aktualnie zainstalowany na koncentratorze
     * @throws MeteringSessionException 
     */
    public long getLoggerId() throws MeteringSessionException{
        sendCommand(Utils.getLoggerIdReq);
        byte[] data = receiveAck(Utils.getLoggerIdReq);
        return Utils.bytes2long(data, 4);
    }
    
    /**
     * Uruchamia nadawanie danych radiowych przez logger o identyfikatorze <code>loggerId</code>, który
     * jest aktualnie zainstalowany na koncentratorze.
     * @param loggerId identyfikator loggera, dla którego jest uruchamiane nadawanie danych radiowych
     * @throws MeteringSessionException 
     */
    public void enableLoggerRadio(long loggerId) throws MeteringSessionException {
        long currentId=getLoggerId();
        if (currentId!=loggerId)
            throw new MeteringSessionException("Incorrect logger identification number");
        sendCommand(Utils.enableLoggerRadioReq);
        receiveAck(Utils.enableLoggerRadioAck);           
    }

    synchronized int receiveAckWithErrCodeAndSetCR(int ack, ComResp[] rs) throws MeteringSessionException {

        int errCode;
        try{
            rs[0] = crd.getNextResp();
            errCode = rs[0].receiveAckWithErrCode(ack);
        }
        finally{
            canSendCommand=true;
            notifyAll();
        }
        
        return errCode;
    }
    
    public void setSerialPortTimeout(int timeoutMS) throws MeteringSessionException{
        try {
            serialPort.enableReceiveTimeout(timeoutMS);
        } catch (UnsupportedCommOperationException ex) {
            throw new MeteringSessionException(ex);
        }
    }

    public void closeRadioSession() throws MeteringSessionException {
      if(radioSession!=null)
        try{
            radioSession.close();
        }
        finally{
            radioSession = null;
        }    }
}
