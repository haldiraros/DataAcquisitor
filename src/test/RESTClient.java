/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

/**
 *
 * @author hp
 */
import REST.RestMenager;
import hubLibrary.meteringcomreader.Hub;
import project.data.Datagram;

public class RESTClient {

    public static void main(String[] args) {
        RestMenager mg = new RestMenager("http://jj-blus.rhcloud.com/bluconsole/1.0/resources/measurementbatch");
        mg.setHub(new Hub(100, "LOL"));
        mg.sendDatagram(new Datagram("ALA"));
        
    }
}
