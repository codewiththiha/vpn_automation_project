package vpn_automation.gui.control;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ChangeName {
	@FXML
	private Stage dialogStage;

	@FXML
	private TextField change_field;

	public ChangeName() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_files/new_name.fxml"));
			loader.setController(this); // Set the controller for the FXML
			Scene scene = new Scene(loader.load());
			dialogStage = new Stage();
			dialogStage.setScene(scene);
			dialogStage.setTitle("Create New Profile");
			dialogStage.initModality(Modality.APPLICATION_MODAL); // make it able to stack over others
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void show() {
		if (dialogStage != null)
			dialogStage.show();
	}
}
