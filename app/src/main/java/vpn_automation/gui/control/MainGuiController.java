package vpn_automation.gui.control;

import java.io.IOException;
import java.net.URL;
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
import javafx.scene.media.MediaView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

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

	@FXML
	private Button recheck_button;

	@FXML
	private MediaView media_view;
	private MediaPlayer mediaPlayer;

	public void initialize() throws Exception {

		RefreshMain();
		Refresh();
		Refresh2();
		RefreshVpns();

		String currentDir = "/home/thiha/Developer/vpn_automation/app/src/main/resources/ovpn_files";

		MediaPlayerTest(false);
		OvpnFileTester.fixUnknownOvpns();
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
				if (newValue.equals("Add new profile")) {
					NewProfile dialog = new NewProfile();
					dialog.initialize(this);
					dialog.show();
				} else {
					WifiProfileDAO.setSelectedWifiNameActive(newValue);
					RefreshVpns(); // todo check errors
				}

				// try {
				// Refresh();
				// } catch (SQLException e) {
				// todo Auto-generated catch block
				// e.printStackTrace();
				// }

			}
		});

		recheck_button.setOnAction(event -> {
			WarningDialog warning_dialog = new WarningDialog();
			System.out.println("Current Recheck Status: " + WifiProfileDAO.GetSearchStatus());
			// System.out.println(warning_dialog.showAndGetResult());

			if (WifiProfileDAO.GetSearchStatus() == 1) {
				ErrorDialog dialog = new ErrorDialog();
				dialog.setErrorMessage("You are Currently On search");
				dialog.show();
			}

			else if (WifiProfileDAO.GetSearchStatus() == 0) {
				if (VPNConfigDAO.GetConnectedIpAddress() != null) {
					warning_dialog.setWarning("You are Vpn will be disconnected!", "Continue");
					if (!warning_dialog.showAndGetResult()) {
						return;
					} else { // todo fix duplicated
						if (backgroundTask != null && backgroundTask.isRunning()) {
							backgroundTask.cancel();
						}

						recheck_button.setText("Cancel");
						connect_button.setText("Connect");
						MediaPlayerTest(true);
						backgroundTask = new Task<>() {
							@Override
							protected Void call() throws Exception {
								RecheckAction();
								return null;
							}
						};
						VPNManager.cancelRecheckCanceler();
						Thread backgroundThread = new Thread(backgroundTask);
						backgroundThread.setDaemon(true);
						backgroundThread.start();
					}
				} else {
					if (backgroundTask != null && backgroundTask.isRunning()) {
						backgroundTask.cancel();
					}
					// set status

					recheck_button.setText("Cancel");
					connect_button.setText("Connect");
					MediaPlayerTest(true);
					backgroundTask = new Task<>() {
						@Override
						protected Void call() throws Exception {
							RecheckAction();
							return null;
						}
					};
					VPNManager.cancelRecheckCanceler();
					Thread backgroundThread = new Thread(backgroundTask);
					backgroundThread.setDaemon(true);
					backgroundThread.start();
				}
			}

			else if (WifiProfileDAO.GetSearchStatus() == 2) {
				if (backgroundTask != null && backgroundTask.isRunning()) {
					backgroundTask.cancel();
				}
				WifiProfileDAO.ResetSearchStatus(); // reapply 0
				connect_status_label.setText("Recheck Cancelled now");
				recheck_button.setText("Recheck");
				System.out.println("Recheck canceled now");
				OvpnFileTester.cancelOvpnProcess();
				VPNManager.recheckCanceller();
				MediaPlayerTest(false);

			}

			// check if vpn is connected or not
			// todo add warning
			// else {
			// if (backgroundTask != null && backgroundTask.isRunning()) {
			// backgroundTask.cancel();
			// VPNManager.disconnectVpn(this::UpdateConnectStatus);
			// VPNConfigDAO.SetVpnDisconnect();

			// warning_dialog.setWarning("This will disconnect your vpn!", "continue");

			// if (warning_dialog.showAndGetResult()) {
			// System.out.println("continued");
			// }
			// }
			// }

		});

		connect_button.setOnAction(event ->

		{
			System.out.println("Here_____________" + VPNConfigDAO.GetConnectedIpAddress());
			if (VPNConfigDAO.GetConnectedIpAddress() == null) {
				System.out.println("Here0008");
				if (backgroundTask != null && backgroundTask.isRunning()) {
					VPNManager.disconnectVpn(this::UpdateConnectStatus);
					VPNConfigDAO.SetVpnDisconnect();
					backgroundTask.cancel();
					return;
				}
				MediaPlayerTest(true);
				backgroundTask = new Task<>() {

					@Override
					protected Void call() throws Exception {
						VPNConnect();
						return null;
					}
				};

				Thread backgroundThread = new Thread(
						backgroundTask);
				backgroundThread.setDaemon(true);
				backgroundThread.start();
			}

			else if (!(VPNConfigDAO.GetConnectedIpAddress() == null)) {
				System.out.println("Here0075");
				connect_button.setText("Connect");

				// Cancel background task first
				if (backgroundTask != null) {
					backgroundTask.cancel(true); // true = interrupt thread
				}

				try {
					VPNManager.disconnectVpn();
					VPNConfigDAO.SetVpnDisconnect();
					MediaPlayerTest(false);
					connect_status_label.setText("Disconnected");
					RefreshVpns();
					Refresh2();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			;
		});

		Platform.runLater(() -> {
			Stage stage = (Stage) connect_button.getScene()
					.getWindow();
			stage.setOnCloseRequest(event -> {
				System.out.println("Window is closing. Cleaning up...");
				WifiProfileDAO.ResetSearchStatus();
				VPNConfigDAO.SetVpnDisconnect();
				try {
					VPNManager.disconnectVpn(this::UpdateConnectStatus);
				} catch (Exception e) {
					e.printStackTrace();
				}
				VPNConfigDAO.SetVpnDisconnect();
				if (backgroundTask != null && backgroundTask.isRunning()) {
					backgroundTask.cancel();
				}
			});

		});

		search_ovpn_button.setOnAction(event -> {
			// Warrning box of disconnecting Vpn should show and will continue if user
			// confirm -- currently for test purpose
			// limit function
			int limit = (int) raw_ovpn_slider.getValue();
			if (WifiProfileDAO.GetSearchStatus() == 0) {
				WifiProfileDAO.SetSearchStatus();
				search_ovpn_button.setText("Cancel");
				MediaPlayerTest(true);
				if (backgroundTask != null && backgroundTask.isRunning()) {
					System.out.println("Background task stopped");

					// TODO should show a pop up
					VPNManager.disconnectVpn();
					VPNConfigDAO.SetVpnDisconnect();
					backgroundTask.cancel();
					return;
				}

				backgroundTask = new Task<>() {
					@Override
					protected Void call() throws Exception {
						SearchOvpn(currentDir, limit);
						return null;
					}
				};

				Thread backgroundThread = new Thread(backgroundTask);
				backgroundThread.setDaemon(true);
				backgroundThread.start();
			} // should ask user if no ovpn configuration is found yet
			else if (WifiProfileDAO.GetSearchStatus() == 1) {
				WifiProfileDAO.ResetSearchStatus();
				System.out.println("Stopped search");

				MediaPlayerTest(false);
				if (backgroundTask != null && backgroundTask.isRunning()) {
					backgroundTask.cancel();
				}
				search_ovpn_button.setText("Search");
				return;
			}

			backgroundTask.setOnSucceeded(e -> {
				WifiProfileDAO.ResetSearchStatus();
				search_ovpn_button.setText("Search");
				MediaPlayerTest(false);
			});

		});

	}

	public void Refresh() throws SQLException {
		System.out.println("Here");
		int userId = UserDAO.getActiveUserId();
		List<String> wifiNames = WifiProfileDAO.getWifiNames(userId);
		Collections.sort(wifiNames);
		ObservableList<String> observableWifiNames = FXCollections.observableArrayList(wifiNames);
		Platform.runLater(() -> {
			// todo spacious of errors
			vpn_profile_combo_box.setItems(observableWifiNames);
			vpn_profile_combo_box.setValue(WifiProfileDAO.getActiveWifiProfileName());
			vpn_profile_combo_box.getItems().add("Add new profile");
			active_wifi_profile_label.setText(WifiProfileDAO.getActiveWifiProfileName());
		});

	}

	public void RefreshVpns() {
		// connect_status_label.setText("Refreshing Vpn List");
		System.out.println("Refreshing Vpn list");
		int activeWifiProfileId = WifiProfileDAO.getActiveWifiProfileId();
		List<String> vpnCountries = VPNConfigDAO.getEncodedCountries(activeWifiProfileId);
		// todo navigate to tab2
		if (!vpnCountries.isEmpty()) {
			Platform.runLater(() -> {
				Collections.sort(vpnCountries);
				ObservableList<String> observableVpnCountries = FXCollections.observableArrayList(vpnCountries);
				config_combo_box.setItems(observableVpnCountries);
				config_combo_box.setValue(vpnCountries.getFirst());
			});
		} else {
			vpnCountries.add("Empty");
			ObservableList<String> observableVpnCountries = FXCollections.observableArrayList(vpnCountries);
			System.out.println("RefreshVPns occurs error");
			config_combo_box.setItems(observableVpnCountries);
			config_combo_box.setValue("Empty");
		}

	}

	public void RefreshMain() {
		// int activeWifiProfileId = WifiProfileDAO.getActiveWifiProfileId();
		VPNConfigDAO.refreshAndGenerateEncodedCountries();
	}

	public void VPNConnect() throws IOException {
		// VPNManager.disconnectVpn(this::UpdateConnectStatus);
		// VPNConfigDAO.SetVpnDisconnect();
		int activeWifiProfileId = WifiProfileDAO.getActiveWifiProfileId();
		String encodedName = config_combo_box.getValue();
		String OvpnPath = VPNConfigDAO.getOvpnConnection(activeWifiProfileId, encodedName);
		VPNManager.connectVpn(OvpnPath, this::UpdateConnectStatus, this::UpdateCurrentLocation,
				this::UpdateCurrentIpAddress, activeWifiProfileId, encodedName, this::UpdateConnectButton, this); // return
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
		Platform.runLater(() -> {
			current_location_label.setText(
					"Current Location: " + CountryCodeConverter.getCountryName(WifiProfileDAO.GetCurrentCountry()));
			current_ip_label.setText("Ip: " + WifiProfileDAO.GetCurrentIpAddress());
		});
	}

	public void UpdateSearchStatus(String message) {
		Platform.runLater(() -> main_status_label.setText(message));
	}

	public void SearchOvpn(String currentDir, int Limit) throws SQLException, Exception {
		OvpnFileModifier modifier = new OvpnFileModifier();
		OvpnFileTester tester = new OvpnFileTester();

		modifier.modifyOvpnFiles(currentDir, this::UpdateSearchStatus);
		tester.testOvpnFiles(currentDir, this::UpdateSearchStatus, Limit, this);
		OvpnFileTester.fixUnknownOvpns();
	}

	public void RecheckAction() throws SQLException, Exception {
		System.out.println(WifiProfileDAO.GetSearchStatus());
		System.out.println("In the Recheck session");
		VPNConfigDAO.SetVpnDisconnect();
		WifiProfileDAO.SetRecheckStatus();
		VPNManager.recheckTheOvpns(this, this::UpdateConnectStatus, this::UpdateConnectButton);
	}

	public void MediaPlayerTest(boolean loadStatus) {
		String countryCode = VPNConfigDAO.GetConnectedCountry();
		String videoPath = null;

		if (loadStatus) {
			countryCode = "LOAD";

		}
		if (countryCode == null) {
			countryCode = WifiProfileDAO.GetCurrentCountry();
		}
		if (countryCode != null) {
			videoPath = CountryCodeConverter.getCountryVideo(countryCode);
		}
		URL resource = getClass().getResource(videoPath);
		if (resource == null) {
			System.out.println("Video file not found!");
			return;
		}

		Media media = new Media(resource.toString());
		mediaPlayer = new MediaPlayer(media);
		media_view.setMediaPlayer(mediaPlayer);
		if (loadStatus) {
			mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
		}
		// Start playing
		mediaPlayer.play();
	}
}
