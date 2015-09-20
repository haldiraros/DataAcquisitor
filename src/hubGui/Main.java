/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui;

import com.sun.javafx.runtime.VersionInfo;
import hubGui.logging.ConsoleLogTarget;
import hubGui.logging.LogTyps;
import hubGui.logging.Logger;
import hubLibrary.meteringcomreader.TestFlashSession;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import hubOperations.HubHandler;
import hubOperations.Tester;
import java.io.IOException;
import java.util.logging.Level;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import localDB.menagers.LocalDataBaseMenager;
import project.data.Datagram;
import project.data.Session;
import test.LocalDBTests;

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
            loader.setLocation(Main.class.getResource("views/MainForm.fxml"));
            Scene scene = new Scene(loader.load());
            primaryStage.setScene(scene);
            primaryStage.show();
            
            HubHandler hubH = HubHandler.getInstance();
            try{
                hubH.getHubControl().openHubConn();
                //hubH.getHubControl().closeAllSesssions();
            }catch (MeteringSessionException ex) {
                 java.util.logging.Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
                
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void stop(){
        HubHandler hubH = HubHandler.getInstance();
        hubH.getHubControl().closeAllSesssions();
        hubH.getHubControl().closeHubConn();
    }
    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        Logger.addTarget(new ConsoleLogTarget());
        //LocalDBTests.main(args);
        launch(args);  
    }
    
}
