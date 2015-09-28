/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui.views;

import hubGui.i18n.Resources;
import hubGui.models.IdKeyPair;
import hubGui.settings.HubConfig;
import hubGui.settings.ProxySetter;
import hubGui.settings.Settings;
import hubGui.settings.SettingsLoader;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Pair;

/**
 * FXML Controller class
 *
 * @author Marcin
 */
public class SettingsFormController implements Initializable {

    @FXML
    private TableView<IdKeyPair> hubIdKeyTable;
    
    @FXML
    private TableColumn<IdKeyPair, String> hubIdColumn;
    
    @FXML
    private TableColumn<IdKeyPair, String> hubKeyColumn;
    
    @FXML
    private TextField restUrlText;
    
    @FXML
    private ComboBox langCombo;
    
    @FXML
    private CheckBox useProxyCheck;
    
    @FXML
    private TextField proxyHostText;
    
    @FXML
    private TextField proxyPortText;
    
    @FXML
    private TextField proxyUserText;
    
    @FXML
    private PasswordField proxyPassword;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        hubIdColumn.setCellValueFactory(c -> c.getValue().getIdProperty());
        hubKeyColumn.setCellValueFactory(c -> c.getValue().getKeyProperty());
        
        ObservableList<IdKeyPair> idKeyPairs = FXCollections.observableArrayList();
        hubIdKeyTable.setItems(idKeyPairs);
        
        ObservableList<String> locales = FXCollections.observableArrayList(Resources.avaliableLocales);
        langCombo.setItems(locales);
        
        proxyPortText.textProperty().addListener(
                (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    if (newValue!=null && !newValue.matches("\\d*")) {
                        proxyPortText.setText(oldValue);
                    }
                });
        
        useProxyCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                boolean disable = newValue == null ? true : !newValue;
                proxyHostText.setDisable(disable);
                proxyPortText.setDisable(disable);
                proxyUserText.setDisable(disable);
                proxyPassword.setDisable(disable);
            }
        });
        
        // Fire first changed event.
        useProxyCheck.setSelected(!useProxyCheck.isSelected());
        
        try {
            Settings settings = SettingsLoader.loadOrCreateEmpty();
            setSettings(settings);
        }
        catch(Exception ex) {
            ex.printStackTrace();
            Dialogs.showInfoAlert(Resources.getFormatString("msg.settings.errorOnLoadingSettings", ex.getMessage()));
            close();
        }
    }    
    
    @FXML
    private void addActionHandler(ActionEvent event) {
        Optional<Pair<String, String>> result = Dialogs.inputStringPair(
                Resources.getString("msg.settigns.hubConfigTitle"),
                Resources.getString("msg.settigns.hubConfigDescription"),
                new Pair<>(
                        Resources.getString("msg.settings.hubId"),
                        Resources.getString("msg.settigns.hubKey")));
        
        if (result.isPresent()) {
            String id = result.get().getKey();
            String key = result.get().getValue();
            IdKeyPair pair = new IdKeyPair(id, key);
            hubIdKeyTable.getItems().add(pair);
        }
    }
    
    @FXML
    private void editActionHandler(ActionEvent event) {
        int index = hubIdKeyTable.getSelectionModel().getSelectedIndex();
        if (index == -1) {
            showNoHubConfigSelectedAlert();
            return;
        }
        
        ObservableList<IdKeyPair> items = hubIdKeyTable.getItems();
        IdKeyPair idKeyPair = items.get(index);
        
        Optional<Pair<String, String>> result = Dialogs.inputStringPair(
                Resources.getString("msg.settigns.hubConfigTitle"),
                Resources.getString("msg.settigns.hubConfigDescription"),
                new Pair<>(
                        Resources.getString("msg.settings.hubId"),
                        Resources.getString("msg.settigns.hubKey")),
                new Pair<>(
                        idKeyPair.getId(),
                        idKeyPair.getKey()));
        
        if (result.isPresent()) {
            String id = result.get().getKey();
            String key = result.get().getValue();
            idKeyPair.setId(id);
            idKeyPair.setKey(key);
        }
    }
    
    @FXML
    private void deleteActionHandler(ActionEvent event) {
        int index = hubIdKeyTable.getSelectionModel().getSelectedIndex();
        if (index == -1) {
            showNoHubConfigSelectedAlert();
            return;
        }
        
        hubIdKeyTable.getItems().remove(index);
    }
    
    @FXML
    private void cancelActionHandler(ActionEvent event) {
        close();
    }
    
    @FXML
    private void okActionHandler(ActionEvent event) {
        try {
            if(langCombo.getValue()!= Resources.getCurrentLang()){
                Dialogs.showInfoAlert(Resources.getString("msg.settings.langChangeRestartNeeded"));
            }
            Settings settings = getSettings();
            setProxySettings(settings);
            SettingsLoader.save(settings); 
            close();
        }
        catch (Exception ex) {
            Dialogs.showInfoAlert(Resources.getFormatString("msg.settigns.errorOnSavingSettings", ex.getMessage()));
        }
    }
    
    private static void showNoHubConfigSelectedAlert() {
        Dialogs.showInfoAlert(Resources.getString("msg.settigns.noHubConfigSelected"));
    }
    
    private void close() {
        Stage stage = (Stage) hubIdKeyTable.getScene().getWindow();
        stage.close();
    }
    
    private void setProxySettings(Settings settings) {
        boolean useProxy = settings.isUseProxy() == null ? false : settings.isUseProxy();
        if (useProxy) {
            ProxySetter.setProxy(
                    settings.getProxyHost(),
                    settings.getProxyPort(),
                    settings.getProxyUser(),
                    settings.getProxyPassword());
        } else {
            ProxySetter.unsetProxy();
        }
    }
    
    private void setSettings(Settings settings) {
        restUrlText.setText(settings.getRestUrl());
        useProxyCheck.setSelected(settings.isUseProxy() == null ? false : settings.isUseProxy());
        proxyHostText.setText(settings.getProxyHost());
        proxyPortText.setText(settings.getProxyPort());
        proxyUserText.setText(settings.getProxyUser());
        proxyPassword.setText(settings.getProxyPassword());
        ObservableList<IdKeyPair> items = hubIdKeyTable.getItems();
        for(HubConfig hubConfig : settings.getHubConfigs()) {
            IdKeyPair pair = new IdKeyPair(hubConfig.getId(), hubConfig.getKey());
            items.add(pair);
        }
        String lang = Resources.getCurrentLang();
        langCombo.getSelectionModel().select(lang);
    }
    
    private Settings getSettings() {
        Settings settings = new Settings();
        settings.setRestUrl(restUrlText.getText());
        settings.setUseProxy(useProxyCheck.isSelected());
        settings.setProxyHost(proxyHostText.getText());
        settings.setProxyPort(proxyPortText.getText());
        settings.setProxyUser(proxyUserText.getText());
        settings.setProxyPassword(proxyPassword.getText());
        for(IdKeyPair pair : hubIdKeyTable.getItems()) {
            HubConfig hubConfig = new HubConfig(pair.getId(), pair.getKey());
            settings.getHubConfigs().add(hubConfig);
        }
        settings.setLang((String)langCombo.getSelectionModel().getSelectedItem());
        return settings;
    }
    
}
