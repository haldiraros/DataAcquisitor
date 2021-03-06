/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui.settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import project.Config;

/**
 *
 * @author Marcin
 */
public class SettingsLoader {
        
    public static Settings load() throws Exception {
        JAXBContext jc = JAXBContext.newInstance(Settings.class);
        Unmarshaller m = jc.createUnmarshaller();
        try (FileReader fstream = new FileReader(Config.getPath("hubGui.settings.fileName"))) {
            return (Settings)m.unmarshal(fstream);
        }
    }
    
    public static void save(Settings settings) throws Exception {
        JAXBContext jc = JAXBContext.newInstance(Settings.class);
        Marshaller m = jc.createMarshaller();
        File file = new File(Config.getPath("hubGui.settings.fileName"));
        file.getParentFile().mkdirs();
        try (FileWriter fstream = new FileWriter(file, false)) {
            m.marshal(settings, fstream);
        }
    }
    
    public static Settings loadOrCreateEmpty() throws Exception {
        Settings settings;
        try {
            settings = load();
        }
        catch(FileNotFoundException ex) {
            settings = new Settings();
            save(settings);
        }
        return settings;
    }

    public static String getHubAuthKey(String hubHexId) throws Exception {
        Settings s = loadOrCreateEmpty();
        for(HubConfig hc : s.getHubConfigs()){
            if(hc.getId() != null && hubHexId != null){
                if (hc.getId().equals(hubHexId)) {
                    return hc.getKey();
                }
            }
        }
        return null;
    }
}
