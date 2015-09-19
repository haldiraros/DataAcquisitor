/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui.views;

import java.util.Optional;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

/**
 *
 * @author Marcin
 */
public class Dialogs {
    
    public static void showInfoAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(message);
        alert.showAndWait();
    }
    
    public static Optional<Pair<String, String>> inputStringPair(
            String title,
            String text,
            Pair<String, String> labels) {
        return inputStringPair(title, text, labels, null);
    }
    
    public static Optional<Pair<String, String>> inputStringPair(
            String title,
            String text,
            Pair<String, String> labels,
            Pair<String, String> values) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        
        dialog.setTitle(title);
        dialog.setHeaderText(text);
        
        ButtonType okButton = new ButtonType("Ok", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField text1 = new TextField();
        TextField text2 = new TextField();
        
        if (values != null) {
            text1.setText(values.getKey());
            text2.setText(values.getValue());
        }

        grid.add(new Label(labels.getKey()), 0, 0);
        grid.add(text1, 1, 0);
        grid.add(new Label(labels.getValue()), 0, 1);
        grid.add(text2, 1, 1);

        // Enable/Disable login button depending on whether a username was entered.
        Node okButtonNode = dialog.getDialogPane().lookupButton(okButton);
        if (text1.getText().equals(""))
            okButtonNode.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        text1.textProperty().addListener((observable, oldValue, newValue) -> {
            okButtonNode.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> text1.requestFocus());

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return new Pair<>(text1.getText(), text2.getText());
            }
            return null;
        });

        return dialog.showAndWait();
    }
    
    public static Optional<String> inputString(
            String title,
            String text,
            String label) {
        return inputString(title, text, label, null);
    }
    
    public static Optional<String> inputString(
            String title,
            String text,
            String label,
            String value) {
        Dialog<String> dialog = new Dialog<>();
        
        dialog.setTitle(title);
        dialog.setHeaderText(text);
        
        ButtonType okButton = new ButtonType("Ok", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);
        
        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField textField = new TextField();
        
        if (value != null) {
            textField.setText(value);
        }

        grid.add(new Label(label), 0, 0);
        grid.add(textField, 1, 0);

        // Enable/Disable login button depending on whether a username was entered.
        Node okButtonNode = dialog.getDialogPane().lookupButton(okButton);
        if (textField.getText().equals(""))
            okButtonNode.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            okButtonNode.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> textField.requestFocus());

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return textField.getText();
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
