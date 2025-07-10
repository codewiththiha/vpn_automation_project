package vpn_automation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import vpn_automation.backend.db.UserDAO;
import vpn_automation.backend.db.VPNConfigDAO;
import vpn_automation.backend.db.WifiProfileDAO;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class RESMain extends Application {

	@Override
	public void start(Stage stage) throws IOException, SQLException {
		WifiProfileDAO.forceResetSearchStatus();
		int activeUserId = UserDAO.getActiveUserId();
		FXMLLoader loader = null;
		if (activeUserId == -1) {
			loader = new FXMLLoader(getClass().getResource("/fxml_files/register_form.fxml"));
		}
		if (activeUserId != -1) {
			List<Integer> wifiProfileIds = WifiProfileDAO.getWifiProfileIds(activeUserId);
			if (wifiProfileIds != null && !wifiProfileIds.isEmpty()) {
				int wifiProfileId = wifiProfileIds.getFirst();

				if (VPNConfigDAO.giveWifiProfileIdGetvpnConfigIds(wifiProfileId).isEmpty()) {
					loader = new FXMLLoader(getClass().getResource("/fxml_files/progressing_startup.fxml"));
				} else {
					loader = new FXMLLoader(getClass().getResource("/fxml_files/mainGui.fxml"));
				}
			} else {
				loader = new FXMLLoader(getClass().getResource("/fxml_files/startup_page.fxml"));
			}
		} else {
			loader = new FXMLLoader(getClass().getResource("/fxml_files/register_form.fxml"));
		}

		if (loader == null) {
			throw new IllegalStateException("Failed to load FXML: loader is null");
		}

		// Load FXML and get root node
		Parent root = loader.load();

		// Wrap root in StackPane for scaling and centering
		StackPane container = new StackPane(root);

		// Apply responsive scaling based on actual FXML size
		setupResponsiveScaling(container, root);

		Scene scene = new Scene(container); // No fixed size — uses natural size
		stage.setScene(scene);
		stage.setTitle("Vpn Automator");
		stage.show();
	}

	/**
	 * Makes the UI scale responsively and stay centered.
	 */
	private void setupResponsiveScaling(StackPane container, Parent content) {
		double initialWidth = content.prefWidth(-1);
		double initialHeight = content.prefHeight(-1);

		// Bind scaling factors to container size divided by initial content size
		content.scaleXProperty().bind(container.widthProperty().divide(initialWidth));
		content.scaleYProperty().bind(container.heightProperty().divide(initialHeight));

		// Center content (StackPane takes care of layout)
		StackPane.setAlignment(content, javafx.geometry.Pos.CENTER);
	}

	public static void main(String[] args) {
		launch(args);
	}
}