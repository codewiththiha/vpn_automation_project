package vpn_automation.gui.control;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vpn_automation.backend.db.UserDAO;
import vpn_automation.backend.db.VPNConfigDAO;
import vpn_automation.backend.db.WifiProfileDAO;

public class ExportUIController {
	@FXML
	private Stage dialogStage;

	@FXML
	private Button cancel_button;

	@FXML
	private Button export_button;

	@FXML
	private VBox checkBoxContainer;

	List<String> ovpnFiles = VPNConfigDAO.getActiveProfileOvpnPaths(WifiProfileDAO.getActiveWifiProfileId());

	public void initialize() {
		cancel_button.setOnAction(e -> {
			dialogStage.close();
		});

		export_button.setOnAction(e -> {
			try {
				handleExport();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		});
	}

	public ExportUIController() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_files/export_gui.fxml"));
			loader.setController(this); // Set the controller for the FXML
			Scene scene = new Scene(loader.load());
			dialogStage = new Stage();
			dialogStage.setScene(scene);
			dialogStage.setTitle("Export Ovpns");
			dialogStage.initModality(Modality.APPLICATION_MODAL); // make it able to stack over others
			initializeData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initializeData() {
		for (String ovpnfile : ovpnFiles) {
			CheckBox cb = new CheckBox(VPNConfigDAO.GetEncodedCountriesByPath(ovpnfile));
			cb.setUserData(ovpnfile);
			checkBoxContainer.getChildren().add(cb);
		}
	}

	public void copyAndRenameOvpn(String sourcePath, String newName) throws IOException, SQLException {
		Path source = Paths.get(sourcePath);
		Path target = Paths.get(UserDAO.getExportPath(), newName);
		Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
	}

	private void handleExport() throws SQLException {
		for (Node node : checkBoxContainer.getChildren()) {
			if (node instanceof CheckBox cb && cb.isSelected()) {
				String ovpn = (String) cb.getUserData();
				try {
					String newName = VPNConfigDAO.GetEncodedCountriesByPath(ovpn) + "_"
							+ WifiProfileDAO.getActiveWifiProfileName() + "_" + WifiProfileDAO.getActiveWifiBrandName();
					copyAndRenameOvpn(ovpn, newName + ".ovpn");
					System.out.println("Copied: " + VPNConfigDAO.GetEncodedCountriesByPath(ovpn));
				} catch (IOException ex) {
					showAlert("Error", "Failed to copy file: " + VPNConfigDAO.GetEncodedCountriesByPath(ovpn) + "\n"
							+ ex.getMessage());
				}
			}
		}
		dialogStage.close();
	}

	@FXML
	private void onCloseButtonClick() throws SQLException {
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

	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
