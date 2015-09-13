/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui;

import com.sun.javafx.runtime.VersionInfo;
import hubLibrary.meteringcomreader.TestFlashSession;
import hubOperations.Tester;
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
            loader.setLocation(Main.class.getResource("views/MainForm.fxml"));
            Scene scene = new Scene(loader.load());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        launch(args);       
    }
    
}
