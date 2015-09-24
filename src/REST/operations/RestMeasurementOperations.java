/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package REST.operations;

import REST.SendStatistics;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import project.data.Measurement;
import project.data.MeasurementUtils;

/**
 *
 * @author hp
 */
public class RestMeasurementOperations {

    private final static int maxMeasurementsPerFrame = 10;

    public SendStatistics sendMeasurement(Measurement measurement) throws Exception {
        SendStatistics stats = new SendStatistics();
        try {
            // Step1: Prepare JSON data
            JSONObject message = RestUtils.getHubLogInfo(measurement.getHubId());
            Set<Measurement> ds = new HashSet<Measurement>();
            ds.add(measurement);
            fillMeasurementsInfos(message, ds);
            System.out.println("\nJSON Object: " + message);
            // Step2: Now pass JSON File Data to REST Service
            JSONObject response = RestUtils.sendToServer(message, RestUtils.getPlainmeasurementbatchURL());
            int status = response.getInt("errCode");
            if (RestUtils.NoErrorResponse == status) {
                stats.addMeasurementSendOkCounter();
                measurement.setDataSend(true);
            } else {
                stats.addMeasurementSendFailsCounter();
                measurement.setNewErrorMessage(Integer.toString(status));
            }
        } catch (Exception e) {
            stats.addMeasurementSendFailsCounter();
            measurement.setNewErrorMessage(e.getMessage());
        }
        return stats;
    }

    public SendStatistics sendMeasurements(Set<Measurement> measurements) {
        Map<String, Set<Measurement>> mappedMeasurements = MeasurementUtils.sortMeasurements(measurements);
        SendStatistics stats = new SendStatistics();
        for (String hubId : mappedMeasurements.keySet()) {
            for (Set<Measurement> datas : MeasurementUtils.splitMeasurements(mappedMeasurements.get(hubId), maxMeasurementsPerFrame)) {
                try {
                    // Step1: Prepare JSON data
                    JSONObject message = RestUtils.getHubLogInfo(hubId);
                    fillMeasurementsInfos(message, datas);
                    System.out.println("\nJSON Object: " + message);
                    // Step2: Now pass JSON File Data to REST Service
                    JSONObject response = RestUtils.sendToServer(message, RestUtils.getPlainmeasurementbatchURL());
                    int status = response.getInt("errCode");
                    if (RestUtils.NoErrorResponse == status) {
                        stats.addMeasurementSendOkCounter(datas.size());
                        datas.stream().forEach((d) -> {
                            d.setDataSend(true);
                        });
                    } else {
                        stats.addMeasurementSendFailsCounter(datas.size());
                        datas.stream().forEach((d) -> {
                            d.setNewErrorMessage(Integer.toString(status));
                        });
                    }
                } catch (Exception e) {
                    stats.addMeasurementSendFailsCounter(datas.size());
                    datas.stream().forEach((d) -> {
                        d.setNewErrorMessage(e.getMessage());
                    });
                }
            }
        }
        return stats;
    }

    private void fillMeasurementsInfos(JSONObject message, Set<Measurement> measurements) throws JSONException {
        JSONArray datas = new JSONArray();
        for (Measurement m : measurements) {
            JSONArray values = new JSONArray();
            for(int i=0; i<m.getMeasurments().length; i++){
                values.put(m.getMeasurments()[i]);
            }
            JSONObject measure = new JSONObject();
            measure.put("logerId", m.getLoggerId());
            measure.put("startTime", m.getMeasurmentTime());
            measure.put("measures", values);
            datas.put(measure);
        }
        message.put("frames", datas);
    }

}
