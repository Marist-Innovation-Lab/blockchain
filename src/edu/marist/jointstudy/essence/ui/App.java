package edu.marist.jointstudy.essence.ui;

import edu.marist.jointstudy.essence.api.CommandLine;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import okhttp3.HttpUrl;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class App extends Application {

    private static final int APP_WIDTH = 1280;
    private static final int APP_HEIGHT = 800;

    private int port;
    private List<HttpUrl> friendUrls;

    public static void main(String[] args) {
        launch(args);
        Application.launch();
    }

    private Stage primaryStage;

    private FXMLLoader loader = new FXMLLoader();

    public List<HttpUrl> getFriendUrls() {
        return friendUrls;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        String[] args = getParameters().getRaw().toArray(new String[getParameters().getRaw().size()]);
        if(!CommandLine.areValidArguments(args)) {
            System.err.println("Invalid arguments.");
            System.exit(-1);
        }

        // Gather this peer's port, and the urls of its friends
        Pair<Integer, List<HttpUrl>> prefs = CommandLine.setupPreferences(args);

        loader.setLocation(getClass().getResource("home.fxml"));
        Parent root = loader.load();
        HomeController controller = loader.getController();
        controller.setApp(this);
        controller.initServer(); // starts the server
        primaryStage.setTitle("EssentialView");
        Scene mainScene = new Scene(root, APP_WIDTH, APP_HEIGHT);
        // mainScene.getStylesheets().add(getClass().getResource("cssFileHere.css").toExternalForm());
        primaryStage.setScene(mainScene);
        primaryStage.show();

        // callback when someone tries to exit the application (e.g. via the red exit)
        primaryStage.setOnCloseRequest(r -> {
            Platform.exit(); // exits all the javafx stuff
            System.exit(0); // kills the threads running the server and client stuff
        });
        loader.setRoot(null);
    }

    public Optional<HttpUrl> showAddFriendDialog() {
        try {
            loader.setLocation(getClass().getResource("add_friend_dialog.fxml"));
            AnchorPane page = loader.load();

            // Dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add Friend");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(this.primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            AddFriendDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            return controller.isOkayClicked();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return Optional.empty();
        }
    }
}
