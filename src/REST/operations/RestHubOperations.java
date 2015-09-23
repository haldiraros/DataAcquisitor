/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package REST.operations;

import hubOperations.HubControl;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author hp
 */
public class RestHubOperations {
    
    public JSONObject sendHubStatus(HubControl hc) throws JSONException, IOException, Exception {
        // Step1: Prepare JSON data
        JSONObject status = RestUtils.getHubLogInfo(hc.getHubId());
        status.put("firmwareVer", "1.0");
        status.put("hardwareVer", "1.0");
        status.put("dateTime", "FFFF");
        JSONObject message = new JSONObject();
        message.put("status", status);
        System.out.println("\nJSON Object: " + message);
        // Step2: Now pass JSON File Data to REST Service
        return RestUtils.sendToServer(message, RestUtils.getHubstatusURL());
    }

    public JSONObject getHubCommand(HubControl hc) throws JSONException, IOException, Exception {
        // Step1: Prepare JSON data
        JSONObject message = RestUtils.getHubLogInfo(hc.getHubId());
        System.out.println("\nJSON Object: " + message);
        // Step2: Now pass JSON File Data to REST Service
        return RestUtils.sendToServer(message, RestUtils.getHubcommandURL());
    }

    public JSONObject sendHubCommandStatus(HubControl hc, Long commandId, String commandStatus) throws JSONException, IOException, Exception {
        // Step1: Prepare JSON data
        JSONObject message = RestUtils.getHubLogInfo(hc.getHubId());
        JSONObject command = new JSONObject();
        command.put("id", commandId);
        command.put("status", commandStatus);
        message.put("command", command);
        System.out.println("\nJSON Object: " + message);
        // Step2: Now pass JSON File Data to REST Service
        return RestUtils.sendToServer(message, RestUtils.getHubcommandstatusURL());
    }

}
