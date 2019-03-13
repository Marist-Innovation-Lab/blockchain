package edu.marist.jointstudy.essence.ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.*;
import javafx.scene.control.Label;
import javafx.util.Duration;

public enum Helpers {
    ; // not meant to be instantiated

    // Turns a string into a fake string property (doesn't actually update as data updates)
    public static StringProperty newFakeStringProperty(String value) {
        StringProperty fakeProperty = new SimpleStringProperty();
        fakeProperty.set(value);
        return fakeProperty;
    }

    public static ObjectProperty<Integer> newFakeIntegerProperty(int value) {
        IntegerProperty fakeProperty = new SimpleIntegerProperty();
        fakeProperty.set(value);
        return fakeProperty.asObject(); // for some reason, https://community.oracle.com/thread/2575601?start=0&tstart=0
    }

    /** Makes a little toast animation at the bottom of the screen with the bread string. Use this to give little
     * notifications to the user. */
    public static void makeToast(Label toastLabel, String bread, int secondsShown) {
        toastLabel.setText(bread);
        toastLabel.setOpacity(0);
        toastLabel.setVisible(true);

        // fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), toastLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setCycleCount(1);

        // fade out
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), toastLabel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setCycleCount(1);

        PauseTransition wait = new PauseTransition(Duration.seconds(secondsShown));
        wait.setCycleCount(1);

        SequentialTransition sequence = new SequentialTransition();
        sequence.setOnFinished(event -> toastLabel.setVisible(false));

        // kind of beautiful code right here
        sequence.getChildren().addAll(
                fadeIn,
                wait,
                fadeOut
        );
        sequence.playFromStart();
    }
}
