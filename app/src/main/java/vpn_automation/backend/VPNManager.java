package vpn_automation.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import vpn_automation.backend.db.VPNConfigDAO;

public class VPNManager {

	private static volatile Process currentVpnProcess = null;

	public static void connectVpn(String ovpnPath, Consumer<String> ConnectStatusGui,
			Consumer<String> LocationStatusGui,
			Consumer<String> IpStatusGui, int activeWifiProfileId, String encodedName) // can use runnable
			throws IOException {
		if (ovpnPath == null || ovpnPath.isEmpty()) {
			ConnectStatusGui.accept("Error: OVPN file path is empty.");
			return;
		}

		try {
			ProcessBuilder pb = new ProcessBuilder("openvpn", ovpnPath);
			pb.redirectErrorStream(true);

			currentVpnProcess = pb.start();

			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(currentVpnProcess.getInputStream()))) {

				String line;
				boolean connected = false;

				while (!Thread.currentThread().isInterrupted() && (line = reader.readLine()) != null) {
					System.out.println(line);

					if (!connected && line.contains("Initialization Sequence Completed")) {
						ConnectStatusGui.accept("Connected");
						VPNConfigDAO.SetConnection(activeWifiProfileId, encodedName);
						LocationStatusGui.accept("Current Location: " + CountryCodeConverter.getCountryName(
								VPNConfigDAO.GetConnectedCountry()));
						IpStatusGui.accept("Ip: " + VPNConfigDAO.GetConnectedIpAddress());
						connected = true;

					} else if (!connected) {
						ConnectStatusGui.accept("Connecting...");
					}

				}

				int exitCode = currentVpnProcess.waitFor();
				ConnectStatusGui.accept("VPN connection ended with exit code: " + exitCode);

			} catch (InterruptedException e) {
				ConnectStatusGui.accept("Connection interrupted.");
				Thread.currentThread().interrupt(); // Restore interrupt status
				if (currentVpnProcess != null && currentVpnProcess.isAlive()) {
					currentVpnProcess.destroyForcibly();
				}

			}
		} finally {
			currentVpnProcess = null;
		}
	}

	public static void disconnectVpn(Consumer<String> ConnectStatusGui) {
		try {
			// 1. Try to destroy the known process first
			if (currentVpnProcess != null && currentVpnProcess.isAlive()) {
				currentVpnProcess.destroyForcibly();
				ConnectStatusGui.accept("Terminating active OpenVPN process...");
			}

			// 2. Fallback: Kill all openvpn processes using `killall openvpn`
			ProcessBuilder pb = new ProcessBuilder("killall", "openvpn");
			pb.redirectErrorStream(true); // Merge stderr into stdout

			Process process = pb.start();

			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()))) {

				String line;
				while ((line = reader.readLine()) != null) {
					System.out.println("Killall output: " + line);
				}
			}

			int exitCode = process.waitFor();
			if (exitCode == 0) {
				ConnectStatusGui.accept("All OpenVPN processes successfully terminated.");
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

}