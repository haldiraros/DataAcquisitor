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

public class RESTClient {

    public static void main(String[] args) {
        RestMenager mg = new RestMenager();
        //Marcin B. Zmieniłem parę rzeczy w RESTManager i nie trzyma samego huba już, w razie co można zmienić potem....
        //mg.setHub(new Hub(100, "LOL"));
        //mg.sendDatagram(new Datagram("ALA"));
        
    }
}
