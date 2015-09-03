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

package test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Juliusz
 */
public class ServiceDiscoveryClient {
    public static final int DISCOVERY_TIMEOUT=100;
    public static final int MAX_DISCOVERED_SEVICES = 5;
    public static List<InetAddress> discoverService(){
        LinkedList<InetAddress> list = new LinkedList<InetAddress>();
        try {            
            DatagramSocket socket;
            socket = new DatagramSocket(ServiceDiscoveryProvider.BROADCAST_PORT+1);
            socket.setSoTimeout(DISCOVERY_TIMEOUT);
            InetAddress group = InetAddress.getByName(ServiceDiscoveryProvider.BROADCAST_GROUP);
            
            byte[] buf ;
            buf = "ELLO".getBytes();
            
            for (int i=0; i<MAX_DISCOVERED_SEVICES; i++){
               try{
                   DatagramPacket packet = new DatagramPacket(buf, buf.length, group, ServiceDiscoveryProvider.BROADCAST_PORT);
                   socket.send(packet);
                   while(true){
                     socket.receive(packet);
                     InetAddress providerAddress=packet.getAddress();
                     if (!list.contains(providerAddress))
                         list.add(providerAddress);                
                   }                
               }catch(SocketTimeoutException e){
                   //try again
               }
               
            }


        } catch (UnknownHostException ex) {
            Logger.getLogger(ServiceDiscoveryClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SocketException ex) {
            Logger.getLogger(ServiceDiscoveryClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ServiceDiscoveryClient.class.getName()).log(Level.SEVERE, null, ex);
        }
           
            
            return list;
    }
    
    public static void main(String[] arg){
        List<InetAddress> list = ServiceDiscoveryClient.discoverService();
        for(InetAddress l :list){
            System.out.println(l.getHostAddress());
            System.out.println(l.getHostName());
        }
    }
    
}
