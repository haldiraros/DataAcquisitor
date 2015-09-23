/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package REST;

/**
 *
 * @author hp
 */
import REST.operations.RestDatagramOperations;
import hubOperations.HubControl;
import java.util.Set;
import project.data.Datagram;

public class RestMenager {

    private HubControl hubC;

    public RestMenager() {
        this(null);
    }

    public RestMenager(HubControl hubC) {
        this.hubC = hubC;
    }
    
    /**
     * @return the hub
     */
    public HubControl getHubControl() {
        return hubC;
    }

    /**
     * @param hub the hub to set
     */
    public void setHubControl(HubControl hub) {
        this.hubC = hub;
    }

    public DatagramsSendStatistics sendDatagrams(Set<Datagram> datagrams) {
        RestDatagramOperations rdo = new RestDatagramOperations();
        return rdo.sendDatagrams(datagrams);
    }

    public DatagramsSendStatistics sendDatagram(Datagram datagram) throws Exception {
        RestDatagramOperations rdo = new RestDatagramOperations();
        return rdo.sendDatagram(datagram);
    }

}
