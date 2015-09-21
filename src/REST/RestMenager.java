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
import hubLibrary.meteringcomreader.Hub;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import project.data.Datagram;

public class RestMenager {

    private String measurementsURL;
    private String hubstatusURL;
    private String hubcommandURL;
    private String hubcommandstatusURL;
    private final static String NoErrorResponse = "0";
    private Hub hub;

    public RestMenager(String measurementsURL) {
        this.measurementsURL = measurementsURL;
    }

    public RestMenager(String measurementsURL, Hub hub) {
        this.measurementsURL = measurementsURL;
        this.hub = hub;
    }

    private JSONObject getHubLogInfo() throws JSONException {
        JSONObject header = new JSONObject();
        header.put("hubId", hub.getHubHexId());
        header.put("authKey", SettingsLoader.getHubAuthKey(hub));
        return header;
    }

    public String sendDatagram(Datagram datagram) {
        try {
            // Step1: Prepare JSON data
            JSONObject message = getHubLogInfo();
            message.put("frames", getDatagramInfos(datagram));
            System.out.println("\nJSON Object: " + message);
            // Step2: Now pass JSON File Data to REST Service
            JSONObject response = sendToServer(message, measurementsURL);
            String status = response.getString("errCode");
            if (NoErrorResponse.equals(status)) {
                datagram.setDataSend(true);
            }
            return status;
        } catch (Exception e) {
            System.out.println("\nError while calling REST Service");
            System.out.println(e);
            return e.getMessage();
        }
    }

    private JSONArray getDatagramInfos(Datagram datagram) throws JSONException {
        JSONObject data = new JSONObject();
        JSONArray datas = new JSONArray();
        data.put("frame", datagram.getData());
        datas.put(data);
        return datas;
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
        JSONObject response = new JSONObject(isr.toString());
        in.close();
        return response;
    }

    public String sendDatagrams(Collection<Datagram> datagrams) {
        try {
            // Step1: Prepare JSON data
            JSONObject message = getHubLogInfo();
            message.put("frames", getDatagramsInfos(datagrams));
            System.out.println("\nJSON Object: " + message);
            // Step2: Now pass JSON File Data to REST Service
            JSONObject response = sendToServer(message, measurementsURL);
            String status = response.getString("errCode");
            if (NoErrorResponse.equals(status)) {
                for (Datagram d : datagrams) {
                    d.setDataSend(true);
                }
            }
            return status;
        } catch (Exception e) {
            System.out.println("\nError while calling REST Service");
            System.out.println(e);
            return e.getMessage();
        }
    }

    private JSONArray getDatagramsInfos(Collection<Datagram> datagrams) throws JSONException {
        JSONArray datas = new JSONArray();
        for (Datagram d : datagrams) {
            JSONObject data = new JSONObject();
            data.put("frame", d.getData());
            datas.put(data);
        }
        return datas;
    }

    public JSONObject sendHubStatus() throws JSONException, IOException {
        // Step1: Prepare JSON data
        JSONObject status = getHubLogInfo();
        status.put("firmwareVer", "1.0");
        status.put("hardwareVer", "1.0");
        status.put("dateTime", hub.toString());
        JSONObject message = new JSONObject();
        message.put("status", status);
        System.out.println("\nJSON Object: " + message);
        // Step2: Now pass JSON File Data to REST Service
        return sendToServer(message, hubstatusURL);
    }

    public JSONObject getHubCommand() throws JSONException, IOException {
        // Step1: Prepare JSON data
        JSONObject message = getHubLogInfo();
        System.out.println("\nJSON Object: " + message);
        // Step2: Now pass JSON File Data to REST Service
        return sendToServer(message, hubcommandURL);
    }

    public JSONObject sendHubCommandStatus(Long commandId, String commandStatus) throws JSONException, IOException {
        // Step1: Prepare JSON data
        JSONObject message = getHubLogInfo();
        JSONObject command = new JSONObject();
        command.put("id", commandId);
        command.put("status", commandStatus);
        message.put("command", command);
        System.out.println("\nJSON Object: " + message);
        // Step2: Now pass JSON File Data to REST Service
        return sendToServer(message, hubcommandstatusURL);
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
    public Hub getHub() {
        return hub;
    }

    /**
     * @param hub the hub to set
     */
    public void setHub(Hub hub) {
        this.hub = hub;
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
