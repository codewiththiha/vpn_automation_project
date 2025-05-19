package vpn_automation.gui.control;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ErrorDialog {
	@FXML
	private Stage dialogStage;

	@FXML
	private Label error_message_label;

	public ErrorDialog() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_files/pop_up.fxml"));
			loader.setController(this); // explicitly set controller (useful when loader itself is controller)

			Scene scene = new Scene(loader.load());
			dialogStage = new Stage();
			dialogStage.setScene(scene);
			dialogStage.setTitle("Error");
			dialogStage.initModality(Modality.APPLICATION_MODAL); // make it able to stack over others
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void setErrorMessage(String message) {
		if (error_message_label != null) {
			error_message_label.setText(message);
		}
	}

	@FXML
	private void onCloseButtonClick() {
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