package vpn_automation.gui.control;

import java.sql.SQLException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import vpn_automation.backend.db.UserDAO;
import vpn_automation.backend.db.VPNConfigDAO;
import vpn_automation.backend.db.WifiProfileDAO;
import vpn_automation.gui.NavigationUtils;
import vpn_automation.gui.RESMain;

public class LoginController {

	@FXML
	private Button back_button;

	@FXML
	private TextField email_field;

	@FXML
	private PasswordField password_field;

	@FXML
	private Button submit_button;

	String email, password;

	@FXML
	private void initialize() {
		back_button.setOnAction(event -> NavigationUtils.navigateTo(event, "/fxml_files/login_or_register.fxml"));
		submit_button.setOnAction(event -> {
			try {
				loginCheck();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public void loginCheck() throws SQLException {
		WifiProfileDAO.resetActiveProfile();
		email = email_field.getText();
		password = password_field.getText();
		Stage currentStage = (Stage) submit_button.getScene().getWindow();
		int userID = UserDAO.checkLoginCred(email, password);
		System.out.println(email + " " + password);
		System.out.println(userID + "This is user id");
		if (userID != -1) {
			UserDAO.setLoginUser(userID);
			List<Integer> wifiProfileIds = WifiProfileDAO.getWifiProfileIds(userID);
			if (wifiProfileIds != null && !wifiProfileIds.isEmpty()) {
				int wifiProfileId = wifiProfileIds.getFirst();

				if (VPNConfigDAO.giveWifiProfileIdGetvpnConfigIds(wifiProfileId).isEmpty()) {
					NavigationUtils.navigateTo(currentStage, "/fxml_files/progressing_startup.fxml");
				} else {
					WifiProfileDAO.setSelectedWifiNameActive(userID);
					NavigationUtils.navigateTo(currentStage, "/fxml_files/mainGui.fxml");
				}
			} else {
				NavigationUtils.navigateTo(currentStage, "/fxml_files/startup_page.fxml");
			}
		}

	}
}
