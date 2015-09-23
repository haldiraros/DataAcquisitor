/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package REST.operations;

import REST.DatagramsSendStatistics;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import project.data.Datagram;
import project.data.DatagramsUtils;

/**
 *
 * @author hp
 */
public class RestDatagramOperations {

    private final static int maxDatagramsPerFrame = 10;

    public DatagramsSendStatistics sendDatagram(Datagram datagram) throws Exception {
        DatagramsSendStatistics stats = new DatagramsSendStatistics();
        try {
            // Step1: Prepare JSON data
            JSONObject message = RestUtils.getHubLogInfo(datagram.getHubId());
            Set<Datagram> ds  = new HashSet<Datagram>();
            ds.add(datagram);
            fillDatagramsInfos(message, ds);
            System.out.println("\nJSON Object: " + message);
            // Step2: Now pass JSON File Data to REST Service
            JSONObject response = RestUtils.sendToServer(message, RestUtils.getMeasurementsURL());
            int status = response.getInt("errCode");
            if (RestUtils.NoErrorResponse == status) {
                stats.addSendOkCounter();
                datagram.setDataSend(true);
            } else {
                stats.addSendFailsCounter();
                datagram.setNewErrorMessage(Integer.toString(status));
            }
        } catch (Exception e) {
            stats.addSendFailsCounter();
            datagram.setNewErrorMessage(e.getMessage());
        }
        return stats;
    }

    public DatagramsSendStatistics sendDatagrams(Set<Datagram> datagrams) {
        Map<String, Set<Datagram>> mappedDatagrams = DatagramsUtils.sortDatagrams(datagrams);
        DatagramsSendStatistics stats = new DatagramsSendStatistics();
        for (String hubId : mappedDatagrams.keySet()) {
            for (Set<Datagram> datas : DatagramsUtils.splitDatagrams(mappedDatagrams.get(hubId), maxDatagramsPerFrame)) {
                try {
                    // Step1: Prepare JSON data
                    JSONObject message = RestUtils.getHubLogInfo(hubId);
                    fillDatagramsInfos(message, datas);
                    System.out.println("\nJSON Object: " + message);
                    // Step2: Now pass JSON File Data to REST Service
                    JSONObject response = RestUtils.sendToServer(message, RestUtils.getMeasurementsURL());
                    int status = response.getInt("errCode");
                    if (RestUtils.NoErrorResponse == status) {
                        stats.addSendOkCounter(datas.size());
                        datas.stream().forEach((d) -> {
                            d.setDataSend(true);
                        });
                    } else {
                        stats.addSendFailsCounter(datas.size());
                        datas.stream().forEach((d) -> {
                            d.setNewErrorMessage(Integer.toString(status));
                        });
                    }
                } catch (Exception e) {
                    stats.addSendFailsCounter(datas.size());
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