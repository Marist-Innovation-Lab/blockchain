package edu.marist.jointstudy.essence.ui;

import edu.marist.jointstudy.essence.Util;
import edu.marist.jointstudy.essence.core.security.Security;
import edu.marist.jointstudy.essence.core.structures.Transaction;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public enum Dialogs {
    ; // not meant to be instantiated

    public static Dialog<Transaction> createTransaction() {
        Dialog<Transaction> d = new Dialog<>();
        d.setTitle("Add Transaction");
        d.setHeaderText("New Transaction");

        ButtonType okay = ButtonType.OK;
        ButtonType cancel = ButtonType.CANCEL;
        d.getDialogPane().getButtonTypes().addAll(okay, cancel);
        d.getDialogPane().lookupButton(okay).setDisable(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Payload"), 0, 0);
        TextField payload = new TextField();
        grid.add(payload, 1, 0);

        grid.add(new Label("Public Key"), 0, 1);
        TextField publicKey = new TextField();
        publicKey.setDisable(true); // program is going to fill this in
        grid.add(publicKey, 1, 1);

        publicKey.setText(Security.INSTANCE.getPublicKeyHexadecimal());

        grid.add(new Label("Signature"), 0, 2);
        TextField signature = new TextField();
        signature.setDisable(true); // program is going to fill this in
        grid.add(signature, 1, 2);

        payload.textProperty().addListener((obs, oldVal, newVal) -> {
            d.getDialogPane().lookupButton(okay).setDisable(newVal.isEmpty());
            try {
                signature.setText(Security.INSTANCE.sign(newVal));
                if(newVal.isEmpty()) {
                    signature.setText("");
                }
            } catch (Exception e) {
                e.printStackTrace();
                d.close(); // TODO: bad UI but idc
            }
        });

        d.getDialogPane().setContent(grid);

        // set the focus on the payload text box
        d.setOnShown(e -> {
            Platform.runLater(payload::requestFocus);
        });

        d.setResultConverter(dialogButton -> {
            // identity
            if(dialogButton != okay) {
                return null;
            }
            try {
                if (payload.getText().isEmpty()) {
                    return null;
                }
            } catch (NullPointerException invalidInput) {
                return null;
            }
            return Transaction.newTransaction(payload.getText());
        });
        return d;
    }

    public static Dialog<Integer> preferences(int port) {
        // Create a custom dialog
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Preferences");
        dialog.setHeaderText("Preferences");

        ButtonType okayButton = ButtonType.OK;
        ButtonType cancelButton = ButtonType.CANCEL;

        dialog.getDialogPane().getButtonTypes().addAll(okayButton, cancelButton);


        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Port"), 0, 0);
        TextField portTextField = new TextField(String.valueOf(port));
        grid.add(portTextField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        portTextField.setDisable(true);

        dialog.setResultConverter(dialogButton -> {
            // checking for identity
            if(dialogButton != okayButton) {
                return null;
            }
            try {
                if(Util.isInt(portTextField.getText())) {
                    return Integer.parseInt(portTextField.getText());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        });
        return dialog;
    }
}
