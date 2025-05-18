package vpn_automation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginOrRegisterController {

	@FXML
	private Button login_button;

	@FXML
	private Button register_button;

	@FXML
	private void initialize() {
		login_button.setOnAction(event -> loadForm(event, "/fxml_files/login_form.fxml"));
		register_button.setOnAction(event -> loadForm(event, "/fxml_files/register_form.fxml"));
	}

	private void loadForm(javafx.event.ActionEvent event, String fxmlFile) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}