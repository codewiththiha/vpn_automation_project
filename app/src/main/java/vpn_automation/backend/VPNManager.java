package vpn_automation.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import javafx.application.Platform;
import vpn_automation.backend.db.VPNConfigDAO;
import vpn_automation.backend.db.WifiProfileDAO;
import vpn_automation.gui.control.MainGuiController;

public class VPNManager {

	private static volatile Process currentVpnProcess = null;
	static List<String> errorKeywords = Arrays.asList("No route to host",
			"TLS key negotiation failed to occur within 60 seconds", "failed: Connection timed out",
			"Connection refused");

	public static void connectVpn(String ovpnPath, Consumer<String> ConnectStatusGui,
			Consumer<String> LocationStatusGui,
			Consumer<String> IpStatusGui, int activeWifiProfileId, String encodedName,
			Consumer<String> ConnectButtonGui,
			MainGuiController guiController) throws IOException {
		if (ovpnPath == null || ovpnPath.isEmpty()) {
			ConnectStatusGui.accept("Error: OVPN file path is empty.");
			return;
		}

		while (!Thread.currentThread().isInterrupted()) {
			try {
				ProcessBuilder pb = new ProcessBuilder("openvpn", ovpnPath);
				pb.redirectErrorStream(true);

				currentVpnProcess = pb.start();

				try (BufferedReader reader = new BufferedReader(
						new InputStreamReader(currentVpnProcess.getInputStream()))) {

					String line;
					boolean connected = false;
					long startTime = System.currentTimeMillis();
					while (!Thread.currentThread().isInterrupted() && (line = reader.readLine()) != null) {
						System.out.println(line);

						if (!connected && line.contains("Initialization Sequence Completed")) {
							ConnectStatusGui.accept("Connected");
							VPNConfigDAO.SetConnection(activeWifiProfileId, encodedName);
							LocationStatusGui.accept("Current Location: " + CountryCodeConverter.getCountryName(
									VPNConfigDAO.GetConnectedCountry()));
							IpStatusGui.accept("Ip: " + VPNConfigDAO.GetConnectedIpAddress());
							ConnectButtonGui.accept("Disconnect");
							connected = true;

						} else if (!connected) {
							ConnectStatusGui.accept("Connecting...");
						}

						// Handle restart
						if (line.contains("SIGUSR1[soft,ping-restart] received, process restarting")) {
							ConnectStatusGui.accept("Connection restarting...");
							connected = false; // Reset flag
							System.out.println("Restart detected, attempting reconnection...");

							// Destroy the current process
							if (currentVpnProcess != null && currentVpnProcess.isAlive()) {
								currentVpnProcess.destroyForcibly();
								currentVpnProcess.waitFor(); // Ensure process is terminated
							}

							// Break inner loop to restart the outer loop (reconnect)
							break;
						}
						// will add auto reconnect
						for (String errorKeyword : errorKeywords) {
							if (!connected && (line.contains(errorKeyword) || System.currentTimeMillis()
									- startTime >= 25 * 1000)) {
								// TODO show pop up and need to refresh UI after this
								VPNConfigDAO.DeleteUnresponsiveOvpn(activeWifiProfileId, ovpnPath);
								VPNConfigDAO.refreshAndGenerateEncodedCountries(activeWifiProfileId);
								VPNManager.disconnectVpn();
								currentVpnProcess.destroyForcibly();
								currentVpnProcess.waitFor();

								Platform.runLater(() -> {
									try {
										guiController.Refresh();
									} catch (SQLException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									guiController.Refresh2();
								});
								ConnectStatusGui.accept("Deleted");

								break;
							}

						}

					}

					// If the inner loop breaks due to a restart, continue to retry
					if (!Thread.currentThread().isInterrupted()) {
						// TODO will show with a pop up
						ConnectStatusGui.accept("Reconnecting...");
						continue; // Retry connection
					}

				} catch (InterruptedException e) {
					ConnectStatusGui.accept("Disconnected");
					Thread.currentThread().interrupt(); // Restore interrupt status
					if (currentVpnProcess != null && currentVpnProcess.isAlive()) {
						currentVpnProcess.destroyForcibly();
					}
					break; // Exit outer loop on interrupt

				} catch (IOException e) {
					ConnectStatusGui.accept("Error reading OpenVPN output: " + e.getMessage());
					break; // Exit on IO error
				}

				// Wait for process to exit and check exit code
				try {
					int exitCode = currentVpnProcess.waitFor();
					ConnectStatusGui.accept("VPN connection ended with exit code: " + exitCode);
				} catch (InterruptedException e) {
					ConnectButtonGui.accept("Connect");
					disconnectVpn(ConnectStatusGui);
					ConnectStatusGui.accept("Connection interrupted.");
					Thread.currentThread().interrupt();
					break;
				}

			} catch (IOException e) {
				ConnectStatusGui.accept("Error starting OpenVPN: " + e.getMessage());
				break; // Exit on process start failure
			} finally {
				currentVpnProcess = null; // Reset process reference
			}
		}
	}

	public static void disconnectVpn(Consumer<String> ConnectStatusGui) {
		try {
			// 1. Try to destroy the known process first
			if (currentVpnProcess != null && currentVpnProcess.isAlive()) {
				currentVpnProcess.destroyForcibly();
				ConnectStatusGui.accept("Terminating active OpenVPN process...");
				System.out.println("Terminating");
			}

			// 2. Fallback: Kill all openvpn processes using `killall openvpn`
			ProcessBuilder pb = new ProcessBuilder("killall", "openvpn");
			pb.redirectErrorStream(true); // Merge stderr into stdout

			Process process = pb.start();
			System.out.println("Killed VPNs");
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()))) {

				String line;
				while ((line = reader.readLine()) != null) {
					System.out.println("Killall output: " + line);
				}
			}

			int exitCode = process.waitFor();
			if (exitCode == 0) {
				ConnectStatusGui.accept("Disconnected");
			} else {
				ConnectStatusGui.accept("No running OpenVPN processes found.");
			}

		} catch (IOException | InterruptedException e) {
			ConnectStatusGui.accept("Error disconnecting VPN: " + e.getMessage());
			Thread.currentThread().interrupt(); // Restore interrupted status
		} finally {
			currentVpnProcess = null; // Reset reference
		}
	}

	public static void disconnectVpn() {
		try {
			// 1. Try to destroy the known process first
			if (currentVpnProcess != null || currentVpnProcess.isAlive()) {
				currentVpnProcess.destroyForcibly();
				System.out.println("Terminating active OpenVPN process...");
				System.out.println("Terminating");
			}

			// 2. Fallback: Kill all openvpn processes using `killall openvpn`
			ProcessBuilder pb = new ProcessBuilder("killall", "openvpn");
			pb.redirectErrorStream(true); // Merge stderr into stdout

			Process process = pb.start();
			System.out.println("Killed VPNs");
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()))) {

				String line;
				while ((line = reader.readLine()) != null) {
					System.out.println("Killall output: " + line);
				}
			}

			int exitCode = process.waitFor();
			if (exitCode == 0) {
				System.out.println("Disconnected");
			} else {
				System.out.println("No running OpenVPN processes found.");
			}

		} catch (IOException | InterruptedException e) {
			System.out.println("Error disconnecting VPN: " + e.getMessage());
			Thread.currentThread().interrupt(); // Restore interrupted status
		} finally {
			currentVpnProcess = null; // Reset reference
		}
	}

	public static void recheckTheOvpns(MainGuiController guiController,
			Consumer<String> ConnectStatusGui, Consumer<String> ConnectButtonGui) throws SQLException, Exception {
		int activeWifiProfileId = WifiProfileDAO.getActiveWifiProfileId();
		List<String> ovpnPaths = VPNConfigDAO.getActiveProfileOvpnPaths(activeWifiProfileId);
		System.out.println(ovpnPaths.size());
		int i = 0;
		for (String ovpnPath : ovpnPaths) {
			i = i + 1;
			ConnectStatusGui.accept("Rechecking" + i);

			if (!OvpnFileTester.testOvpnFileReCheck(ovpnPath)) {
				VPNConfigDAO.DeleteUnresponsiveOvpn(activeWifiProfileId, ovpnPath);
				VPNConfigDAO.refreshAndGenerateEncodedCountries(activeWifiProfileId);
				// VPNManager.disconnectVpn();
				guiController.Refresh();
			}

		}
		ConnectStatusGui.accept("Recheck completed");
		// Platform.runLater(() -> {
		// try {

		// } catch (SQLException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// guiController.Refresh2();
		// ConnectStatusGui.accept("Deleted");

		// });
	}

}