package vpn_automation.gui.control;

import java.sql.SQLException;

import javafx.animation.KeyFrame;
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

	public void initialize() throws SQLException, Exception {
		if (web_view_component != null) {
			WebEngine engine = web_view_component.getEngine();
			String gifPath = getClass()
					.getResource("/assets/dancing_panda.gif")
					.toExternalForm();
			engine.load(gifPath);
		}

		animateProgressBarTo100InSeconds(20);
		// main();
		// making appear only after that main
		// Start background task
		Task<Void> backgroundTask = new Task<>() {
			@Override
			protected Void call() throws Exception {
				// Simulate or run actual work here
				String currentDir = "/home/thiha/Developer/vpn_automation/app/src/main/resources/ovpn_files";
				OvpnFileModifier modifier = new OvpnFileModifier();
				OvpnFileTester tester = new OvpnFileTester();

				modifier.modifyOvpnFiles(currentDir);
				tester.testOvpnFiles(currentDir);

				return null;
			}
		};

		// Optional: Update progress label on UI thread
		backgroundTask.setOnRunning(e -> {
			setMessage("Processing OVPN files...");
		});

		backgroundTask.setOnSucceeded(e -> {
			setMessage("Done! Ready to use.");
		});

		// Run the background task in a separate thread
		new Thread(backgroundTask).start();
		// TODO must fix
		// this have issue too it not stop walking eventhough i close the gui
		// i can't set message for the gui (need to change sysprints with gui but not
		// flexible)
		// the tested ones are fixed to id1 must fix that too
	}

	int current = 0;

	private void animateProgressBarTo100InSeconds(int durationSeconds) {
		double totalFrames = durationSeconds * 60; // 60 frames per second
		final double[] currentProgress = { 0.0 };

		Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1000 / 60), e -> {
			if (currentProgress[0] < 1.0) {
				currentProgress[0] += 1.0 / totalFrames;

				current += 1; // current will 1200 since 1 frame = 60 per second and you run 20 second so 1200
				progress_bar.setProgress(currentProgress[0]);
				// adjustable_text.setText("Loading... " + (int) (current / 12) + "%");
			}
		}));

		timeline.setCycleCount((int) totalFrames);
		timeline.play();
	}

	public void setMessage(String message) {
		adjustable_text.setText(message);
	}

	public void main() throws SQLException, Exception {
		String currentDir = "/home/thiha/Developer/vpn_automation/app/src/main/resources/ovpn_files";
		OvpnFileModifier modifier = new OvpnFileModifier();
		OvpnFileTester tester = new OvpnFileTester();

		modifier.modifyOvpnFiles(currentDir);
		tester.testOvpnFiles(currentDir);
	}
}
