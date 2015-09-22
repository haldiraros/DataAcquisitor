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
package hubGui.settings;

/**
 *
 * @author Haros
 */
public class ProxySetter {
    
    public static void setProxy(String host,String port, String user, String pass){
        
        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", port);
        System.setProperty("http.proxyUserName", user);
        System.setProperty("http.proxyPassword", pass);
        
        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", port);
        System.setProperty("https.proxyUserName", user);
        System.setProperty("https.proxyPassword", pass);
        
        
            
        
        
    }
    
    public static void unsetProxy(){
        System.clearProperty("http.proxyHost");
        System.clearProperty("https.proxyHost");
    }
    
}
