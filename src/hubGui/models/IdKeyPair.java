/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Marcin
 */
public class IdKeyPair {
    private final StringProperty idProperty;
    private final StringProperty keyProperty;
    
    public IdKeyPair() {
        this(null, null);
    }
    
    public IdKeyPair(String id, String key) {
        idProperty = new SimpleStringProperty(id);
        keyProperty = new SimpleStringProperty(key);
    }
    
    public StringProperty getIdProperty() {
        return idProperty;
    }

    public String getId() {
        return idProperty.getValue();
    }

    public void setId(String id) {
        idProperty.setValue(id);
    }
    
    public StringProperty getKeyProperty() {
        return keyProperty;
    }

    public String getKey() {
        return keyProperty.getValue();
    }

    public void setKey(String key) {
        keyProperty.setValue(key);
    }
}
