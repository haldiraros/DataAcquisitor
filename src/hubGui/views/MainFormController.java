/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui.views;

import hubGui.logging.GuiLogTarget;
import hubGui.logging.LogTyps;
import hubGui.logging.Logger;
import hubGui.models.Chip;
import hubGui.models.Message;
import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
        //TODO: Find hub (here so that I can do a dialog if something goes wrong
        //TODO: Start connection
        
        ObservableList<Chip> chips = FXCollections.observableArrayList();
        //TODO: Add scan for chips
        chipsList.setItems(chips);
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
        Optional<String> chipId = Dialogs.inputString("Registration", "Logger registration", "Logger ID");
        
        if (chipId.isPresent()) {
            //TODO: Do actual registering logger
            ObservableList<Chip> items = chipsList.getItems();
            Chip chip = new Chip(chipId.get());
            addMessage(chip, "Registered.");
            items.add(chip);
        }
    }
    
    @FXML
    private void registerAutoActionHandler(ActionEvent event) {
        //TODO: Do actual registering logger
        ObservableList<Chip> items = chipsList.getItems();
        Chip chip = new Chip(Integer.toString(items.size() + 1));
        addMessage(chip, "Registered.");
        items.add(chip);
    }
    
    @FXML
    private void unregisterActionHandler(ActionEvent event) {
        int index = chipsList.getSelectionModel().getSelectedIndex();
        if (index == -1) {
            showNoChipsSelectedAlert();
            return;
        }
        //TODO: Unregister logger!
        ObservableList<Chip> items = chipsList.getItems();
        Chip chip = items.get(index);
        addMessage(chip, "Unregistered.");
        items.remove(index);
    }

    @FXML
    private void readActionHandler(ActionEvent event) {
        int index = chipsList.getSelectionModel().getSelectedIndex();
        if (index == -1) {
            showNoChipsSelectedAlert();
            return;
        }
        Chip chip = chipsList.getItems().get(index);
        //TODO: Logger Flash
        addMessage(chip, "Received data.");
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
        //TODO: Read from hub
        addMessage("Received data from Hub.");
    }
    
    @FXML
    private void radioSessionHandler(ActionEvent event) {
        toggleRadioSession();
        addMessage("Radio session is " + (radioSessionToggle.isSelected() ? "on." : "off."));
    }
    
    private void addMessage(Chip chip, String message) {
        addMessage(chip.toString() + ": " + message);
    }
    
    private void toggleRadioSession() {
        //TODO: Add starting and closing of the radio sessions
        isRadioSessionActive = !isRadioSessionActive;
        radioSessionMenuItem.setSelected(isRadioSessionActive);
        radioSessionToggle.setSelected(isRadioSessionActive);
    }
    
    private void addMessage(String message) {
        Logger.write(message, LogTyps.LOG);
    }
    
    private void showNoChipsSelectedAlert() {
        Dialogs.showInfoAlert("No loggers selected");
    }
}
