package vpn_automation;

import javafx.application.Application;
import javafx.stage.Stage;
import vpn_automation.gui.control.ErrorDialog;

public class PopUp extends Application {

	@Override
	public void start(Stage primaryStage) {
		ErrorDialog dialog = new ErrorDialog();
		dialog.setErrorMessage("Something went wrong!");
		dialog.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}