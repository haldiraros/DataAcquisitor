/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui.views;

import hubGui.Main;
import hubGui.logging.GuiLogTarget;
import hubGui.logging.LogTyps;
import hubGui.logging.Logger;
import hubGui.models.Chip;
import hubGui.models.Message;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionOperationAlreadyInProgressException;
import hubOperations.HubControl;
import hubOperations.HubHandler;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import localDB.menagers.LocalDataBaseMenager;
import project.data.Session;

/**
 * FXML Controller class
 *
 * @author Marcin
 */
public class MainFormController implements Initializable {
    
    @FXML
    private TableView<Message> messageTable;
    
    @FXML
    private TableColumn<Message, LocalTime> timeColumn;
    
    @FXML
    private TableColumn<Message, String> messageColumn;
    
    @FXML
    private ListView<Chip> chipsList;
    
    @FXML
    private ToggleButton radioSessionToggle;
    
    @FXML
    private CheckMenuItem radioSessionMenuItem;
    
    private boolean isRadioSessionActive;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        timeColumn.setCellValueFactory(c -> c.getValue().getTimeProperty());
        messageColumn.setCellValueFactory(c -> c.getValue().getMessageProperty());
        
        ObservableList<Message> messages = FXCollections.observableArrayList();
        messageTable.setItems(messages);
        Logger.addTarget(new GuiLogTarget(messages));
        Session ses =null;
        try {
             ses = setupDBSession();
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(MainFormController.class.getName()).log(Level.SEVERE, null, ex);
        }
        hubControlInit(ses);
        initializeLoggerList();
        
        
        
    } 
    
    private void hubControlInit(Session dbSession){
        HubHandler hubH = null;
        try{
            hubH = HubHandler.getInstance();
            hubH.setDBSession(dbSession);
        }catch(Exception ex){
            Logger.write("Error on hub autofinding.", LogTyps.ERROR);
            Dialogs.showErrorAlert("Error on hub autofinding, make sure that "+
                    "Hub device is connected, drivers are installed and "+
                    "the application has appropriate rights.\n"+
                    "Close all other instances of appliactions with access to the Hub device \n"+
                    "Restart the application.");
            this.closeActionHandler(null);
        }
        try{
            hubH.getHubControl().openHubConn();
            hubH.getHubControl().closeAllSesssions();           
        }catch (Exception ex) {
             Logger.write("Error on creating hub connection", LogTyps.ERROR);
             Dialogs.showErrorAlert("Error connecting to the hub device, make sure that "+
                    "Hub device is connected, drivers are installed and "+
                    "the application has appropriate rights \n"+
                    "Restart the application.");
            return;
        }
        addMessage("HUB: "+hubH.getHubControl().getHubId()+" connected");
    }
    
    private void initializeLoggerList(){
        try {
            ObservableList<Chip> loggers = FXCollections.observableArrayList();
            
            long[] listLoggers= HubHandler.getInstance().getHubControl().getRegisteredLoggersList();
            if(listLoggers!=null){
                addMessage("Found "+listLoggers.length+" registered loggers");
                for (int i=0; i<listLoggers.length; i++)
                { 
                    Chip logger = new Chip(Long.toHexString(listLoggers[i]));
                    loggers.add(logger);
                    addMessage(logger, "Was found registered on Hub device.");
                }
            }
            chipsList.setItems(loggers);
        } catch (MeteringSessionException ex) {
            Logger.write("Error getting logger list", LogTyps.ERROR);
        }
    }
        
    @FXML
    private void settingsActionHandler(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SettingsForm.fxml"));
            Parent root = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setTitle("Settings");
            stage.setScene(new Scene(root));  
            stage.show();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    

    @FXML
    private void registerActionHandler(ActionEvent event) {
        boolean flag=true;
        long idLogger = -1;
        Optional<String> chipId;
        do{
            flag=true;
             chipId = Dialogs.inputString("Registration", "Logger registration", "Logger ID");
            if(chipId.isPresent()){
                if(chipId.get().length()!=8) flag=false;
                try{
                    idLogger = Long.parseLong(chipId.get(), 16);
                }catch(NumberFormatException ex){
                   flag=false;
                }
            }
            
            if(!flag) Dialogs.showErrorAlert("Error registering new logger. Wrong ID format.\n"+
                       "The given ID should be 8 characters long and given in HEX notation");
        }
        while(!flag);
        
        if (chipId.isPresent()) {
            HubControl hubC; 
            
            try {
                hubC = HubHandler.getInstance().getHubControl();
                idLogger = hubC.registerNewLogger(idLogger);
            } catch (MeteringSessionException ex) {
                Logger.write("Error registering new logger", LogTyps.ERROR);
                Dialogs.showErrorAlert("Error registering new logger");
            return;
            }catch(NumberFormatException ex){
                Logger.write("Error registering new logger. Wrong ID format.", LogTyps.ERROR);
                Dialogs.showErrorAlert("Error registering new logger. Wrong ID format.\n"+
                       "The given ID should be a String with logger ID given in HEX notation");
                return;
            }
            if(idLogger !=-1){
                ObservableList<Chip> items = chipsList.getItems();
                Chip chip = new Chip(Long.toHexString(idLogger));
                addMessage(chip, "Registered.");
                items.add(chip);
            }
            
        }
    }
    
    @FXML
    private void registerAutoActionHandler(ActionEvent event) {
        //TODO: Do actual registering logger
        long idLog = -1;
        HubControl hubC;
        try {
            hubC = HubHandler.getInstance().getHubControl();
            idLog = hubC.autoRegisterLogger();
        } catch (MeteringSessionException ex) {
            Logger.write("Error auto registering new logger.", LogTyps.ERROR);
                Dialogs.showErrorAlert("Error registering new logger.\n"+
                       "Make sure that the logger is placed correctly on the Hub device.\n"+
                       "Logger autoregistration is known to fail due to unsure IR connection.\n"+
                       "Loggers tend to get stuck for prolonged periods");
                return;
        }
        if(idLog !=-1){
            ObservableList<Chip> items = chipsList.getItems();
            Chip chip = new Chip(Long.toHexString(idLog));
            addMessage(chip, "Registered.");
            items.add(chip);
        }
    }
    
    @FXML
    private void unregisterActionHandler(ActionEvent event) {
        int index = chipsList.getSelectionModel().getSelectedIndex();
        if (index == -1) {
            showNoChipsSelectedAlert();
            return;
        }
        
        HubControl hubC; 
        try {
            hubC = HubHandler.getInstance().getHubControl();
            ObservableList<Chip> items = chipsList.getItems();
            Chip chip = items.get(index);
            hubC.unregisterLogger(Long.parseLong(chip.getName(),16));
            items.remove(index);
            addMessage(chip, "Unregistered.");
        } catch (MeteringSessionException ex) {
            java.util.logging.Logger.getLogger(MainFormController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void readActionHandler(ActionEvent event) {
        Task<Void> task = new Task<Void>() {
            @Override public Void call() throws MeteringSessionException {
                HubControl hubC = HubHandler.getInstance().getHubControl();
                hubC.readPacketsLoggerFlash();
                return null;
            }
        };
        
        ProgressForm test = new ProgressForm("Reading from Logger flash memory with IR connection");
        task.setOnRunning((e) -> test.getDialogStage().show());
        task.setOnSucceeded((e) -> {
            test.getDialogStage().hide();
            addMessage("Data from Logger device read.");
        });
        task.setOnFailed((e) -> {
            test.getDialogStage().hide();
            try {
                task.get();
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(MainFormController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                System.out.println("bla bla bla");
                MeteringSessionException msEx =(MeteringSessionException) ex.getCause();
                msEx.printStackTrace();
            }
            addMessage("Operation failed");  
        });
        new Thread(task).start();
    }
    
    @FXML
    private void closeActionHandler(ActionEvent event){
        Stage stage = (Stage) messageTable.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void aboutActionHandler(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("MCR Data Acquirer");
        alert.setContentText("Poznań University of Technology ©2015");
        alert.showAndWait();
    }
    
    @FXML
    private void readFromHubHandler(ActionEvent event) {
        
        Task<Void> task = new Task<Void>() {
            @Override public Void call() throws MeteringSessionException {
                HubControl hubC = HubHandler.getInstance().getHubControl();
                hubC.readPacketsHubFlash();
                return null;
            }
        };
        
        ProgressForm test = new ProgressForm("Reading from Hub device flash memory");
        task.setOnRunning((e) -> test.getDialogStage().show());
        task.setOnSucceeded((e) -> {
            test.getDialogStage().hide();
            addMessage("Data from Hub device read.");
        });
        task.setOnFailed((e) -> {
            test.getDialogStage().hide();
            try {
                task.get();
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(MainFormController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                System.out.println("bla bla bla");
                MeteringSessionException msEx =(MeteringSessionException) ex.getCause();
                msEx.printStackTrace();
            }
            addMessage("Operation failed");   
        });
        new Thread(task).start();
        
        
    }
    
    @FXML
    private void radioSessionHandler(ActionEvent event) {
        boolean ret = toggleRadioSession();
        if(ret) addMessage("Radio session recieving is now: " + (radioSessionToggle.isSelected() ? "on." : "off."));
        if(!ret) Logger.write("Changing the state of radio session failed", LogTyps.ERROR);
    }
    
    private void addMessage(Chip chip, String message) {
        addMessage(chip.toString() + ": " + message);
    }
    
    private boolean toggleRadioSession() {
        
        HubControl hubC;
        boolean isToggled = false;
        try {
            hubC = HubHandler.getInstance().getHubControl();
       
            if(!isRadioSessionActive){
                hubC.startRecievingInRadioSession();
                isRadioSessionActive = true;
            }else{
               hubC.stopRecievingInRadioSession();
               isRadioSessionActive = false;
            }
            isToggled = true;
         } catch (MeteringSessionException ex) {
        }

        radioSessionMenuItem.setSelected(isRadioSessionActive);
        radioSessionToggle.setSelected(isRadioSessionActive);
        return isToggled;
    }
    
    private void addMessage(String message) {
        Logger.write(message, LogTyps.LOG);
    }
    
    private void showNoChipsSelectedAlert() {
        Dialogs.showInfoAlert("No loggers selected");
    }

    private Session setupDBSession() throws SQLException, ClassNotFoundException, Exception {
        LocalDataBaseMenager ldbm = new LocalDataBaseMenager();
        if (ldbm.fullTestBDExists() == false) {
            Logger.write("Local DB Not Found!", LogTyps.WARNING);
            Logger.write("Trying to create new Local DB.", LogTyps.SUCCESS);
            try {
                ldbm.setupDataBase();
                Logger.write("New Local DB created.", LogTyps.SUCCESS);
            } catch (Exception e) {
                Logger.write("Error while creating Local DB:" + e.getMessage(), LogTyps.ERROR);
                e.printStackTrace();
            }
            if (ldbm.fullTestBDExists() == false) {
                Logger.write("Error: Local BD is not valid!", LogTyps.ERROR);
                return null;
            }
        }

        Session localDBSession = new Session(ldbm, true);
        return localDBSession;

    }
}
