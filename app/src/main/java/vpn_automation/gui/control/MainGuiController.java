package vpn_automation.gui.control;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import vpn_automation.backend.CountryCodeConverter;
import vpn_automation.backend.FileUtils;
import vpn_automation.backend.OvpnFileModifier;
import vpn_automation.backend.OvpnFileTester;
import vpn_automation.backend.VPNManager;
import vpn_automation.backend.db.UserDAO;
import vpn_automation.backend.db.VPNConfigDAO;
import vpn_automation.backend.db.WifiProfileDAO;

public class MainGuiController {
	private Task<Void> backgroundTask;
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

		String currentDir = "/home/thiha/Developer/vpn_automation/app/src/main/resources/ovpn_files";
		Refresh();
		Refresh2();

		try {
			raw_ovpn_slider.setMax((double) FileUtils.getOvpnFiles(currentDir).size());
		} catch (Exception e) {
			e.printStackTrace();
		}
		raw_ovpn_slider.valueProperty().addListener((obs, oldValue, newValue) -> {
			raw_ovpn_amount_label.setText(String.valueOf(newValue.intValue()));
		});

		connect_status_label.setText("");
		vpn_profile_combo_box.valueProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null) {
				System.out.println("User selected: " + newValue);
				WifiProfileDAO.setSelectedWifiNameActive(newValue);
				try {
					Refresh();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

		connect_button.setOnAction(event -> {

			if (VPNConfigDAO.GetConnectedIpAddress() == null) {
				if (backgroundTask != null && backgroundTask.isRunning()) {
					VPNManager.disconnectVpn(this::UpdateConnectStatus);
					VPNConfigDAO.SetVpnDisconnect();
					backgroundTask.cancel();
				}

				backgroundTask = new Task<>() {
					@Override
					protected Void call() throws Exception {
						VPNConnect();
						return null;
					}
				};

				Thread backgroundThread = new Thread(backgroundTask);
				backgroundThread.setDaemon(true);
				backgroundThread.start();
			}
			;
			if (!(VPNConfigDAO.GetConnectedIpAddress() == null)) {
				Refresh2();
				connect_button.setText("Connect");
				try {
					VPNManager.disconnectVpn(this::UpdateConnectStatus);
					VPNConfigDAO.SetVpnDisconnect();
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (backgroundTask != null && backgroundTask.isRunning()) {
					backgroundTask.cancel();
				}
			}
			;
		});

		Platform.runLater(() -> {
			Stage stage = (Stage) connect_button.getScene().getWindow();
			stage.setOnCloseRequest(event -> {
				System.out.println("Window is closing. Cleaning up...");

				try {
					VPNManager.disconnectVpn(this::UpdateConnectStatus);
					VPNConfigDAO.SetVpnDisconnect();
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (backgroundTask != null && backgroundTask.isRunning()) {
					backgroundTask.cancel();
				}
			});

		});

		search_ovpn_button.setOnAction(event -> {
			// Warrning box of disconnecting Vpn should show and will continue if user
			// confirm -- currently for test purpose
			// limit function isn't set yet

			if (backgroundTask != null && backgroundTask.isRunning()) {
				System.out.println("Background task stopped");
				VPNManager.disconnectVpn(this::UpdateConnectStatus);
				VPNConfigDAO.SetVpnDisconnect();
				backgroundTask.cancel();
			}

			backgroundTask = new Task<>() {
				@Override
				protected Void call() throws Exception {
					SearchOvpn(currentDir);
					return null;
				}
			};

			Thread backgroundThread = new Thread(backgroundTask);
			backgroundThread.setDaemon(true);
			backgroundThread.start();
		});

	}

	public void Refresh() throws SQLException {
		System.out.println("Here");
		int userId = UserDAO.getActiveUserId();
		int activeWifiProfileId = WifiProfileDAO.getActiveWifiProfileId();
		VPNConfigDAO.refreshAndGenerateEncodedCountries(activeWifiProfileId);
		List<String> vpnCountries = VPNConfigDAO.getEncodedCountries(activeWifiProfileId);
		List<String> wifiNames = WifiProfileDAO.getWifiNames(userId);
		Collections.sort(wifiNames);
		Collections.sort(vpnCountries);
		ObservableList<String> observableVpnCountries = FXCollections.observableArrayList(vpnCountries);
		ObservableList<String> observableWifiNames = FXCollections.observableArrayList(wifiNames);
		vpn_profile_combo_box.setItems(observableWifiNames);
		vpn_profile_combo_box.setValue(WifiProfileDAO.getActiveWifiProfileName());
		config_combo_box.setItems(observableVpnCountries);
		active_wifi_profile_label.setText(WifiProfileDAO.getActiveWifiProfileName());

	}

	public void VPNConnect() throws IOException {
		VPNManager.disconnectVpn(this::UpdateConnectStatus);
		VPNConfigDAO.SetVpnDisconnect();
		int activeWifiProfileId = WifiProfileDAO.getActiveWifiProfileId();
		String encodedName = config_combo_box.getValue();
		String OvpnPath = VPNConfigDAO.getOvpnConnection(activeWifiProfileId, encodedName);
		VPNManager.connectVpn(OvpnPath, this::UpdateConnectStatus, this::UpdateCurrentLocation,
				this::UpdateCurrentIpAddress, activeWifiProfileId, encodedName, this::UpdateConnectButton); // return
																											// true
																											// logic
																											// failed

		// VPNConfigDAO.SetConnection(activeWifiProfileId, encodedName);

		// // Wrap UI updates in Platform.runLater
		// Platform.runLater(() -> {
		// current_location_label.setText("Current Location: " +
		// CountryCodeConverter.getCountryName(
		// VPNConfigDAO.GetConnectedCountry()));
		// current_ip_label.setText("Ip: " + VPNConfigDAO.GetConnectedIpAddress());
		// });
	}

	public void UpdateConnectStatus(String message) {
		Platform.runLater(() -> connect_status_label.setText(message));
	}

	public void UpdateCurrentLocation(String message) {
		Platform.runLater(() -> current_location_label.setText(message));
	}

	public void UpdateCurrentIpAddress(String message) {
		Platform.runLater(() -> current_ip_label.setText(message));
	}

	public void UpdateConnectButton(String message) {
		Platform.runLater(() -> connect_button.setText(message));
	}

	public void Refresh2() {
		current_location_label.setText(
				"Current Location: " + CountryCodeConverter.getCountryName(WifiProfileDAO.GetCurrentCountry()));
		current_ip_label.setText("Ip: " + WifiProfileDAO.GetCurrentIpAddress());
	}

	public void UpdateSearchStatus(String message) {
		Platform.runLater(() -> main_status_label.setText(message));
	}

	public void SearchOvpn(String currentDir) throws SQLException, Exception {

		OvpnFileModifier modifier = new OvpnFileModifier();
		OvpnFileTester tester = new OvpnFileTester();

		modifier.modifyOvpnFiles(currentDir, this::UpdateSearchStatus);
		tester.testOvpnFiles(currentDir, this::UpdateSearchStatus);
	}
}
