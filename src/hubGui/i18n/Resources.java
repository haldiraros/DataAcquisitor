/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui.i18n;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

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
    
    private static void assertCurrentLocaleIsNotEmpty() {
        if (currentLocale == null) {
            throw new RuntimeException("Current locale is empty.");
        }
    }
}
