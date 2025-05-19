package vpn_automation.gui.control;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import vpn_automation.gui.NavigationUtils;

public class RegisterController {

	@FXML
	private Button back_button;

	@FXML
	private TextField email_field;

	@FXML
	private TextField first_name_field;

	@FXML
	private TextField last_name_field;

	@FXML
	private PasswordField password_confirm_field;

	@FXML
	private PasswordField password_field;

	@FXML
	private Button register_button;

	@FXML
	private void initialize() {
		back_button.setOnAction(event -> NavigationUtils.navigateTo(event, "/fxml_files/login_or_register.fxml"));
		register_button.setOnAction(event -> handleRegister());
	}

	@FXML
	private void handleRegister() {
		String firstName = first_name_field.getText();
		String lastName = last_name_field.getText();
		String email = email_field.getText();
		String password = password_field.getText();
		String confirmPassword = password_confirm_field.getText();

		String fullName = capitalize(firstName) + capitalize(lastName);
		System.out.println(fullName);
		System.out.println(email);
		System.out.println(password);
		System.out.println(confirmPassword);
		if (!password.equals(confirmPassword)) {
			ErrorDialog dialog = new ErrorDialog();
			dialog.setErrorMessage("Password didn't match!");
			dialog.show();
		}

	}

	private String capitalize(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}
		return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
	}
}
