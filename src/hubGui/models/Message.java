/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui.models;

import java.time.LocalTime;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Marcin
 */
public class Message {
    private final ObjectProperty<LocalTime> timeProperty;
    private final StringProperty messageProperty;
    
    public Message() {
        this(null, null);
    }
    
    public Message(LocalTime date, String message)
    {
        timeProperty = new SimpleObjectProperty(date);
        messageProperty = new SimpleStringProperty(message);
    }

    public ObjectProperty<LocalTime> getTimeProperty() {
        return timeProperty;
    }
    
    public LocalTime getTime() {
        return timeProperty.get();
    }

    public void setTime(LocalTime time) {
        timeProperty.set(time);
    }
    
    public StringProperty getMessageProperty() {
        return messageProperty;
    }
    
    public String getMessage() {
        return messageProperty.get();
    }
    
    public void setMessage(String message) {
        messageProperty.set(message);
    }
}
