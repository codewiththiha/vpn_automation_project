package vpn_automation.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import vpn_automation.backend.db.UserDAO;
import vpn_automation.backend.db.VPNConfigDAO;
import vpn_automation.backend.db.WifiProfileDAO;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class Main2 extends Application {

	@Override
	public void start(Stage stage) throws IOException, SQLException {
		int activeUserId = UserDAO.getActiveUserId();
		FXMLLoader loader = null;
		// first check if it logged in?
		if (activeUserId != -1) {
			List<Integer> wifiProfileIds = WifiProfileDAO.getWifiProfileIds(activeUserId);
			if (wifiProfileIds.size() > 0) {
				int wifiProfileId = wifiProfileIds.getFirst(); // need to refactor cuz it look dumb
				if (VPNConfigDAO.checkCorrespondingConfigForWifiProfile(wifiProfileId).isEmpty()) {
					// should add a new gui that warn user since no vpn config is found will start
					// searching now
					loader = new FXMLLoader(getClass().getResource("/fxml_files/progressing_startup.fxml"));
				}
			} else if (wifiProfileIds.isEmpty()) { // if wifiprofile id not found that mean you missed this part
				loader = new FXMLLoader(getClass().getResource("/fxml_files/startup_page.fxml"));
			}
		} else if (activeUserId == -1) {
			System.out.println("here");
			loader = new FXMLLoader(getClass().getResource("/fxml_files/register_form.fxml"));
		} else {
			System.out.println("Unknown");
		}
		if (loader == null) {
			// Fallback in case of unexpected state
			throw new IllegalStateException("Failed to load FXML: loader is null");
		}
		Scene scene = new Scene(loader.load());
		stage.setTitle("Vpn Automator");
		stage.setScene(scene);
		stage.show();

	}

	public static void main(String[] args) {
		launch(args);
	}
}