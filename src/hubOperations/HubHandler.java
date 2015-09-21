/*
 * Copyright (C) 2015 Haros
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
package hubOperations;

import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;

/**
 *
 * @author Haros
 */
public class HubHandler {
    private static volatile HubHandler instance = null;
    
    private HubControl hubControl = null;

    public HubControl getHubControl() {
        return hubControl;
    }
    
    private HubHandler() throws MeteringSessionException {
        hubControl = new HubControl();
       
    }
    
    public static synchronized HubHandler getInstance() throws MeteringSessionException {
        if (instance == null) {
            instance = new HubHandler();
        }

        return instance;
    }


    
}
