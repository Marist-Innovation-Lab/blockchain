package edu.marist.jointstudy.essence.ui;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import okhttp3.HttpUrl;

import java.awt.*;
import java.util.Optional;

public class AddFriendDialogController {

    private Stage dialogStage;

    @FXML private TextField friendField;

    private boolean okayIsClicked = false;
    private Optional<HttpUrl> maybeFriend = Optional.empty();

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    @FXML private void initialize() {
        // setup stuff here
    }

    @FXML private void onOkayClicked() {
        maybeFriend = Optional.of(HttpUrl.parse(friendField.getText()));
        okayIsClicked = true;
    }

    public Optional<HttpUrl> isOkayClicked() {
        if(okayIsClicked) {
            return maybeFriend;
        }
        return Optional.empty();
    }

}
