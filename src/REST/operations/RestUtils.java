/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package REST.operations;

import hubGui.i18n.Resources;
import hubGui.logging.LogTyps;
import hubGui.settings.SettingsLoader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author hp
 */
public class RestUtils {

    private final static String measurementsURL = "/bluconsolerest/1.0/resources/measurementbatch";
    private final static String plainMeasurementBatchURL = "/bluconsolerest/1.0/resources/plainmeasurementbatch";
    private final static String hubStatusURL = "/bluconsolerest/1.0/resources/hubstatus";
    private final static String hubCommandURL = "/bluconsolerest/1.0/resources/hubcommand";
    private final static String hubCommandStatusURL = "/bluconsolerest/1.0/resources/hubcommandstatus";
    public final static int noErrorResponse = 0;

    public static JSONObject getHubLogInfo(String hubId) throws JSONException, Exception {
        JSONObject header = new JSONObject();
        header.put("hubId", hubId);
        try {
            String key = SettingsLoader.getHubAuthKey(hubId);
            if (key == null) {
                hubGui.logging.Logger.write(Resources.getFormatString("msg.restUtils.noAuthKeyForHub", hubId), LogTyps.ERROR);
            } else {
                header.put("authKey", key);
            }
        } catch (Exception ex) {
            hubGui.logging.Logger.write(Resources.getFormatString("msg.restUtils.errorOnAuthKeySearch", hubId), LogTyps.ERROR);
            throw ex;
        }
        return header;
    }

    public static String readBigStringIn(BufferedReader buffIn) throws IOException {
        StringBuilder everything = new StringBuilder();
        String line;
        while ((line = buffIn.readLine()) != null) {
            everything.append(line);
        }
        System.out.println(everything.toString());
        return everything.toString();
    }
    
    public static JSONObject sendToServer(JSONObject message, String service) throws IOException, JSONException {
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
        JSONObject response = new JSONObject(readBigStringIn(in));
        in.close();
        return response;
    }

    /**
     * @return the measurementsURL
     */
    public static String getMeasurementsURL() throws Exception {
        return SettingsLoader.load().getRestUrl() + measurementsURL;
    }

    /**
     * @return the hubstatusURL
     */
    public static String getHubStatusURL() throws Exception {
        return SettingsLoader.load().getRestUrl() + hubStatusURL;
    }

    /**
     * @return the hubcommandURL
     */
    public static String getHubCommandURL() throws Exception {
        return SettingsLoader.load().getRestUrl() + hubCommandURL;
    }

    /**
     * @return the hubcommandstatusURL
     */
    public static String getHubCommandStatusURL() throws Exception {
        return SettingsLoader.load().getRestUrl() + hubCommandStatusURL;
    }

    /**
     * @return the plainmeasurementbatchURL
     */
    public static String getPlainMeasurementBatchURL() throws Exception {
        return SettingsLoader.load().getRestUrl() + plainMeasurementBatchURL;
    }

}
