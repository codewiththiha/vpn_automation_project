package vpn_automation.gui.control;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import vpn_automation.gui.NavigationUtils;

public class LoginOrRegisterController {

	@FXML
	private Button login_button;

	@FXML
	private Button register_button;

	@FXML
	private Button back_button;

	@FXML
	private void initialize() {
		login_button.setOnAction(event -> NavigationUtils.navigateTo(event, "/fxml_files/login_form.fxml"));
		register_button.setOnAction(event -> NavigationUtils.navigateTo(event, "/fxml_files/register_form.fxml"));
	}

}