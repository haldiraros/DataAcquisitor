/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project.devices;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author hp
 */
public class HUB extends Device {

    private Set<CHIP> registredCHIPs;

    public HUB(String name) {
        super(name);
        registredCHIPs = new HashSet<CHIP>();
    }

    public void registerCHIP(CHIP chip) {
        registredCHIPs.add(chip);
    }

    public void unregisterCHIP(CHIP chip) {
        registredCHIPs.remove(chip);
    }

    public Set<CHIP> getRegistredCHIPs() {
        return registredCHIPs;
    }

}
