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

import hubGui.i18n.Resources;
import hubGui.logging.LogTyps;
import hubGui.logging.Logger;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import project.data.Session;

/**
 *
 * @author Haros
 */
public class HubHandler {
    private static volatile HubHandler instance = null;
    
    private HubControl hubControl = null;
    private Session dbSession = null;
    private ScheduledExecutorService exec;
    
    /**
     * Funkcja zwracająca obiekt kontroli HUBa
     * @return obiekt kontroli HUBa
     */
    public HubControl getHubControl() {
        return hubControl;
    }
    /**
     * Konstruktor obiektu obsługującego Hub.
     * Dodatkowo tworzony jest wątek odpowiedzialny za wysyłanie statusu HUBa co 5 minut.
     * @throws MeteringSessionException 
     */
    private HubHandler() throws MeteringSessionException {
        hubControl = new HubControl();
        
         exec = Executors.newSingleThreadScheduledExecutor();
         exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    Session dbSession = hubControl.getDbSession();
                    if(dbSession!=null) dbSession.sendHubStatus();
                } catch (Exception ex) {
                    Logger.write(Resources.getString("msg.hubHandler.errorSubmitingHubStatus"), LogTyps.ERROR);
                }
            }
        }, 1, 4, TimeUnit.MINUTES);
       
    }
    /**
     * Funkcja zamykająca wątek wysyłający stan Huba
     */
    public void shutdown(){
        if (exec != null) {
            exec.shutdown();
        }
        instance = null;
    }
    /**
     * Getter singletonowej instancji obiektu zarzadzajacego HUBem
     * @return
     * @throws MeteringSessionException 
     */
    public static synchronized HubHandler getInstance() throws MeteringSessionException {
        if (instance == null) {
            instance = new HubHandler();
        }

        return instance;
    }
    
    /**
     * Funkcja ustawiająca odwołanie do obiektu Sesji z DB
     * @param dbSession 
     */
    public void setDBSession(Session dbSession) {
        this.dbSession = dbSession;
        hubControl.setDbSession(dbSession);
    }
    
    public Session getDBSession(){
        return this.dbSession;
    }


    
}
