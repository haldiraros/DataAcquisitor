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
public class Chip {
    private final StringProperty nameProperty;
    
    public Chip() {
        this(null);
    }
    
    public Chip(String name) {
        nameProperty = new SimpleStringProperty(name);
    }

    public StringProperty getNameProperty() {
        return nameProperty;
    }
    
    public String getName() {
        return nameProperty.get();
    }
    
    public void setName(String name) {
        nameProperty.set(name);
    }
    
    @Override
    public String toString() {
        if (getName() == null)
            return "?";
        return getName();
    }
}
