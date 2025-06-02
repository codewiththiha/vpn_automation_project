package vpn_automation.gui.control;

import java.io.IOException;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vpn_automation.backend.IPInfoFetcher;
import vpn_automation.backend.db.UserDAO;
import vpn_automation.backend.db.WifiProfileDAO;

public class NewProfile {

	@FXML
	private TextField brand_name_field;

	@FXML
	private Button submit_button;

	@FXML
	private TextField wifi_name_field;

	@FXML
	private Stage dialogStage;

	public NewProfile() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_files/new_profile.fxml"));
			loader.setController(this); // explicitly set controller (useful when loader itself is controller)

			Scene scene = new Scene(loader.load());
			dialogStage = new Stage();
			dialogStage.setScene(scene);
			dialogStage.setTitle("Create New Profile");
			dialogStage.initModality(Modality.APPLICATION_MODAL); // make it able to stack over others
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initialize() {
		submit_button.setOnAction(event -> {
			try {
				WifiProfileCreator();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});
	}

	public void WifiProfileCreator() throws Exception {
		ErrorDialog dialog = new ErrorDialog();
		String wifiBrand = brand_name_field.getText();
		String wifiName = wifi_name_field.getText();
		int userID = UserDAO.getActiveUserId();
		String currentIPAddress = IPInfoFetcher.getIPAddress();
		String currentLocation = IPInfoFetcher.getCountry();
		if (wifiBrand.isEmpty() || wifiBrand.isEmpty()) {
			dialog.setErrorMessage("Please fill the Fields first");
			dialog.show();
		} else {
			WifiProfileDAO.makeWifiProfileForLoggedInUser(userID, wifiBrand.toLowerCase(), currentIPAddress,
					currentLocation,
					wifiName);
			onCloseButtonClick(null);
		}
	}

	@FXML
	private void onCloseButtonClick(MainGuiController guiController) throws SQLException {
		guiController.Refresh();
		dialogStage.close();
	}

	@FXML
	public void showAndWait() {
		if (dialogStage != null)
			dialogStage.showAndWait();
	}

	@FXML
	public void show() {
		if (dialogStage != null)
			dialogStage.show();
	}

}
