package vpn_automation.gui.control;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import vpn_automation.backend.Policies;
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
		ErrorDialog dialog = new ErrorDialog();
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

		// Check the credentials are okay or not
		if (!Policies.isValidEmail(email)) {
			dialog.setErrorMessage("Invalid Email format");
			dialog.show();
		} else if (password.isEmpty()) {
			dialog.setErrorMessage("Password fill the password field.");
			dialog.show();
		} else if (!Policies.hasUpperCase(password)) {
			dialog.setErrorMessage("Password must have at least\n one capital letter!");
			dialog.show();
		} else if (!Policies.hasLowerCase(password)) {
			dialog.setErrorMessage("Password must have at least\n one lower case letter!");
			dialog.show();
		} else if (!Policies.hasMinLength(password)) {
			dialog.setErrorMessage("Your password is too short!");
			dialog.show();
		} else if (!password.equals(confirmPassword)) {
			dialog.setErrorMessage("Password didn't match!");
			dialog.show();
		} else {
			dialog.setErrorMessage("Succeed!");
			dialog.show();
		}
		// if (!password.equals(confirmPassword)) {
		// ErrorDialog dialog = new ErrorDialog();
		// dialog.setErrorMessage("Password didn't match!");
		// dialog.show();
		// }

	}

	private String capitalize(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}
		return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
	}
}
