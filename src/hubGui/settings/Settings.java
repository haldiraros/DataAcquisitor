/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui.settings;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Marcin
 */
@XmlRootElement( name = "settings")
public class Settings {
    
    private ArrayList<HubConfig> hubConfigs = new ArrayList<>();

    /**
     * @return the hubConfigs
     */
    public ArrayList<HubConfig> getHubConfigs() {
        return hubConfigs;
    }

    /**
     * @param hubConfigs the hubConfigs to set
     */
    public void setHubConfigs(ArrayList<HubConfig> hubConfigs) {
        this.hubConfigs = hubConfigs;
    }
}
