package vpn_automation.gui.control;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class WarningDialog {

	@FXML
	private Label error_message_label;

	@FXML
	private Button accept_button;

	@FXML
	private Button cancel_button;

	private Stage dialogStage;

	private boolean isAccepted = false;

	public WarningDialog() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_files/warning_pop_up.fxml"));
			loader.setController(this);

			Scene scene = new Scene(loader.load());
			dialogStage = new Stage();
			dialogStage.setScene(scene);
			dialogStage.setTitle("Warning");
			dialogStage.initModality(Modality.APPLICATION_MODAL);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initialize() {
		accept_button.setOnAction(event -> {
			isAccepted = true;
			dialogStage.close();
		});

		cancel_button.setOnAction(event -> {
			isAccepted = false;
			dialogStage.close();
		});
	}

	public void setWarning(String message, String acceptButtonText) {
		error_message_label.setText(message);
		if (acceptButtonText != null && !acceptButtonText.isEmpty()) {
			accept_button.setText(acceptButtonText);
		}
	}

	public boolean showAndGetResult() {
		if (dialogStage == null)
			return false;

		// Ensure running on JavaFX thread
		if (!Platform.isFxApplicationThread()) {
			final boolean[] result = new boolean[1];
			Platform.runLater(() -> {
				dialogStage.showAndWait();
				result[0] = isAccepted;
			});
			// Wait until dialog closes (not ideal but works for simple use cases)
			while (dialogStage.isShowing()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException ignored) {
				}
			}
			return result[0];
		} else {
			dialogStage.showAndWait();
			return isAccepted;
		}
	}

	public void close() {
		if (dialogStage != null && dialogStage.isShowing()) {
			dialogStage.close();
		}
	}
}