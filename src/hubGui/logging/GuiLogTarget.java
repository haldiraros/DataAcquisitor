/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui.logging;

import hubGui.models.Message;
import java.time.LocalTime;
import javafx.collections.ObservableList;

/**
 *
 * @author Marcin
 */
public class GuiLogTarget implements ILogTarget {
    
    private ObservableList<Message> messages;

    public GuiLogTarget(ObservableList<Message> messages) {
        this.messages = messages;
    }
    
    @Override
    public void write(String message, LogTyps type) {
        messages.add(new Message(LocalTime.now(), message, type));
    }
    
}
