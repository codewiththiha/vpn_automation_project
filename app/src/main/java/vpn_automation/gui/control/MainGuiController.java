package vpn_automation.gui.control;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import vpn_automation.backend.db.UserDAO;
import vpn_automation.backend.db.VPNConfigDAO;
import vpn_automation.backend.db.WifiProfileDAO;

public class MainGuiController {

	@FXML
	private Label active_wifi_profile_label;

	@FXML
	private Button change_password_button;

	@FXML
	private ComboBox<String> config_combo_box;

	@FXML
	private Button connect_button;

	@FXML
	private Label connect_status_label;

	@FXML
	private Label current_ip_label;

	@FXML
	private Label current_location_label;

	@FXML
	private Button edit_user_name_button;

	@FXML
	private Button log_out_button;

	@FXML
	private Label logged_in_user_label;

	@FXML
	private Label main_status_label;

	@FXML
	private Label raw_ovpn_amount_label;

	@FXML
	private Slider raw_ovpn_slider;

	@FXML
	private Button search_ovpn_button;

	@FXML
	private ComboBox<String> vpn_profile_combo_box;

	public void initialize() throws SQLException {
		refresh();
		vpn_profile_combo_box.valueProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null) {
				System.out.println("User selected: " + newValue);
				WifiProfileDAO.setSelectedWifiNameActive(newValue);
				try {
					refresh();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
	}

	public void refresh() throws SQLException {
		System.out.println("Here");
		int userId = UserDAO.getActiveUserId();
		int activeWifiProfileId = WifiProfileDAO.getActiveWifiProfileId();
		List<String> vpnCountries = VPNConfigDAO.getEncodedCountries(activeWifiProfileId);
		List<String> wifiNames = WifiProfileDAO.getWifiNames(userId);
		Collections.sort(wifiNames);
		Collections.sort(vpnCountries);
		ObservableList<String> observableVpnCountries = FXCollections.observableArrayList(vpnCountries);
		ObservableList<String> observableWifiNames = FXCollections.observableArrayList(wifiNames);
		vpn_profile_combo_box.setItems(observableWifiNames);
		vpn_profile_combo_box.setValue(WifiProfileDAO.getActiveWifiProfileName());
		config_combo_box.setItems(observableVpnCountries);
	}
}
