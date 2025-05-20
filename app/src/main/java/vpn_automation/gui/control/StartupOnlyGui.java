package vpn_automation.gui.control;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import vpn_automation.backend.IPInfoFetcher;
import vpn_automation.backend.db.UserDAO;
import vpn_automation.backend.db.VPNProfileDAO;

public class StartupOnlyGui {

	@FXML
	private Label adjustable_label;

	@FXML
	private TextField brand_name_field;

	@FXML
	private Button submit_button;

	@FXML
	private TextField wifi_name_field;

	@FXML
	private void initialize() {
		submit_button.setOnAction(event -> {
			try {
				wifiNameAndISPCollector();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	public void wifiNameAndISPCollector() throws Exception {
		String wifiBrand = brand_name_field.getText();
		String wifiName = wifi_name_field.getText();
		int userID = UserDAO.getActiveUserId();
		String currentIPAddress = IPInfoFetcher.getIPAddress();
		String currentLocation = IPInfoFetcher.getCountry();

		VPNProfileDAO.makeWifiProfileForLoggedInUser(userID, wifiBrand, currentIPAddress, currentLocation, wifiName);
	}

}
