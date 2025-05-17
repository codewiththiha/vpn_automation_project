package vpn_automation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Gui extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		// Load the FXML file
		Parent root = FXMLLoader.load(getClass().getResource("/assets/gui.fxml"));

		// Create the scene with the loaded FXML
		Scene scene = new Scene(root, 400, 300);

		// Set stage title and scene
		primaryStage.setTitle("JavaFX GUI Example");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}