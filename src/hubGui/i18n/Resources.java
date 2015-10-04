/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui.i18n;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;

/**
 *
 * @author Marcin
 */
public class Resources {
    
    private static Locale currentLocale;
    
    public static final List<String> avaliableLocales = Arrays.asList("en", "pl");
    
    public static void setLang(String countryCode) {
        if (!avaliableLocales.contains(countryCode)) {
            throw new RuntimeException("Locale '" + countryCode + "' not available.");
        }
        currentLocale = new Locale(countryCode);
    }

    public static ResourceBundle getResourceBundle() {
        assertCurrentLocaleIsNotEmpty();
        return ResourceBundle.getBundle("hubGui.i18n.lang", currentLocale);
    }
    
    public static String getString(String key) {
        return getResourceBundle().getString(key);
    }
    
    public static String getFormatString(String key, Object... args) {
        return String.format(getString(key), args);
    }
    
    public static String getCurrentLang() {
        assertCurrentLocaleIsNotEmpty();
        return currentLocale.getLanguage();
    }
    
    public static java.awt.Image getAwtImage(String name) {
        try {
            return ImageIO.read(Resources.class.getResource(name));
        } catch (IOException ex) {
            return null;
        }
    }
    
    public static javafx.scene.image.Image getFxImage(String name) {
        return new javafx.scene.image.Image(Resources.class.getResourceAsStream(name));
    }
    
    private static void assertCurrentLocaleIsNotEmpty() {
        if (currentLocale == null) {
            throw new RuntimeException("Current locale is empty.");
        }
    }
}
