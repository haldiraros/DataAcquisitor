/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui.settings;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Marcin
 */
public class SettingsLoader {
    
    private static final String FILE_NAME = "settings.xml";
    
    public static Settings load() throws Exception {
        JAXBContext jc = JAXBContext.newInstance(Settings.class);
        Unmarshaller m = jc.createUnmarshaller();
        try (FileReader fstream = new FileReader(FILE_NAME)) {
            return (Settings)m.unmarshal(fstream);
        }
    }
    
    public static void save(Settings settings) throws Exception {
        JAXBContext jc = JAXBContext.newInstance(Settings.class);
        Marshaller m = jc.createMarshaller();
        try (FileWriter fstream = new FileWriter(FILE_NAME, false)) {
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
}
