package edu.marist.jointstudy.essence.ui;

import com.sun.javafx.collections.ObservableListWrapper;
import edu.marist.jointstudy.essence.api.Peer;
import edu.marist.jointstudy.essence.api.client.BlockchainPullObserver;
import edu.marist.jointstudy.essence.api.client.Friend;
import edu.marist.jointstudy.essence.api.client.LoggingBlockchainObserver;
import edu.marist.jointstudy.essence.api.server.APIConstants;
import edu.marist.jointstudy.essence.api.store.Preferences;
import edu.marist.jointstudy.essence.core.hash.Hashcode;
import edu.marist.jointstudy.essence.core.structures.Block;
import edu.marist.jointstudy.essence.core.structures.Transaction;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import okhttp3.HttpUrl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static edu.marist.jointstudy.essence.ui.Helpers.newFakeIntegerProperty;
import static edu.marist.jointstudy.essence.ui.Helpers.newFakeStringProperty;

public class HomeController {

    private App app;

    /** Peers contain the server and client logic associated with the node. */
    private Peer peer;

    @FXML private AnchorPane transactionBufferPane;
    @FXML private TransactionTableViewController transactionBufferPaneController;

    @FXML private MenuItem requestMiningItem;

    @FXML private ProgressIndicator miningIndicator;

    @FXML private Button requestMiningButton;

    // Blockchain table
    @FXML private TableView<Block> blockchainTableView;
    @FXML private TableColumn<Block, Integer> blockIdColumn;
    @FXML private TableColumn<Block, String> hashColumn;
    @FXML private TableColumn<Block, String> nonceColumn;
    @FXML private TableColumn<Block, String> merkleRootColumn;
    @FXML private TableColumn<Block, String> previousBlockHashColumn;
    @FXML private TableColumn<Block, Integer> numberOfTxColumn;

    @FXML private AnchorPane selectedBlock;
    @FXML private TransactionTableViewController selectedBlockController;

    @FXML private Button toggleServerButton;

    @FXML private Label peerStatusLabel;

    @FXML private Label apiNameLabel;

    @FXML private Label toastLabel;

    @FXML private ListView<String> friendListView;

    @FXML private AnchorPane currentFriendDownloadStatus;

    private List<Friend> friends;

    /** An observable boolean value indicating whether the peer is running. */
    private BooleanProperty peerIsRunning = new SimpleBooleanProperty();

    // setup goes here
    @FXML private void initialize() {

        apiNameLabel.setText(APIConstants.apiName);

        // if the serverIsRunning boolean changes, automatically update the label
        peerIsRunning.addListener(this.onPeerStatusChanged);
        peerIsRunning.setValue(false);
    }

    /**
     * Setup for after the initialize goes here.
     * Must be called after the {@code setApp()} is set.
     */
    public void initServer() {
        this.peer = new Peer(Preferences.getPort(), Preferences.getFriendlyUrls());

        this.friends = peer.friends();
        this.friendListView.getItems().setAll(
                this.friends.stream().map((f) -> f.baseUrl().toString()).collect(Collectors.toList())
        );

        // observe all the friends through logging
        this.friends.forEach((f) -> f.addObserver(new LoggingBlockchainObserver(f.baseUrl().toString())));

        initTransactionBufferTable();
        initBlockchainTable();
        initFriendListView();
        Helpers.makeToast(toastLabel,"Welcome!", 2);
        peer.start();
    }

    private void initTransactionBufferTable() {
        startPeriodicRunnable(2, this::refreshTxBuffer);
    }

    private void initBlockchainTable() {
        blockIdColumn.setCellValueFactory((cellData) -> newFakeIntegerProperty(cellData.getValue().getId()));
        hashColumn.setCellValueFactory((cellData) -> newFakeStringProperty(cellData.getValue().getHash().toString()));
        nonceColumn.setCellValueFactory((cellData) -> newFakeStringProperty(cellData.getValue().getNonce().toString()));
        merkleRootColumn.setCellValueFactory((cellData) ->
                newFakeStringProperty(cellData.getValue().getTransactionsAsMerkleTree().getMerkleRoot().toString()));

        previousBlockHashColumn.setCellValueFactory((cellData) ->  {
            Optional<Hashcode> hashCode = cellData.getValue().getPreviousBlockHash();
            return newFakeStringProperty(hashCode.isPresent() ? hashCode.get().toString() : "None");
        });
        numberOfTxColumn.setCellValueFactory((cellData) ->
                newFakeIntegerProperty(cellData.getValue().getTransactions().size()));

        startPeriodicRunnable(2, this::refreshBlockchain);

        blockchainTableView.getSelectionModel().selectedItemProperty().addListener(
                ((observable, oldValue, newValue) -> {
                    showBlock(newValue);
                })
        );
    }

    private void showBlock(Block b) {
        if(b != null) {
            this.selectedBlockController.setTransactions(b);
        }
    }

    private void initFriendListView() {
        this.currentFriendDownloadStatus.getChildren().add(
                new HBox(10, dlStatusLabel, dlStatusBar, countdownLabel)
        );
        dlStatusBar.setVisible(false);
        this.friendListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    dlStatusBar.setVisible(true);
                    showDownload(newValue);
                }
        );
    }
    private Friend selectedFriend;
    private Label dlStatusLabel = new Label();
    private ProgressBar dlStatusBar = new ProgressBar();
    private Label countdownLabel = new Label();
    private BlockchainPullObserver obs = new UIBlockchainObserver(dlStatusLabel, dlStatusBar, countdownLabel, 5);

    private void showDownload(String friendUrl) {
        // find the friend in the list of our friends based on the selected url
        Optional<Friend> maybeFriend =
                this.friends.stream().filter((f) -> f.baseUrl().toString().equals(friendUrl)).findFirst();

        // if it's not in our list, no biggie, just don't do anything
        if(!maybeFriend.isPresent()) {
            System.err.println("Warning: selected a download with an invalid friend url.");
            return;
        }

        // our observer is observing the previous selected friend, remove it because we don't want to
        // observe that friend anymore
        if(selectedFriend != null) {
            selectedFriend.removeObserver(obs);
        }

        // get the new friend
        selectedFriend = maybeFriend.get();

        // start observing that new friend, displayed on the right in the GUI
        selectedFriend.addObserver(obs);
    }

    @FXML
    private void onRequestMining(ActionEvent ae) {
        CompletableFuture<Void> miningRequest = peer.requestMining();

        if(miningRequest == null) {
            Helpers.makeToast(toastLabel, "No transactions to mine.", 1);
            return;
        }

        setMiningUIOn(true);

        // after the mining is completed...
        miningRequest.thenAccept((ignored) -> {
            setMiningUIOn(false);
            refreshAll(); // both tx buffer and blockchain

            // make sure that rendering the toast happens on JavaFX's main thread
            Platform.runLater(() -> Helpers.makeToast(toastLabel, "Finished mining.", 1));
        });
        Helpers.makeToast(toastLabel, "Started mining.", 1);
    }

    private void setMiningUIOn(boolean miningIsHappening) {
        miningIndicator.setVisible(miningIsHappening);
        requestMiningButton.setDisable(miningIsHappening);
        requestMiningItem.setDisable(miningIsHappening);
    }

    /**
     * 1. Change the peer status label to reflect the new peer status.
     * 2. Turn peer on or off if changed.
     */
    private final ChangeListener<? super Boolean> onPeerStatusChanged = (observable, wasRunning, isCurrentlyRunning) -> {
        peerStatusLabel.setText(isCurrentlyRunning ? "running" : "stopped");
        if(!wasRunning && isCurrentlyRunning) {
            // better have that server running
            peer.start();
            toggleServerButton.setText("Stop Server");
            peerStatusLabel.setTextFill(Color.GREEN);
        } else if (wasRunning && !isCurrentlyRunning){
            // turn it off!
            peer.stop();
            toggleServerButton.setText("Start Server");
            peerStatusLabel.setTextFill(Color.MAROON);
        }
    };

    /** Refresh the transaction buffer tableview every secondsBetween seconds */
    private void startPeriodicRunnable(long secondsBetween, Runnable r) {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(
                r,
                0,
                secondsBetween,
                TimeUnit.SECONDS
        );
    }

    /** Refreshes the transaction buffer and blockchain. */
    private void refreshAll() {
        refreshTxBuffer();
        refreshBlockchain();
    }

    private void refreshTxBuffer() {
        Platform.runLater(() -> transactionBufferPaneController.setTransactions(peer.transactionBuffer()));
    }

    private void refreshBlockchain() {
        Platform.runLater(() -> blockchainTableView.setItems(new ObservableListWrapper<>(peer.blocks())));
    }

    @FXML private void onToggleServer(ActionEvent ae) {
        peerIsRunning.setValue(toggleServerButton.getText().equals("Start Server"));
    }

    @FXML private void onAddFriend(ActionEvent ae) {
        Optional<HttpUrl> friend = app.showAddFriendDialog();
        if(friend.isPresent()) {
            Helpers.makeToast(toastLabel, friend + " added.", 2);
        }
    }

    @FXML private void onAddTransactionToBuffer(ActionEvent ae) {
        Dialog<Transaction> d = Dialogs.createTransaction();

        d.showAndWait().ifPresent((transaction) -> {
            peer.submitTransactionToBuffer(transaction);
            refreshTxBuffer();
        });
    }

    @FXML private void onPreferencesAction(ActionEvent e) {
        Dialogs.preferences(Preferences.getPort()).showAndWait().ifPresent(System.out::println);
    }

    @FXML private void onQuit() {
        System.exit(0);
    }

    public void setApp(App app) {
        this.app = app;
    }
}
