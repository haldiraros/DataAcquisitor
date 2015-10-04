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
import hubGui.views.MainFormController;
import hubOperations.HubHandler;
import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 *
 * @author Marcin
 */
public class Main extends Application {
    
    private java.awt.SystemTray tray;
    private java.awt.TrayIcon trayIcon;
        
    @Override
    public void start(Stage primaryStage) {
        try {
            Platform.setImplicitExit(false);
            
            System.out.println("JavaFX runtime version: " + VersionInfo.getRuntimeVersion());
            
            addAppToTray(primaryStage);
            
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(Resources.getResourceBundle());
            loader.setLocation(Main.class.getResource("views/MainForm.fxml"));
            Scene scene = new Scene(loader.load());
            primaryStage.getIcons().add(Resources.getFxImage("ico16.png"));
            primaryStage.setScene(scene);
            primaryStage.show();
            
            MainFormController c = (MainFormController)loader.getController();
            c.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void stop(){
        try{
            HubHandler hubH = HubHandler.getInstance();
            hubH.getHubControl().closeAll();
            hubH.getDBSession().closeSession();
            hubH.shutdown();
            hubH=null;
        }catch(Exception e){
            e.printStackTrace();
        } finally {
            if (tray != null && trayIcon != null)
                tray.remove(trayIcon);
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
    
    private void addAppToTray(final Stage stage) {
        try {
            // ensure awt toolkit is initialized.
            java.awt.Toolkit.getDefaultToolkit();

            // app requires system tray support, just exit if there is no support.
            if (!java.awt.SystemTray.isSupported()) {
                System.out.println("No system tray support, application exiting.");
                Platform.exit();
            }

            // set up a system tray icon.
            tray = java.awt.SystemTray.getSystemTray();
            java.awt.Image image = Resources.getAwtImage("ico16.png");
            trayIcon = new java.awt.TrayIcon(image);

            // if the user double-clicks on the tray icon, show the main app stage.
            trayIcon.addActionListener(event -> Platform.runLater(() -> showAndBringToFront(stage)));

            // if the user selects the default menu item (which includes the app name), 
            // show the main app stage.
            java.awt.MenuItem openItem = new java.awt.MenuItem(Resources.getString("gui.tray.open"));
            openItem.addActionListener(event -> Platform.runLater(() -> showAndBringToFront(stage)));

            // the convention for tray icons seems to be to set the default icon for opening
            // the application stage in a bold font.
            java.awt.Font defaultFont = java.awt.Font.decode(null);
            java.awt.Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);
            openItem.setFont(boldFont);

            // to really exit the application, the user must go to the system tray icon
            // and select the exit option, this will shutdown JavaFX and remove the
            // tray icon (removing the tray icon will also shut down AWT).
            java.awt.MenuItem exitItem = new java.awt.MenuItem(Resources.getString("gui.tray.exit"));
            exitItem.addActionListener(event -> Platform.exit());

            // setup the popup menu for the application.
            final java.awt.PopupMenu popup = new java.awt.PopupMenu();
            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            // add the application tray icon to the system tray.
            tray.add(trayIcon);
        } catch (java.awt.AWTException e) {
            System.out.println("Unable to init system tray");
            e.printStackTrace();
        }
    }
    
    private void showAndBringToFront(Stage s) {
        s.show();
        s.toFront();
    }
}
