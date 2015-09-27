/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package REST.operations;

import hubGui.i18n.Resources;
import hubGui.logging.LogTyps;
import hubOperations.HubControl;
import hubOperations.HubHandler;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author hp
 */
public class RestHubOperations {

    public void sendHubStatus() {
        try {
            HubControl hc = HubHandler.getInstance().getHubControl();
            // Step1: Prepare JSON data
            JSONObject message = RestUtils.getHubLogInfo(hc.getHubId());
            message.put("firmwareVer", hc.getHubFirmVer());
            message.put("hardwareVer", hc.getHubHardVer());
            message.put("dateTime", String.format("%0#8X", (long) (hc.getHubConn().getHubTime().getTime() / 1000)));
            message.put("ntpTime", "N");
            System.out.println("\nJSON Object: " + message);
            // Step2: Now pass JSON File Data to REST Service
            JSONObject response;
            try {
                response = RestUtils.sendToServer(message, RestUtils.getHubStatusURL());
                int status = response.getInt("errCode");
                if (RestUtils.noErrorResponse != status) {
                    hubGui.logging.Logger.write(Resources.getFormatString("msg.sendHubStatus.nonValidResponseError", status), LogTyps.ERROR);
                }
//            } catch (IOException ex) {
//                hubGui.logging.Logger.write(Resources.getFormatString("", ex.getMessage()), LogTyps.ERROR);
//            } catch (JSONException ex) {
//                hubGui.logging.Logger.write(Resources.getFormatString("", ex.getMessage()), LogTyps.ERROR);
            } catch (Exception ex) {
                hubGui.logging.Logger.write(Resources.getFormatString("msg.sendHubStatus.sendMessageError", ex.getMessage()), LogTyps.ERROR);
            }
//        } catch (MeteringSessionException ex) {
//            hubGui.logging.Logger.write(Resources.getFormatString("", status), LogTyps.ERROR);
//        } catch (JSONException ex) {
//            hubGui.logging.Logger.write(Resources.getFormatString("", status), LogTyps.ERROR);
        } catch (Exception ex) {
            hubGui.logging.Logger.write(Resources.getFormatString("msg.sendHubStatus.prepareMessageError", ex.getMessage()), LogTyps.ERROR);
        }
    }

    public JSONObject getHubCommand(HubControl hc) throws JSONException, IOException, Exception {
        // Step1: Prepare JSON data
        JSONObject message = RestUtils.getHubLogInfo(hc.getHubId());
        System.out.println("\nJSON Object: " + message);
        // Step2: Now pass JSON File Data to REST Service
        return RestUtils.sendToServer(message, RestUtils.getHubCommandURL());
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
        return RestUtils.sendToServer(message, RestUtils.getHubCommandStatusURL());
    }

}
