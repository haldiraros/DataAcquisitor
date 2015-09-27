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
import REST.operations.RestHubOperations;
import REST.operations.RestMeasurementOperations;
import java.util.Set;
import org.json.JSONObject;
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

    public SendStatistics sendMeasurement(Measurement measurement) throws Exception {
        RestMeasurementOperations rmo = new RestMeasurementOperations();
        return rmo.sendMeasurement(measurement);
    }

    public SendStatistics sendMeasurements(Set<Measurement> measurements) {
        RestMeasurementOperations rmo = new RestMeasurementOperations();
        return rmo.sendMeasurements(measurements);
    }
    
    public JSONObject sendHubStatus() throws Exception {
        RestHubOperations rho = new RestHubOperations();
        return rho.sendHubStatus();
    }

}
