package vpn_automation.gui.control;

import java.io.IOException;
import java.sql.SQLException;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vpn_automation.backend.db.UserDAO;

public class ChangeName {
	@FXML
	private Stage dialogStage;

	@FXML
	private TextField change_field;

	@FXML
	private Button submit_button;

	public void initialize(Consumer<String> profileName) {
		submit_button.setOnAction(event -> {

			String name = change_field.getText();
			UserDAO.ChangeName(name);
			try {
				updateProfileName(profileName);
			} catch (SQLException e) {

			}
			dialogStage.close();

		});
	}

	public ChangeName() {

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_files/new_name.fxml"));
			loader.setController(this); // Set the controller for the FXML
			Scene scene = new Scene(loader.load());
			dialogStage = new Stage();
			dialogStage.setScene(scene);
			dialogStage.setTitle("Create New Profile");
			dialogStage.initModality(Modality.APPLICATION_MODAL); // make it able to stack over others
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void show() {
		if (dialogStage != null)
			dialogStage.show();
	}

	@FXML
	private void onCloseButtonClick() throws SQLException {
		dialogStage.close();
	}

	private static void updateProfileName(Consumer<String> profileName) throws SQLException {
		profileName.accept(UserDAO.getActiveUserName());
	}

}
