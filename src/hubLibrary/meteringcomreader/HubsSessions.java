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

import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Reprezentuje kontener połączeń do koncentratorów.  
 * @author Juliusz Jezierski
 */
public class HubsSessions extends HashMap<String, HubConnection>{

    /**
     * Konstruuje pusty kontener połączeń do koncentratorów o początkowej objętości 16.
     */
    public HubsSessions() {
        super();
    }

    /**
     * Konstruuje pusty kontener koncentratorów o wskazanej początkowej liczebności.
     * @param i początkowa objętość
     */
    public HubsSessions(int i) {
        super(i);
    }

    /**
     * Zwraca obiekt połączenia do koncentratora na podstawie heksadecymalnego 
     * identyfikatora koncentratora.
     * @param hubNo heksadecymalny identyfikator koncentratora
     * @return obiekt połączenia do koncentratora 
     * @throws MeteringSessionException zgłaszany w przypadku nie znalezienia 
     */
    public HubConnection getHubConnection(String hubNo) throws MeteringSessionException {
        HubConnection hc = get(hubNo);
        if (hc==null) 
            throw new MeteringSessionException("Hub Connection for hub no "+hubNo+" no found");
        return hc;
    }
    
    /**  
     * Sprawdza czy w kontenerze znajduje się połączenie do koncentratora 
     * wykorzystujące wskazany port komunikacyjny, co oznacza, że port jest
     * wykorzystywany
     * @param serialPortName nazwa portu komunikacyjnego
     * @return true jeżeli port jest wykorzystywany przez połączenie do dowolnego
     * koncentratora
     */
    public boolean isPortUsed(String serialPortName){
        boolean ret=false;
        Set<Map.Entry<String, HubConnection>> connectionSet= this.entrySet();
        Iterator<Map.Entry<String, HubConnection>> it = connectionSet.iterator();
        while(it.hasNext()){
            Map.Entry<String, HubConnection> pair= it.next();
            HubConnection hc= pair.getValue();
            if (hc.hub.comPortName.equals(serialPortName))
                ret = true;
        }        
        return ret;
    }
        
}
