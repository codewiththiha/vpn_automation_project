package vpn_automation.gui.control;

import java.sql.SQLException;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import vpn_automation.backend.FileUtils;
import vpn_automation.backend.OvpnFileModifier;
import vpn_automation.backend.OvpnFileTester;
import vpn_automation.backend.VPNManager;
import vpn_automation.backend.db.UserDAO;
import vpn_automation.backend.db.VPNConfigDAO;
import vpn_automation.backend.db.WifiProfileDAO;
import vpn_automation.gui.NavigationUtils;

public class StartupOnlyProgressingGui {

	@FXML
	private Label adjustable_text;

	@FXML
	private WebView web_view_component;

	@FXML
	public Button continue_button;

	private Task<Void> backgroundTask;
	private Thread backgroundThread;

	public void initialize() throws SQLException, Exception {
		continue_button.isDisabled();
		continue_button.setVisible(false);

		Platform.runLater(() -> {
			Stage currentStage = (Stage) continue_button.getScene().getWindow();

			continue_button.setOnAction(e -> {
				WifiProfileDAO.forceResetSearchStatus();
				VPNConfigDAO.SetVpnDisconnect();
				try {
					VPNManager.disconnectVpn();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				VPNConfigDAO.SetVpnDisconnect();
				if (backgroundTask != null && backgroundTask.isRunning()) {
					backgroundTask.cancel();
				}
				NavigationUtils.navigateTo(currentStage, "/fxml_files/mainGui.fxml");
			});

		});

		if (web_view_component != null) {
			WebEngine engine = web_view_component.getEngine();
			String gifPath = getClass().getResource("/assets/loading_third.gif").toExternalForm();
			engine.load(gifPath);
		}

		// animateProgressBarTo100InSeconds(20);

		int limit = FileUtils.getOvpnFiles(UserDAO.getOvpnPath()).size();
		// Create background task
		backgroundTask = new Task<>() {
			@Override
			protected Void call() throws Exception {
				OvpnFileModifier modifier = new OvpnFileModifier();
				OvpnFileTester tester = new OvpnFileTester();
				modifier.modifyOvpnFiles(UserDAO.getOvpnPath(), StartupOnlyProgressingGui.this::updateGuiMessage);
				WifiProfileDAO.SetSearchStatus();
				tester.testOvpnFiles(UserDAO.getOvpnPath(), StartupOnlyProgressingGui.this::updateGuiMessage, limit,
						StartupOnlyProgressingGui.this);

				return null;
			}
		};

		// backgroundTask.setOnRunning(e -> updateGuiMessage("Processing OVPN
		// files..."));
		// backgroundTask.setOnSucceeded(e -> updateGuiMessage("Done! Ready to use."));
		backgroundTask.setOnFailed(e -> {
			updateGuiMessage("⚠ Error occurred during processing.");
			backgroundTask.getException().printStackTrace();
		});

		backgroundThread = new Thread(backgroundTask);
		// close with the gui
		backgroundThread.setDaemon(true);
		backgroundThread.start();
	}

	public void updateGuiMessage(String message) {
		Platform.runLater(() -> adjustable_text.setText(message));
	}

	int current = 0;

	// private void animateProgressBarTo100InSeconds(int durationSeconds) {
	// double totalFrames = durationSeconds * 60; // 60 frames per second
	// final double[] currentProgress = { 0.0 };

	// Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1000 / 60), e
	// -> {
	// if (currentProgress[0] < 1.0) {
	// currentProgress[0] += 1.0 / totalFrames;
	// current += 1;
	// progress_bar.setProgress(currentProgress[0]);
	// }
	// }));

	// timeline.setCycleCount((int) totalFrames);
	// timeline.play();
	// }

	// public void main() throws SQLException, Exception {
	// String currentDir =
	// "/home/thiha/Developer/vpn_automation/app/src/main/resources/ovpn_files";
	// OvpnFileModifier modifier = new OvpnFileModifier();
	// OvpnFileTester tester = new OvpnFileTester();

	// modifier.modifyOvpnFiles(currentDir, this::updateGuiMessage);
	// tester.testOvpnFiles(currentDir, this::updateGuiMessage);
	// }
}
