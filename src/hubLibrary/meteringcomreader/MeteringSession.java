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

import hubLibrary.meteringcomreader.exceptions.MeteringSessionCRCException;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionFlashLoggerTransException;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionTimeoutException;

/**
 * Reprezentuje abstrakcyjny obiekt sesji w bieżącym połączeniu 
 * z koncentratorem.
 * @author Juliusz Jezierski
 */

abstract public class MeteringSession {
    
    /**
     * Obiekt połączenia z koncentratorem.
     */
    protected HubConnection hc;

    /**
     * Inicjuje abstrakcyjny obiekt sesji ustawiając połączenie z koncentratorem.
     * i wysyłając polecenie rozpoczynające abstrakcyjną sesję
     * @param hc bieżące połączenie z koncentratorem
     * @param command  polecenie rozpoczynające abstrakcyjną sesję
     * @throws MeteringSessionException zgłaszany w przypadku niepowodzenia
     * wysłania polecenia lub odebranie potwierdzenia rozpoczęcia abstrakcyjnej
     * sesji
     */
    public MeteringSession(HubConnection hc, int command) throws MeteringSessionException{
            this.hc=hc;
            hc.sendCommand(command);
            hc.receiveAck(command);
    }
    
    /**
     * Inicjuje abstrakcyjny obiekt sesji ustawiając połączenie z koncentratorem.
     * @param hc ustawia bieżące połączenie z koncentratorem
     */
    public MeteringSession(HubConnection hc) {
            this.hc=hc;
    }
    
    /**
     * Zamyka połączenie abstrakcyjnej sesji.
     * @throws MeteringSessionException zgłaszany w przypadku niepowodzenia
     * wysłania polecenia lub odebrania potwierdzenia zakończenia sesję 
     */
    abstract public void close() throws MeteringSessionException;
    
    /**
     * Pobiera kolejny pakiet danych w abstrakcyjnej sesji.
     * @return pobrany pakiet danych
     * @throws MeteringSessionException zwracany w przypadku niepowodzenia
     * pobrania pakietu danych
     */
    abstract public DataPacket getNextPacket() throws MeteringSessionException;
    


   /**
     * Ponownie pobiera kolejny pakiet danych w abstrakcyjnej sesji.
     * @return pobrany pakiet danych
     * @throws MeteringSessionException zwracany w przypadku niepowodzenia
     * pobrania pakietu danych
     */    
    abstract public DataPacket regetPrevPacket() throws MeteringSessionException;

    /**
     * Ponownie pobiera kolejny pakiet danych w abstrakcyjnej sesji.
     * @param maxRetries maksymalna liczba powtórzeń w przypdku niepowodzenia,
     * po której zgłaszany jest wyjątek
     * @return pobrany pakiet danych
     * @throws MeteringSessionException zwracany w przypadku niepowodzenia
     * pobrania pakietu danych
     */
    public DataPacket getNextPacket(int maxRetries) throws MeteringSessionException {
           boolean go=true;
           DataPacket packet=null;
           int retries=0;
           while (go){
               try{
                if (retries==0)
                    packet = getNextPacket();
                else
                    packet = regetPrevPacket();                    
               go = false;

               }catch (MeteringSessionTimeoutException e){
                   if (retries==maxRetries)
                       throw e;
                   retries++; 
               }catch (MeteringSessionCRCException e){
                   if (retries==maxRetries)
                       throw e;
                   retries++;                    
               }catch (MeteringSessionFlashLoggerTransException e){
                   if (retries==maxRetries)
                       throw e;
                   retries++;                                       
               }
           }
           return packet;
    }     
    
}
