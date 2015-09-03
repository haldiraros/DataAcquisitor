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

import oracle.jdbc.dcn.DatabaseChangeRegistration;

/**
 * Reprezentuje koncentrator urządzeń A4P.
 * @author Juliusz Jezierski
 */
public class Hub {
    /**
     * Heksadecymalny identyfikator koncentratora wraz obszarem zastosowania
     * zakodowanym na dwóch najstarszych bajtach
     */
    protected String hubId;
    
    /**
     * Nazwa portu, do którego podłączony jest koncentrator
     */
    protected String comPortName;

    /**
     * Obiekt umożliwiający rejestrowanie listenerów
     * na zdarzenia modyfikacji bazy danych
     */    
    public DatabaseChangeRegistration getDCR() {
        return dcr;
    }

    public void setDCR(DatabaseChangeRegistration dcr) {
        this.dcr = dcr;
    }
    
    protected DatabaseChangeRegistration dcr;

    /**
     * Konstruuje obiekt koncentratora
     * @param hubId  identyfikator koncentratora
     * @param comPortName nazwa portu, do którego podłączony jest koncentrator
     */
    public Hub(long hubId, String comPortName){
        this.hubId=convertHubId2Hex(hubId);
        this.comPortName=comPortName;
    }
    /**
     * Konstruuje obiekt koncentratora
     * @param hexHubId heksadecymalny identyfikator koncentratora
     * @param comPortName nazwa portu, do którego podłączony jest koncentrator
     */
    public Hub(String hexHubId, String comPortName){
        this.hubId=hexHubId;
        this.comPortName=comPortName;
    }    
        
    /** 
     * Zwraca nazwę portu, do którego podłączony jest koncentrator
     * @return nazwa portu
     */
    public String getComPortName() {
        return comPortName;
    }

    /** 
     * Ustawia nazwę portu, do którego podłączony jest koncentrator
     * @param comPortName nazwa portu
     */
    public void setComPortName(String comPortName) {
        this.comPortName = comPortName;
    }

    /**
     * Konwertuje numeryczny identyfikator na heksadecymalny identyfikator koncentratora
     * doklejając jako 2 najstarsze bajty obszar zastosowania 0x454D
     * @param hubId numeryczny identyfikator koncentratora
     * @return heksadecymalny identyfikator koncentratora
     */
    static String convertHubId2Hex(long hubId){
           long fullLoggerId= hubId | 0x4D4500000000L; //"454D" 
           String hexHubId=
                   String.format("%12X", fullLoggerId);           
           return hexHubId;        
    }
    
    /**
     * Zwraca heksadecymalny identyfikator koncentratora
     * @return heksadecymalny identyfikator koncentratora
     */
    public String getHubHexId() {
        return hubId;
    }
}
