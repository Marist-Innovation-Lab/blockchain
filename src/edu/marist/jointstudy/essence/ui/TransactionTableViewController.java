package edu.marist.jointstudy.essence.ui;

import com.sun.javafx.collections.ObservableListWrapper;
import edu.marist.jointstudy.essence.core.structures.Block;
import edu.marist.jointstudy.essence.core.structures.Transaction;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

import static edu.marist.jointstudy.essence.ui.Helpers.newFakeIntegerProperty;
import static edu.marist.jointstudy.essence.ui.Helpers.newFakeStringProperty;

public class TransactionTableViewController {

    @FXML private TableView<Transaction> transactionTableView;
    @FXML private TableColumn<Transaction, Integer> idColumn;
    @FXML private TableColumn<Transaction, String> payloadColumn;
    @FXML private TableColumn<Transaction, String> publicKeyColumn;
    @FXML private TableColumn<Transaction, String> signatureColumn;

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory((cellData) -> newFakeIntegerProperty(cellData.getValue().getId()));
        payloadColumn.setCellValueFactory((cellData) -> newFakeStringProperty(cellData.getValue().getPayload()));
        publicKeyColumn.setCellValueFactory((cellData) -> newFakeStringProperty(cellData.getValue().getPublicKey()));
        signatureColumn.setCellValueFactory((cellData) -> newFakeStringProperty(cellData.getValue().getSignature()));
    }

    public void setTransactions(List<Transaction> txs) {
        this.transactionTableView.setItems(new ObservableListWrapper<>(txs));
    }

    public void setTransactions(Block block) {
        this.transactionTableView.setItems(new ObservableListWrapper<>(block.getTransactions()));
    }
}
