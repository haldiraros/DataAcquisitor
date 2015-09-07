/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project.devices;

/**
 *
 * @author hp
 */
public abstract class Device {

    protected String name;

    public Device(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
