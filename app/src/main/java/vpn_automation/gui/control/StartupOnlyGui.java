package vpn_automation.gui.control;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import vpn_automation.backend.IPInfoFetcher;
import vpn_automation.backend.db.UserDAO;
import vpn_automation.backend.db.VPNConfigDAO;
import vpn_automation.backend.db.WifiProfileDAO;
import vpn_automation.gui.NavigationUtils;

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
				wifiProfileCreator();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	public void wifiProfileCreator() throws Exception {
		ErrorDialog dialog = new ErrorDialog();
		String wifiBrand = brand_name_field.getText();
		String wifiName = wifi_name_field.getText();
		int userID = UserDAO.getActiveUserId();
		String currentIPAddress = IPInfoFetcher.getIPAddress();
		String currentLocation = IPInfoFetcher.getCountry();
		if (wifiBrand.isEmpty() || wifiBrand.isEmpty()) {
			dialog.setErrorMessage("Please fill the Fields first");
		} else {
			WifiProfileDAO.makeWifiProfileForLoggedInUser(userID, wifiBrand.toLowerCase(), currentIPAddress,
					currentLocation,
					wifiName);
			List<Integer> wifiProfileIds = WifiProfileDAO.getWifiProfileIds(userID);
			if (wifiProfileIds.size() == 1) {
				int wifiProfileId = wifiProfileIds.getFirst(); // need to refactor cuz it look dumb
				if (VPNConfigDAO.giveWifiProfileIdGetvpnConfigIds(wifiProfileId).isEmpty()) {
					// Navigate to the startup page
					Stage currentStage = (Stage) submit_button.getScene().getWindow();
					NavigationUtils.navigateTo(currentStage, "/fxml_files/progressing_startup.fxml");
				}
			}
		}

	}

}
