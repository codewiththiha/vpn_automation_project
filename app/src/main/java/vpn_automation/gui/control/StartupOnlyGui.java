package vpn_automation.gui.control;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class StartupOnlyGui {

	@FXML
	private Label adjustable_label;

	@FXML
	private TextField brand_name_field;

	@FXML
	private Button submit_button;

	@FXML
	private TextField wifi_name_field;

	public void wifiNameAndISPCollector() {
		String brandName = brand_name_field.getText();
		String wifiName = wifi_name_field.getText();
	}

}
