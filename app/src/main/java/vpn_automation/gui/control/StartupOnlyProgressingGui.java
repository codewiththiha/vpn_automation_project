package vpn_automation.gui.control;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;

public class StartupOnlyProgressingGui {

	@FXML
	private Label adjustable_text;

	@FXML
	private WebView web_view_component;

	@FXML
	private ProgressBar progress_bar;

	public void initialize() {
		if (web_view_component != null) {
			WebEngine engine = web_view_component.getEngine();
			String gifPath = getClass()
					.getResource("/assets/dancing_panda.gif")
					.toExternalForm();
			engine.load(gifPath);
		}

		animateProgressBarTo100InSeconds(20);
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
				adjustable_text.setText("Loading... " + (int) (current / 12) + "%");
			}
		}));

		timeline.setCycleCount((int) totalFrames);
		timeline.play();
	}
}
