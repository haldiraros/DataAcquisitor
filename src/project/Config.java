/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project;

import hubGui.i18n.Resources;
import hubGui.logging.LogTyps;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * @author hp
 */
public class Config {

    private static final String BUNDLE_NAME = "project.settings";

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(BUNDLE_NAME);

    public static String getPath(String key) {
        String workingDirectory;
        //here, we assign the name of the OS, according to Java, to a variable...
        String OS = (System.getProperty("os.name")).toUpperCase();
        //to determine what the workingDirectory is.
        //if it is some version of Windows
        if (OS.contains("WIN")) {
            //it is simply the location of the "AppData" folder
            workingDirectory = System.getenv("AppData");
        } //Otherwise, we assume Linux or Mac
        else {
            //in either case, we would start in the user's home directory
            workingDirectory = System.getProperty("user.home");
            //if we are on a Mac, we are not done, we look for "Application Support"
            if (!OS.contains("NUX")) { // chcecking if the system is some kind of Linux
                workingDirectory += "/Library/Application Support";
            }
        }
        //we are now free to set the workingDirectory to the subdirectory that is our file.
        String separator = java.nio.file.FileSystems.getDefault().getSeparator();
        workingDirectory = workingDirectory
                + separator + Config.getString("application.folder.path")
                + separator + Config.getString(key);
        return workingDirectory;
    }

    private Config() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public static Integer getInteger(String key) {
        try {
            return Integer.parseInt(RESOURCE_BUNDLE.getString(key));
        } catch (MissingResourceException e) {
            hubGui.logging.Logger.write(Resources.getFormatString("project.Config.getIntegerMissingResourceException", key), LogTyps.ERROR);
            return null;
        }
    }
}
