/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui.views;

import REST.RestMenager; 
import hubGui.i18n.Resources;
import hubGui.logging.GuiLogTarget;
import hubGui.logging.LogTyps;
import hubGui.logging.Logger;
import hubGui.models.Chip;
import hubGui.models.Message;
import hubGui.settings.SettingsLoader;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
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
import javafx.collections.ListChangeListener;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
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
    }
    
    public void init() { 
        timeColumn.setCellValueFactory(c -> c.getValue().getTimeProperty());
        messageColumn.setCellValueFactory(c -> c.getValue().getMessageProperty());
        
        messageColumn.setCellFactory(new Callback<TableColumn<Message, String>, TableCell<Message, String>>() {

            @Override
            public TableCell<Message, String> call(TableColumn<Message, String> column) {
                return new TableCell<Message, String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!isEmpty()) {
                            int currentIndex = indexProperty().getValue() < 0
                                    ? 0 
                                    : indexProperty().getValue();
                            Message msg = column.getTableView().getItems().get(currentIndex);
                            switch (msg.getType()) {
                                case ERROR:
                                    this.setTextFill(Color.RED);
                                    break;
                                case SUCCESS:
                                    this.setTextFill(Color.DARKGREEN);
                                    break;
                                case WARNING:
                                    this.setTextFill(Color.ORANGE);
                                    break;
                                case LOG:
                                    this.setTextFill(Color.BLACK);
                                    break;
                            }                            
                            setText(item);
                        }
                    }
                };
            }
        });

        ObservableList<Message> messages = FXCollections.observableArrayList();
        messageTable.setItems(messages);
        
        messageTable.getItems().addListener((ListChangeListener<Message>) (c -> {
            c.next();
            final int size = messageTable.getItems().size();
            if (size > 0) {
                messageTable.scrollTo(size - 1);
            }
        }));
        
        Logger.addTarget(new GuiLogTarget(messages));
        Session ses = null;
        try {
            ses = setupDBSession();
        } catch (Exception ex) {
            Dialogs.showErrorAlert(
                    Resources.getFormatString(
                            "msg.main.errorOnDBSetup",ex.getLocalizedMessage()));
            this.closeActionHandler(null);
        }
        try{    
            System.loadLibrary("rxtxSerial");
        }catch(UnsatisfiedLinkError e){
            Logger.write(Resources.getString("msg.main.errorOnRXTXLibLoad"), LogTyps.ERROR);
            Dialogs.showErrorAlert(
                    Resources.getString(
                            "msg.main.errorOnRXTXLibLoadAlert"));
            this.closeActionHandler(null);
        }
        
        if (!hubControlInit(ses))
            return;
        
        initializeLoggerList();
        
        try {
            String hID = HubHandler.getInstance().getHubControl().getHubId();
            if(SettingsLoader.getHubAuthKey(hID) == null){
                Dialogs.showInfoAlert(
                        Resources.getFormatString(
                                "msg.main.noAuthKeyForHubOnInitAlert",hID,
                                Resources.getString("msg.main.AuthKeyInstructions")));
                hubGui.logging.Logger.write(Resources.getFormatString("msg.main.noAuthKeyForHubOnInit", hID), LogTyps.WARNING);
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(MainFormController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean hubControlInit(Session dbSession) { //TODO: make sure there is a auth key for the HUB
        HubHandler hubH = null;
        try {
            hubH = HubHandler.getInstance();
            hubH.setDBSession(dbSession);
        } catch (Exception ex) {
            Logger.write(Resources.getString("msg.main.errorOnHubAutofinding"), LogTyps.ERROR);
            Dialogs.showErrorAlert(
                    Resources.getFormatString(
                            "msg.main.errorOnHubAutofindingAltert",
                            Resources.getString("msg.main.programPrerequisites")));
            
            this.closeActionHandler(null);
            return false;
        }
        try {
            hubH.getHubControl().openHubConn();
            hubH.getHubControl().closeAllSessions();
            dbSession.setRestMenager(new RestMenager());
        } catch (Exception ex) {
            Logger.write(Resources.getString("msg.main.errorOnHubConnection"), LogTyps.ERROR);
            Dialogs.showErrorAlert(
                    Resources.getFormatString(
                            "msg.main.errorOnHubConnectionAlert",
                            Resources.getString("msg.main.programPrerequisites")));
            ex.printStackTrace();
            this.closeActionHandler(null);
            return false;
        }
        addMessage(Resources.getFormatString("msg.main.hubConnected", hubH.getHubControl().getHubId()));
        return true;
    }

    private void initializeLoggerList() {
        try {
            ObservableList<Chip> loggers = FXCollections.observableArrayList();

            long[] listLoggers = HubHandler.getInstance().getHubControl().getRegisteredLoggersList();
            if (listLoggers != null) {
                addMessage(Resources.getFormatString("msg.main.foundXLoggers", listLoggers.length));
                for (int i = 0; i < listLoggers.length; i++) {
                    Chip logger = new Chip(Long.toHexString(listLoggers[i]));
                    loggers.add(logger);
                    addMessage(logger, Resources.getString("msg.main.loggerFoundOnHub"));
                }
            }
            chipsList.setItems(loggers);
        } catch (MeteringSessionException ex) {
            Logger.write(Resources.getString("msg.main.errorGettingLoggerList"), LogTyps.ERROR);
        }
    }

    @FXML
    private void settingsActionHandler(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SettingsForm.fxml"));
            fxmlLoader.setResources(Resources.getResourceBundle());
            Parent root = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setTitle(Resources.getString("msg.main.settingsTitle"));
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void registerActionHandler(ActionEvent event) {
        boolean flag = true;
        long idLogger = -1;
        Optional<String> chipId;
        do {
            flag = true;
            chipId = Dialogs.inputString(
                    Resources.getString("msg.main.registrationTitle"),
                    Resources.getString("msg.main.registrationDetails"),
                    Resources.getString("msg.main.loggerId"));
            if (chipId.isPresent()) {
                if (chipId.get().length() != 8) {
                    flag = false;
                }
                try {
                    idLogger = Long.parseLong(chipId.get(), 16);
                } catch (NumberFormatException ex) {
                    flag = false;
                }
            }

            if (!flag) {
                Dialogs.showErrorAlert(Resources.getString("msg.main.invalidLoggerId"));
            }
        } while (!flag);

        if (chipId.isPresent()) {
            HubControl hubC;

            try {
                hubC = HubHandler.getInstance().getHubControl();
                idLogger = hubC.registerNewLogger(idLogger);
            } catch (MeteringSessionException ex) {
                Logger.write(Resources.getString("msg.main.errorRegisteringNewLogger"), LogTyps.ERROR);
                Dialogs.showErrorAlert(Resources.getString("msg.main.errorRegisteringNewLogger"));
                return;
            } catch (NumberFormatException ex) {
                Logger.write(Resources.getString("msg.main.errorRegisteringNewLogger"), LogTyps.ERROR);
                Dialogs.showErrorAlert(Resources.getString("msg.main.invalidLoggerId"));
                return;
            }
            if (idLogger != -1) {
                ObservableList<Chip> items = chipsList.getItems();
                Chip chip = new Chip(Long.toHexString(idLogger));
                addMessage(chip, Resources.getString("msg.main.registered"));
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
            Logger.write(Resources.getString("msg.main.errorOnRegisteringLogger"), LogTyps.ERROR);
            Dialogs.showErrorAlert(
                        Resources.getFormatString(
                                "msg.main.errorOnRegisteringLoggerAlert",
                                Resources.getString("msg.main.loggerPrerequisites")));
            try {
                hubC = HubHandler.getInstance().getHubControl();
                hubC.restartAll();
            } catch (MeteringSessionException ex1) {
                ;
            }
            return;
        }
        if (idLog != -1) {
            ObservableList<Chip> items = chipsList.getItems();
            Chip chip = new Chip(Long.toHexString(idLog));
            addMessage(chip, Resources.getString("msg.main.registered"));
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
            hubC.unregisterLogger(Long.parseLong(chip.getName(), 16));
            items.remove(index);
            addMessage(chip, Resources.getString("msg.main.unregistered"));
        } catch (MeteringSessionException ex) {
            java.util.logging.Logger.getLogger(MainFormController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void readActionHandler(ActionEvent event) {
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws MeteringSessionException {
                HubControl hubC = HubHandler.getInstance().getHubControl();
                hubC.readPacketsLoggerFlash();
                return null;
            }
        };

        ProgressForm test = new ProgressForm(Resources.getString("msg.main.readingFlashProgress"));
        task.setOnRunning((e) -> test.getDialogStage().show());
        task.setOnSucceeded((e) -> {
            test.getDialogStage().hide();
            Logger.write(Resources.getString("msg.main.dataReceived"),LogTyps.SUCCESS);
        });
        task.setOnFailed((e) -> {
            test.getDialogStage().hide();
            try {
                task.get();
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(MainFormController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                MeteringSessionException msEx = (MeteringSessionException) ex.getCause();
                msEx.printStackTrace();
                Dialogs.showErrorAlert(
                        Resources.getFormatString(
                                "msg.main.errorOnReadingLoggerFlashAlert",
                                Resources.getString("msg.main.loggerPrerequisites")));
                try {
                    HubControl hubC = HubHandler.getInstance().getHubControl();
                    hubC.restartAll();
                } catch (MeteringSessionException ex1) {
                    java.util.logging.Logger.getLogger(MainFormController.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            Logger.write(Resources.getString("msg.main.datareceiveFailed"), LogTyps.ERROR);
        });
        new Thread(task).start();
    }

    @FXML
    private void closeActionHandler(ActionEvent event) {
        Stage stage = (Stage) messageTable.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void aboutActionHandler(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(Resources.getString("gui.main.about"));
        alert.setHeaderText(Resources.getString("common.program"));
        alert.setContentText(Resources.getString("msg.main.copyrights"));
        alert.showAndWait();
    }
    
    @FXML
    private void infoLoggerActionHandler(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("LoggerInfoForm.fxml"));
            fxmlLoader.setResources(Resources.getResourceBundle());
            Stage stage = new Stage();
            Parent root = (Parent) fxmlLoader.load();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setTitle(Resources.getString("msg.main.loggerInfoTitle"));
            stage.setScene(new Scene(root));
            stage.show();
            LoggerInfoFormController c = (LoggerInfoFormController)fxmlLoader.getController();
            c.readInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void readFromHubHandler(ActionEvent event) {

        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws MeteringSessionException {
                HubControl hubC = HubHandler.getInstance().getHubControl();
                hubC.readPacketsHubFlash();
                return null;
            }
        };

        ProgressForm test = new ProgressForm(Resources.getString("msg.main.readingHubFlashProgress"));
        task.setOnRunning((e) -> test.getDialogStage().show());
        task.setOnSucceeded((e) -> {
            test.getDialogStage().hide();
            addMessage(Resources.getString("msg.main.hubDataReceived"));
        });
        task.setOnFailed((e) -> {
            test.getDialogStage().hide();
            try {
                task.get();
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(MainFormController.class.getName()).log(Level.SEVERE, null, ex);

                MeteringSessionException msEx = (MeteringSessionException) ex.getCause();
                msEx.printStackTrace();
                Dialogs.showErrorAlert(Resources.getString("msg.main.errorOnReceiveingHubData"));
                try {
                    HubControl hubC = HubHandler.getInstance().getHubControl();
                    //hubC.restartAll();
                } catch (MeteringSessionException ex1) {
                    java.util.logging.Logger.getLogger(MainFormController.class.getName()).log(Level.SEVERE, null, ex1);
                }
            } catch (ExecutionException ex) {
                java.util.logging.Logger.getLogger(MainFormController.class.getName()).log(Level.SEVERE, null, ex);
            }
            addMessage(Resources.getString("msg.main.hubDataReceiveFailed"));
        });
        new Thread(task).start();

    }

    @FXML
    private void radioSessionHandler(ActionEvent event) {
        boolean ret = toggleRadioSession();
        if (ret) {
            addMessage(
                    Resources.getFormatString(
                    "msg.main.radioSessionStatus",
                    radioSessionToggle.isSelected() ? Resources.getString("common.on") : Resources.getString("common.off")));
        }
        if (!ret) {
            Logger.write(Resources.getString("msg.main.errorOnChangingRadioSession"), LogTyps.ERROR);
        }
    }

    private void addMessage(Chip chip, String message) {
        addMessage(chip.toString() + ": " + message);
    }

    private boolean toggleRadioSession() {

        HubControl hubC;
        boolean isToggled = false;
        try {
            hubC = HubHandler.getInstance().getHubControl();

            if (!isRadioSessionActive) {
                hubC.startRecievingInRadioSession();
                isRadioSessionActive = true;
            } else {
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
        Dialogs.showInfoAlert(Resources.getString("msg.main.noLoggersSelected"));
    }

    private Session setupDBSession() throws SQLException, ClassNotFoundException, Exception {
        LocalDataBaseMenager ldbm = new LocalDataBaseMenager();
        if (ldbm.fullTestBDExists() == false) {
            Logger.write(Resources.getString("msg.main.dbNotFound"), LogTyps.WARNING);
            Logger.write(Resources.getString("msg.main.tryingToCreateDb"), LogTyps.SUCCESS);
            try {
                ldbm.setupDataBase();
                Logger.write(Resources.getString("msg.main.createdDb"), LogTyps.SUCCESS);
            } catch (Exception e) {
                Logger.write(Resources.getFormatString("msg.main.errorOnCreatingDb", e.getMessage()), LogTyps.ERROR);
                e.printStackTrace();
            }
            if (ldbm.fullTestBDExists() == false) {
                Logger.write(Resources.getString("msg.main.invalidDb"), LogTyps.ERROR);
                throw new SQLException("Can not set up database.");
                //return null;
            }
        }
        Session localDBSession = new Session(ldbm, true);
        return localDBSession;
    }
}
