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
import java.util.Map;

/**
 * Reprezentuje kontener koncentratorów.
 * @author Juliusz Jezierski
 */
public class Hubs extends HashMap<String, Hub>{


    /**
     * Konstruuje pusty kontener koncentratorów o początkowej objętości 16. 
     */
    public Hubs() {
        super();
    }
    
    /**
     * Konstruuje pusty kontener koncentratorów o wskazanej początkowej liczebności.
     * @param i początkowa objętość
     */
    public Hubs(int i) {
        super(i);
    }


    /**
     * Zwraca obiekt koncentratora na postawie jego heksadecymalnego identyfikatora.
     * @param hubNo heksadecymalny identyfikator koncentratora
     * @return obiekt koncentratora
     * @throws MeteringSessionException zgłaszany w przypadku nie znalezienia koncentratora
     */
    public Hub getHub(String hubNo) throws MeteringSessionException {
        Hub h = super.get(hubNo);
        if (h==null) throw new MeteringSessionException("Hub number:"+hubNo+" no found");
        return h;
     
    }
    

    
    

}
