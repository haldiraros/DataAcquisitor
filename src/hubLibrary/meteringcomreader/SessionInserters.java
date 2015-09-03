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

import java.util.HashMap;
import java.util.Map;
import hubLibrary.meteringcomreader.SessionInserter;

/**
 *
 * @author Juliusz
 */
public class SessionInserters extends HashMap<String, SessionInserter> {
    
    public  void addInserter(String hubId, SessionInserter ins){
        put(hubId, ins);
    }
    public  SessionInserter getInserter(String hubId){
        return get(hubId);
    }
    public  SessionInserter removeInserter(String hubId){
        return remove(hubId);
    }
    public SessionInserters(Map<? extends String, ? extends SessionInserter> map) {
        super(map);
    }

    public SessionInserters() {
    }

    public SessionInserters(int i) {
        super(i);
    }

    public SessionInserters(int i, float f) {
        super(i, f);
    }
    
    
}
