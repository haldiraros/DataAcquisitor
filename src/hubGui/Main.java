/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui;

import com.sun.javafx.runtime.VersionInfo;
import hubGui.i18n.Resources;
import hubGui.logging.ConsoleLogTarget;
import hubGui.logging.Logger;
import hubGui.settings.Settings;
import hubGui.settings.SettingsLoader;
import hubOperations.HubHandler;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Marcin
 */
public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("JavaFX runtime version: " + VersionInfo.getRuntimeVersion());
            
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(Resources.getResourceBundle());
            loader.setLocation(Main.class.getResource("views/MainForm.fxml"));
            Scene scene = new Scene(loader.load());
            primaryStage.setScene(scene);
            primaryStage.show();
      
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void stop(){
        try{
            HubHandler hubH = HubHandler.getInstance();
            hubH.getHubControl().closeAll();
            hubH.getHubControl().getDbSession().closeSession();
            hubH=null;
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        initResources();
        initLogger();
        launch(args);  
    }
    
    private static void initResources() throws Exception {
        Settings settings = SettingsLoader.loadOrCreateEmpty();
        String locale = settings.getLang();
        if (locale == null) {
            locale = Resources.avaliableLocales.get(0);
        }
        Resources.setLang(locale);
    }
    
    private static void initLogger() {
        Logger.addTarget(new ConsoleLogTarget());
    }
    
}
