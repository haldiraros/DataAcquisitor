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

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
 
public class ServiceDiscoveryProvider implements Runnable{
        protected boolean stop=false;
        public final static int BROADCAST_PORT = 13131;
        public final static String BROADCAST_GROUP = "230.12.12.12";
 
    public static void main(String[] args) throws IOException, InterruptedException {
 
        ServiceDiscoveryProvider nsp = new ServiceDiscoveryProvider();
        nsp.start();
        
//        Thread.sleep(1000*60);
    }
    private Thread thread;

    public void start(){
        thread = new Thread(this);
        stop=false;
        thread.start();       
    }
    
    public void stop(){
        stop=true;
        thread.interrupt();       
    }
    
    @Override
    public void run() {
        MulticastSocket socket=null;
        InetAddress address = null;
        byte[] buf = new byte[256];
        DatagramPacket resPacket;
        try {
            socket = new MulticastSocket(BROADCAST_PORT);
            address = InetAddress.getByName(BROADCAST_GROUP);
            socket.joinGroup(address);
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            
            while (!stop || Thread.interrupted()){
                socket.receive(packet);
                InetAddress clientAddr = packet.getAddress();
                int clientPort = packet.getPort();
                resPacket = new DatagramPacket(buf, buf.length, clientAddr, clientPort);
                socket.send(packet);
                
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        finally{
            try {
                socket.leaveGroup(address);            
                socket.close();
            } catch (IOException ex) {
                
            }
        }
    }
 
}