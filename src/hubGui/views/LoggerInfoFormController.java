/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui.views;

import hubGui.i18n.Resources;
import hubGui.logging.LogTyps;
import hubGui.logging.Logger;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import hubOperations.HubControl;
import hubOperations.HubHandler;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Marcin
 */
public class LoggerInfoFormController implements Initializable {

    @FXML
    private TextField idText;
    
    @FXML
    private TextField deviceVersionText;
    
    @FXML
    private TextField firmwareVersionText;
    
    @FXML
    private TextField aesText;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }
    
    public void readInfo() {
        try {
            HubHandler hubH = HubHandler.getInstance();
            HubControl hubC = hubH.getHubControl();
            idText.setText(Long.toHexString(hubC.getHubConn().getLoggerId()));
            firmwareVersionText.setText(hubC.getHubConn().getLoggerFirmawareVersion());
            deviceVersionText.setText(hubC.getHubConn().getLoggerHardwareVersion());
            aesText.setText(hubC.getHubConn().getLoggerAesKey());
        } catch (MeteringSessionException ex) {
            Logger.write(Resources.getString("gui.logger.errorOnInfoRead"), LogTyps.ERROR);
            Dialogs.showErrorAlert(Resources.getFormatString("gui.logger.errorOnInfoReadAlert", ex.getMessage()));
            ((Stage)idText.getScene().getWindow()).close();
        }
    }
}
