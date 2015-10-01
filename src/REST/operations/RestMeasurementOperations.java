/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package REST.operations;

import REST.SendStatistics;
import hubGui.i18n.Resources;
import hubGui.logging.LogTyps;
import hubGui.settings.SettingsLoader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import project.Config;
import project.data.Measurement;
import project.data.MeasurementUtils;

/**
 *
 * @author hp
 */
public class RestMeasurementOperations {


    public SendStatistics sendMeasurement(Measurement measurement) throws Exception {
        SendStatistics stats = new SendStatistics();
        String key = null;
        try {
            key = SettingsLoader.getHubAuthKey(measurement.getHubId());
        } catch (Exception ex) {
            key = null;
        }
        if (key == null) {
            hubGui.logging.Logger.write(Resources.getFormatString("msg.restUtils.noAuthKeyForHub", measurement.getHubId()), LogTyps.ERROR);
            return stats;
        }
        try {
            // Step1: Prepare JSON data
            JSONObject message = RestUtils.getHubLogInfo(measurement.getHubId());
            Set<Measurement> ds = new HashSet<Measurement>();
            ds.add(measurement);
            fillMeasurementsInfos(message, ds);
            System.out.println("\nJSON Object: " + message);
            // Step2: Now pass JSON File Data to REST Service
            JSONObject response = RestUtils.sendToServer(message, RestUtils.getPlainMeasurementBatchURL());
            int status = response.getInt("errCode");
            if (RestUtils.noErrorResponse == status) {
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
            String key = null;
            try {
                key = SettingsLoader.getHubAuthKey(hubId);
            } catch (Exception ex) {
                key = null;
            }
            if (key == null) {
                hubGui.logging.Logger.write(Resources.getFormatString("msg.restUtils.noAuthKeyForHub", hubId), LogTyps.ERROR);
                continue;
            }
            for (Set<Measurement> datas : MeasurementUtils.splitMeasurements(mappedMeasurements.get(hubId), Config.getInteger("maxMeasurementsPerRestMessage"))) {
                try {
                    // Step1: Prepare JSON data
                    JSONObject message = RestUtils.getHubLogInfo(hubId);
                    fillMeasurementsInfos(message, datas);
                    System.out.println("\nJSON Object: " + message);
                    // Step2: Now pass JSON File Data to REST Service
                    JSONObject response = RestUtils.sendToServer(message, RestUtils.getPlainMeasurementBatchURL());
                    int status = response.getInt("errCode");
                    if (RestUtils.noErrorResponse == status) {
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
            JSONObject measure = new JSONObject();
            measure.put("logerId", m.getLoggerId());
            measure.put("startTime", m.getMeasurmentTime());
            measure.put("period", m.getPeriod());
            measure.put("measures", m.getData());
            datas.put(measure);
        }
        message.put("measurements", datas);
    }

}
