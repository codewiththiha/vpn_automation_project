package vpn_automation.gui.control;

import java.sql.SQLException;
import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import vpn_automation.backend.OvpnFileModifier;
import vpn_automation.backend.OvpnFileTester;

public class StartupOnlyProgressingGui {

	@FXML
	private Label adjustable_text;

	@FXML
	private WebView web_view_component;

	@FXML
	private ProgressBar progress_bar;

	private Task<Void> backgroundTask;
	private Thread backgroundThread;

	public void initialize() throws SQLException, Exception {
		if (web_view_component != null) {
			WebEngine engine = web_view_component.getEngine();
			String gifPath = getClass().getResource("/assets/dancing_panda.gif").toExternalForm();
			engine.load(gifPath);
		}

		animateProgressBarTo100InSeconds(20);

		// Create background task
		backgroundTask = new Task<>() {
			@Override
			protected Void call() throws Exception {
				String currentDir = "/home/thiha/Developer/vpn_automation/app/src/main/resources/ovpn_files";
				OvpnFileModifier modifier = new OvpnFileModifier();
				OvpnFileTester tester = new OvpnFileTester();

				modifier.modifyOvpnFiles(currentDir, StartupOnlyProgressingGui.this::updateGuiMessage);
				tester.testOvpnFiles(currentDir, StartupOnlyProgressingGui.this::updateGuiMessage);

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

	private void animateProgressBarTo100InSeconds(int durationSeconds) {
		double totalFrames = durationSeconds * 60; // 60 frames per second
		final double[] currentProgress = { 0.0 };

		Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1000 / 60), e -> {
			if (currentProgress[0] < 1.0) {
				currentProgress[0] += 1.0 / totalFrames;
				current += 1;
				progress_bar.setProgress(currentProgress[0]);
			}
		}));

		timeline.setCycleCount((int) totalFrames);
		timeline.play();
	}

	public void main() throws SQLException, Exception {
		String currentDir = "/home/thiha/Developer/vpn_automation/app/src/main/resources/ovpn_files";
		OvpnFileModifier modifier = new OvpnFileModifier();
		OvpnFileTester tester = new OvpnFileTester();

		modifier.modifyOvpnFiles(currentDir, this::updateGuiMessage);
		tester.testOvpnFiles(currentDir, this::updateGuiMessage);
	}
}