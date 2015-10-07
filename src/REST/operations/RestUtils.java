/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package REST.operations;

import hubGui.i18n.Resources;
import hubGui.logging.LogTyps;
import hubGui.settings.Settings;
import hubGui.settings.SettingsLoader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import org.json.JSONException;
import org.json.JSONObject;
import project.Config;

/**
 *
 * @author hp
 */
public class RestUtils {

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
        return everything.toString();
    }

    public static JSONObject sendToServer(JSONObject message, String service) throws IOException, JSONException, Exception {
        URL url = new URL(service);
        URLConnection connection = connectionSetup(url);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
        out.write(message.toString());
        out.close();
        HttpURLConnection httpConnection = (HttpURLConnection) connection;

        int code = httpConnection.getResponseCode();
        if (code != 200) {
            throw new Exception(Resources.getFormatString("REST.operations.invalidResponseCode", code));
        }
        InputStreamReader isr = new InputStreamReader(connection.getInputStream());
        BufferedReader in = new BufferedReader(isr);
        JSONObject response = new JSONObject(readBigStringIn(in));
        in.close();
        return response;
    }

    /**
     * @return the measurementsURL
     */
    public static String getMeasurementsURL() throws Exception {
        return SettingsLoader.load().getRestUrl() + Config.getString("REST.measurementsURL");
    }

    /**
     * @return the hubstatusURL
     */
    public static String getHubStatusURL() throws Exception {
        return SettingsLoader.load().getRestUrl() + Config.getString("REST.hubStatusURL");
    }

    /**
     * @return the hubcommandURL
     */
    public static String getHubCommandURL() throws Exception {
        return SettingsLoader.load().getRestUrl() + Config.getString("REST.hubCommandURL");
    }

    /**
     * @return the hubcommandstatusURL
     */
    public static String getHubCommandStatusURL() throws Exception {
        return SettingsLoader.load().getRestUrl() + Config.getString("REST.hubCommandStatusURL");
    }

    /**
     * @return the plainmeasurementbatchURL
     */
    public static String getPlainMeasurementBatchURL() throws Exception {
        return SettingsLoader.load().getRestUrl() + Config.getString("REST.plainMeasurementBatchURL");
    }

    public static URLConnection connectionSetup(URL url) throws IOException, Exception {
        Settings sett = SettingsLoader.load();
        URLConnection connection;

        if (sett.isUseProxy() == null ? false : sett.isUseProxy()) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(sett.getProxyHost(), Integer.parseInt(sett.getProxyPort())));
            connection = url.openConnection(proxy);
        } else {
            connection = url.openConnection();
        }
        return connection;

    }
}
