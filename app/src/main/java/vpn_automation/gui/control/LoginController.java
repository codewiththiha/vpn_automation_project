package vpn_automation.gui.control;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import vpn_automation.gui.NavigationUtils;

public class LoginController {

	@FXML
	private Button back_button;

	@FXML
	private void initialize() {
		back_button.setOnAction(event -> NavigationUtils.navigateTo(event, "/fxml_files/login_or_register.fxml"));
	}

}
