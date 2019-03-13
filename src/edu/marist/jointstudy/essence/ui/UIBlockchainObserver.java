package edu.marist.jointstudy.essence.ui;

import edu.marist.jointstudy.essence.api.client.BlockchainPullEvent;
import edu.marist.jointstudy.essence.api.client.BlockchainPullObserver;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class UIBlockchainObserver extends BlockchainPullObserver {

    private Label bcLabel;
    private ProgressBar indicator;
    private Label counter;
    private int seconds;

    public UIBlockchainObserver(Label bcLabel, ProgressBar indicator, Label counter, int seconds) {
        super("UI Blockchain Observer");
        Platform.runLater(() -> {
            this.bcLabel = bcLabel;
            this.indicator = indicator;
            this.counter = counter;
        });
        this.seconds = seconds;
    }

    @Override
    public void on(BlockchainPullEvent e) {
        switch (e) {
            case DOWNLOADING:
                onDownloading();
                break;
            case DOWNLOADED:
                onDownloaded();
                break;
            case SAVING:
                onSaving();
                break;
            case SAVED:
                onSaved();
                break;
            case SKIPPED:
                onSkipped();
                break;
        }
    }

    private void onDownloading() {
        Platform.runLater(() -> {
            indicator.setStyle("");
            indicator.setProgress(0.25);
            bcLabel.setText("Downloading bc...");
        });
    }

    private void onDownloaded() {
        Platform.runLater(() -> {
            indicator.setProgress(0.50);
            bcLabel.setText("Downloaded bc.");
        });
    }

    private void onSaving() {
        Platform.runLater(() -> {
            indicator.setProgress(0.75);
            bcLabel.setText("Saving bc...");
        });
    }

    private void onSaved() {
        Platform.runLater(() -> {
            indicator.setProgress(1.0);
            bcLabel.setText("Saved bc.");
        });
    }

    private void onSkipped() {
        Platform.runLater(() -> {
            indicator.setProgress(1.0);
            bcLabel.setText("Skipped bc.");
        });
    }

    @Override
    public void onFailed(Exception e) {
        Platform.runLater(() -> {
            bcLabel.setText("Failed.");
            indicator.setProgress(0.99);
        });
    }
}
