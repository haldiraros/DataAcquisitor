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
        // na upartego możnaby dołożyć sprawdzanie, czy to jest poprawny Path w danym systemie plików
        return Config.getString(key);
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
