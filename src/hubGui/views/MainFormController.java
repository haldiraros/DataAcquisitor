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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

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
        
        // test
        ObservableList<Message> messages = FXCollections.observableArrayList();
        messages.add(new Message(LocalTime.now(), "Hello world!"));
        messages.add(new Message(LocalTime.now(), "8 messages sent from chip #1"));
        messageTable.setItems(messages);
        
        ObservableList<Chip> chips = FXCollections.observableArrayList();
        chips.add(new Chip("chip #1"));
        chipsList.setItems(chips);
    }        
}
