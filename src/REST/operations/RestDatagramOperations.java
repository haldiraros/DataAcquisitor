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
import project.data.Datagram;
import project.data.DatagramsUtils;

/**
 *
 * @author hp
 */
public class RestDatagramOperations {

    public SendStatistics sendDatagram(Datagram datagram) throws Exception {
        SendStatistics stats = new SendStatistics();
        String key = null;
        try {
            key = SettingsLoader.getHubAuthKey(datagram.getHubId());
        } catch (Exception ex) {
            key = null;
        }
        if (key == null) {
            hubGui.logging.Logger.write(Resources.getFormatString("msg.restUtils.noAuthKeyForHub", datagram.getHubId()), LogTyps.ERROR);
            return stats;
        }
        try {
            // Step1: Prepare JSON data
            JSONObject message = RestUtils.getHubLogInfo(datagram.getHubId());
            Set<Datagram> ds = new HashSet<Datagram>();
            ds.add(datagram);
            fillDatagramsInfos(message, ds);
            // Step2: Now pass JSON File Data to REST Service
            JSONObject response = RestUtils.sendToServer(message, RestUtils.getMeasurementsURL());
            int status = response.getInt("errCode");
            if (Config.getInteger("REST.noErrorNumericResponse") == status) {
                stats.addDatagramSendOkCounter();
                datagram.setDataSend(true);
            } else {
                stats.addDatagramSendFailsCounter();
                datagram.setNewErrorMessage(Integer.toString(status));
            }
        } catch (Exception e) {
            stats.addDatagramSendFailsCounter();
            datagram.setNewErrorMessage(e.getMessage());
        }
        return stats;
    }

    public SendStatistics sendDatagrams(Set<Datagram> datagrams) {
        Map<String, Set<Datagram>> mappedDatagrams = DatagramsUtils.sortDatagrams(datagrams);
        SendStatistics stats = new SendStatistics();
        for (String hubId : mappedDatagrams.keySet()) {
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
            for (Set<Datagram> datas : DatagramsUtils.splitDatagrams(mappedDatagrams.get(hubId), Config.getInteger("REST.maxDatagramsPerRestMessage"))) {
                try {
                    // Step1: Prepare JSON data
                    JSONObject message = RestUtils.getHubLogInfo(hubId);
                    fillDatagramsInfos(message, datas);
                    // Step2: Now pass JSON File Data to REST Service
                    JSONObject response = RestUtils.sendToServer(message, RestUtils.getMeasurementsURL());
                    int status = response.getInt("errCode");
                    if (Config.getInteger("REST.noErrorNumericResponse") == status) {
                        stats.addDatagramSendOkCounter(datas.size());
                        datas.stream().forEach((d) -> {
                            d.setDataSend(true);
                        });
                    } else {
                        stats.addDatagramSendFailsCounter(datas.size());
                        datas.stream().forEach((d) -> {
                            d.setNewErrorMessage(Integer.toString(status));
                        });
                    }
                } catch (Exception e) {
                    stats.addDatagramSendFailsCounter(datas.size());
                    datas.stream().forEach((d) -> {
                        d.setNewErrorMessage(e.getMessage());
                    });
                }
            }
        }
        return stats;
    }

    private void fillDatagramsInfos(JSONObject message, Set<Datagram> datagrams) throws JSONException {
        JSONArray datas = new JSONArray();
        JSONArray times = new JSONArray();
        for (Datagram d : datagrams) {
            datas.put(d.getData());
            times.put(d.getDataTimestamp());
        }
        message.put("frames", datas);
        message.put("times", times);
    }

}
