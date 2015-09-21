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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import project.data.Datagram;

public class RestMenager {

    private String restServerURL;
    private Hub hub;

    public RestMenager(String restServerURL) {
        this.restServerURL = restServerURL;
    }

    public RestMenager(String restServerURL, Hub hub) {
        this.restServerURL = restServerURL;
        this.hub = hub;
    }

    public String sendDatagram(Datagram datagram) {
        String status = "PENDING";
        JSONObject message = null;
        // Step1: Prepare JSON data
        try {
            message = getHubLogInfo();
            message.put("frames", getDatagramInfos(datagram));
        } catch (JSONException ex) {
            Logger.getLogger(RestMenager.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("\nJSON Object: " + message);
        // Step2: Now pass JSON File Data to REST Service
        try {
            System.out.println("\nURL: " + getRestServerURL());
            URL url = new URL(getRestServerURL());
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
            System.out.println("\nJSON RESPONSE Object: " + response);
            String line = "";
            System.out.println("\nRESPONSE:");
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println("\nREST Service Invoked Successfully..");
            status = parseResponse(response, datagram);
            in.close();
        } catch (Exception e) {
            System.out.println("\nError while calling REST Service");
            System.out.println(e);
        }
        return status;
    }

    private String parseResponse(JSONObject response, Datagram datagram) throws JSONException {
        if(response.getInt("errCode") == 0){
            datagram.setDataSend(true);
        }
        return response.getString("errCode");
    }

    private JSONObject getDatagramInfos(Datagram datagram) throws JSONException {
        JSONObject data = new JSONObject();
        data.put("frame", datagram.getData());
        return data;
    }

    private JSONObject getHubLogInfo() throws JSONException {
        JSONObject header = new JSONObject();
        header.put("hubId", hub.getHubHexId());
        header.put("authKey", SettingsLoader.getHubAuthKey(hub));
        return header;
    }

    /**
     * @return the RestServerURL
     */
    public String getRestServerURL() {
        return restServerURL;
    }

    /**
     * @param RestServerURL the RestServerURL to set
     */
    public void setRestServerURL(String restServerURL) {
        this.restServerURL = restServerURL;
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

}
