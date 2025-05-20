package vpn_automation.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NavigationUtils {

	/**
	 * Navigates to the specified FXML file in the same window.
	 *
	 * @param event    The ActionEvent that triggered the navigation
	 * @param fxmlFile Path to the FXML file (e.g., "/fxml_files/login_form.fxml")
	 */
	public static void navigateTo(ActionEvent event, String fxmlFile) {
		try {
			Parent root = FXMLLoader.load(NavigationUtils.class.getResource(fxmlFile));
			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void navigateTo(Stage stage, String fxmlFile) {
		try {
			Parent root = FXMLLoader.load(NavigationUtils.class.getResource(fxmlFile));
			stage.setScene(new Scene(root));
			stage.show();
		} catch (IOException e) {
			System.err.println("Error loading FXML file: " + fxmlFile);
			e.printStackTrace();
		}
	}
}