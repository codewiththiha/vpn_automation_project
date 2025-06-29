package vpn_automation.gui.control;

import java.io.IOException;
import java.sql.SQLException;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vpn_automation.backend.Policies;
import vpn_automation.backend.db.UserDAO;

public class ChangePassword {
	@FXML
	private Stage dialogStage;

	@FXML
	private TextField change_field;

	@FXML
	private Button submit_button;

	@FXML
	private PasswordField confirm_field;

	ErrorDialog dialog = new ErrorDialog();

	public void initialize() {
		submit_button.setOnAction(event -> {
			String oldPass = confirm_field.getText();
			String newPass = change_field.getText();

			try {
				if (oldPass.equals(UserDAO.getActiveUserPass())) {
					if (oldPass.equals(newPass)) {
						dialog.setErrorMessage("New Pass can't be same as the old one.");
						dialog.show();
					} else {
						System.out.println(oldPass + " " + newPass);
						if (newPass.isEmpty()) {
							dialog.setErrorMessage("Password fill the newPass field.");
							dialog.show();
						} else if (!Policies.hasUpperCase(newPass)) {
							dialog.setErrorMessage("Password must have at least\n one capital letter!");
							dialog.show();
						} else if (!Policies.hasLowerCase(newPass)) {
							dialog.setErrorMessage("Password must have at least\n one lower case letter!");
							dialog.show();
						} else if (!Policies.hasMinLength(newPass)) {
							dialog.setErrorMessage("Your newPass is too short!");
							dialog.show();
						} else {
							UserDAO.ChangePassword(newPass);
							dialogStage.close();
						}
					}
				} else {
					dialog.setErrorMessage("Please Enter correct Old Password");
					dialog.show();
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});
	}

	public ChangePassword() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_files/new_password.fxml"));
			loader.setController(this); // Set the controller for the FXML
			Scene scene = new Scene(loader.load());
			dialogStage = new Stage();
			dialogStage.setScene(scene);
			dialogStage.setTitle("Change Password");
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

}