/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui.views;

import hubGui.models.Chip;
import hubGui.models.Message;
import java.net.URL;
import java.time.LocalTime;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        timeColumn.setCellValueFactory(c -> c.getValue().getTimeProperty());
        messageColumn.setCellValueFactory(c -> c.getValue().getMessageProperty());
        
        ObservableList<Message> messages = FXCollections.observableArrayList();
        messageTable.setItems(messages);
        
        ObservableList<Chip> chips = FXCollections.observableArrayList();
        chipsList.setItems(chips);
    } 
    
     @FXML
    private void registerActionHandler(ActionEvent event) {
        ObservableList<Chip> items = chipsList.getItems();
        Chip chip = new Chip("Chip #" + items.size());
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
    
    private void addMessage(Chip chip, String message) {
        Message msg = new Message(LocalTime.now(), chip.toString() + ": " + message);
        messageTable.getItems().add(msg);
    }
    
    private void showNoChipsSelectedAlert() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText("No chips selected");
        alert.showAndWait();
    }
}
