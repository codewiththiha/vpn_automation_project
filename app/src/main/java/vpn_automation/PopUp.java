package vpn_automation;

import javafx.application.Application;
import javafx.stage.Stage;
import vpn_automation.gui.control.NewProfile;

public class PopUp extends Application {

	@Override
	public void start(Stage primaryStage) {
		NewProfile dialog = new NewProfile();
		dialog.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}