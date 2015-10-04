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

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 *
 * @author Haros
 */
public class ProxySetter {
    
    public static void setProxy(String host,String port, String user, String pass){
        
        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", port);
        
        if(user!=null && user!="") 
            Authenticator.setDefault( new BasicAuthenticator(user, pass));

        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", port); 
    }
    
    public static void unsetProxy(){
        System.clearProperty("http.proxyHost");
        System.clearProperty("https.proxyHost");
    }
    
    static class BasicAuthenticator extends Authenticator {
        String baName;
        String baPassword;
        private BasicAuthenticator(String baName1, String baPassword1) {
            baName = baName1;
            baPassword = baPassword1;
        }
        @Override
            public PasswordAuthentication getPasswordAuthentication() {
                //System.out.println("Authenticating...");
                return new PasswordAuthentication(baName, baPassword.toCharArray());
            }
    }
    
}
