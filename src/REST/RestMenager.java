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
import java.util.Set;
import project.data.Datagram;
import project.data.Measurement;

public class RestMenager {

    public SendStatistics sendDatagrams(Set<Datagram> datagrams) {
        RestDatagramOperations rdo = new RestDatagramOperations();
        return rdo.sendDatagrams(datagrams);
    }

    public SendStatistics sendDatagram(Datagram datagram) throws Exception {
        RestDatagramOperations rdo = new RestDatagramOperations();
        return rdo.sendDatagram(datagram);
    }

    public SendStatistics sendMeasurement(Measurement measurement) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public SendStatistics sendMeasurements(Set<Measurement> measurementsToSend) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
