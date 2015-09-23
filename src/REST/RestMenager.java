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
import hubGui.settings.SettingsLoader;
import static hubGui.settings.SettingsLoader.getHubAuthKey;
import hubOperations.HubControl;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import project.data.Datagram;
import project.data.DatagramsUtils;

public class RestMenager {

    private final static int maxDatagramsPerFrame = 10;

    private String measurementsURL;
    private String hubstatusURL;
    private String hubcommandURL;
    private String hubcommandstatusURL;
    private final static int NoErrorResponse = 0;
    private HubControl hubC;

    public RestMenager() {
        this(null);
    }

    public RestMenager(HubControl hubC) {
        this.hubC = hubC;
        setupDefaultURLS();
    }

    private void setupDefaultURLS() {
        measurementsURL = "/bluconsolerest/1.0/resources/measurementbatch";
        hubstatusURL = "/bluconsolerest/1.0/resources/hubstatus";
        hubcommandURL = "/bluconsolerest/1.0/resources/hubcommand";
        hubcommandstatusURL = "/bluconsolerest/1.0/resources/hubcommandstatus";
    }

    private JSONObject getHubLogInfo(String hubId) throws JSONException {
        JSONObject header = new JSONObject();
        header.put("hubId", hubId);
        try {
            header.put("authKey", getHubAuthKey(hubId));
        } catch (Exception ex) {
            Logger.getLogger(RestMenager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return header;
    }

    public DatagramsSendStatistics sendDatagram(Datagram datagram) throws Exception {
        DatagramsSendStatistics stats = new DatagramsSendStatistics();
        try {
            // Step1: Prepare JSON data
            JSONObject message = getHubLogInfo(datagram.getHubId());
            message.put("frames", getDatagramInfos(datagram));
            System.out.println("\nJSON Object: " + message);
            // Step2: Now pass JSON File Data to REST Service
            JSONObject response = sendToServer(message, SettingsLoader.load().getRestUrl() + measurementsURL);
            int status = response.getInt("errCode");
            if (NoErrorResponse == status) {
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

    private JSONArray getDatagramInfos(Datagram datagram) throws JSONException {
        JSONArray datas = new JSONArray();
        datas.put(datagram.getData());
        return datas;
    }

    public String ReadBigStringIn(BufferedReader buffIn) throws IOException {
        StringBuilder everything = new StringBuilder();
        String line;
        while ((line = buffIn.readLine()) != null) {
            everything.append(line);
        }
        System.out.println(everything.toString());
        return everything.toString();
    }

    private JSONObject sendToServer(JSONObject message, String service) throws IOException, JSONException {
        URL url = new URL(service);
        System.out.println("Creating connection");
        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        System.out.println("Creating OutputStreamWriter");
        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
        System.out.println("Writng Message");
        out.write(message.toString());
        System.out.println("out.close");
        out.close();

        System.out.println("Creating InputStreamReader");
        InputStreamReader isr = new InputStreamReader(connection.getInputStream());
        System.out.println("Creating BufferedReader");
        BufferedReader in = new BufferedReader(isr);
        System.out.println("Creating JSONObject response");
        JSONObject response = new JSONObject(ReadBigStringIn(in));
        in.close();
        return response;
    }

    public DatagramsSendStatistics sendDatagrams(Set<Datagram> datagrams) {
        Map<String, Set<Datagram>> mappedDatagrams = DatagramsUtils.sortDatagrams(datagrams);
        DatagramsSendStatistics stats = new DatagramsSendStatistics();
        for (String hubId : mappedDatagrams.keySet()) {
            for (Set<Datagram> datas : DatagramsUtils.splitDatagrams(mappedDatagrams.get(hubId), maxDatagramsPerFrame)) {
                try {
                    // Step1: Prepare JSON data
                    JSONObject message = getHubLogInfo(hubId);
                    message.put("frames", getDatagramsInfos(datas));
                    System.out.println("\nJSON Object: " + message);
                    // Step2: Now pass JSON File Data to REST Service
                    JSONObject response = sendToServer(message, SettingsLoader.load().getRestUrl() + measurementsURL);
                    int status = response.getInt("errCode");
                    if (NoErrorResponse == status) {
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

    private JSONArray getDatagramsInfos(Set<Datagram> datagrams) throws JSONException {
        JSONArray datas = new JSONArray();
        for (Datagram d : datagrams) {
            datas.put(d.getData());
        }
        return datas;
    }

    public JSONObject sendHubStatus(HubControl hc) throws JSONException, IOException, Exception {
        // Step1: Prepare JSON data
        JSONObject status = getHubLogInfo(hc.getHubId());
        status.put("firmwareVer", "1.0");
        status.put("hardwareVer", "1.0");
        status.put("dateTime", "FFFF");
        JSONObject message = new JSONObject();
        message.put("status", status);
        System.out.println("\nJSON Object: " + message);
        // Step2: Now pass JSON File Data to REST Service
        return sendToServer(message, SettingsLoader.load().getRestUrl() + hubstatusURL);
    }

    public JSONObject getHubCommand(HubControl hc) throws JSONException, IOException, Exception {
        // Step1: Prepare JSON data
        JSONObject message = getHubLogInfo(hc.getHubId());
        System.out.println("\nJSON Object: " + message);
        // Step2: Now pass JSON File Data to REST Service
        return sendToServer(message, SettingsLoader.load().getRestUrl() + hubcommandURL);
    }

    public JSONObject sendHubCommandStatus(HubControl hc, Long commandId, String commandStatus) throws JSONException, IOException, Exception {
        // Step1: Prepare JSON data
        JSONObject message = getHubLogInfo(hc.getHubId());
        JSONObject command = new JSONObject();
        command.put("id", commandId);
        command.put("status", commandStatus);
        message.put("command", command);
        System.out.println("\nJSON Object: " + message);
        // Step2: Now pass JSON File Data to REST Service
        return sendToServer(message, SettingsLoader.load().getRestUrl() + hubcommandstatusURL);
    }

    /**
     * @return the measurementsURL
     */
    public String getMeasurementsURL() {
        return measurementsURL;
    }

    /**
     * @param measurementsURL the measurementsURL to set
     */
    public void setMeasurementsURL(String measurementsURL) {
        this.measurementsURL = measurementsURL;
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

    /**
     * @return the hubstatusURL
     */
    public String getHubstatusURL() {
        return hubstatusURL;
    }

    /**
     * @param hubstatusURL the hubstatusURL to set
     */
    public void setHubstatusURL(String hubstatusURL) {
        this.hubstatusURL = hubstatusURL;
    }

    /**
     * @return the hubcommandURL
     */
    public String getHubcommandURL() {
        return hubcommandURL;
    }

    /**
     * @param hubcommandURL the hubcommandURL to set
     */
    public void setHubcommandURL(String hubcommandURL) {
        this.hubcommandURL = hubcommandURL;
    }

    /**
     * @return the hubcommandstatusURL
     */
    public String getHubcommandstatusURL() {
        return hubcommandstatusURL;
    }

    /**
     * @param hubcommandstatusURL the hubcommandstatusURL to set
     */
    public void setHubcommandstatusURL(String hubcommandstatusURL) {
        this.hubcommandstatusURL = hubcommandstatusURL;
    }

}
