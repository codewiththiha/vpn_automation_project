package vpn_automation.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.time.LocalDateTime;

import vpn_automation.backend.db.VPNConfigDAO;
import vpn_automation.backend.db.WifiProfileDAO;

public class OvpnFileTester {
	private final static int TIMEOUT_SECONDS = 20;

	public List<String> testOvpnFiles(String directory, Consumer<String> guiUpdater) throws SQLException, Exception {
		guiUpdater.accept("Starting OVPN file testing...");
		guiUpdater.accept("Scanning directory: " + directory);

		List<String> workingFiles = new ArrayList<>();

		try {
			List<Path> ovpnFiles = FileUtils.getOvpnFiles(directory);
			if (ovpnFiles.isEmpty()) {
				guiUpdater.accept("No .ovpn files found in the current directory");
				return workingFiles;
			}
			// you can specify here for the user limit option
			for (Path file : ovpnFiles) {
				try {
					if (FileUtils.isChecked(file.toString())) {
						guiUpdater.accept("Skipping already checked file: " + file.getFileName());
						continue;
					}

					guiUpdater.accept(
							"Testing: " + file.getFileName() + "\n" + workingFiles.size() + " vpn configs found.");

					if (testOvpnFile(file, guiUpdater)) {
						workingFiles.add(file.toString());
						guiUpdater.accept("Success: " + file.getFileName() + " connected successfully");
					} else {
						guiUpdater.accept("Failed: " + file.getFileName());
					}
				} catch (IOException e) {
					guiUpdater.accept("Error processing " + file.getFileName() + ": " + e.getMessage());
				}
				System.out.println("--------------------------------------------------");
			}

			if (!workingFiles.isEmpty()) {
				saveWorkingFiles(workingFiles, guiUpdater);
				guiUpdater.accept("Found " + workingFiles.size() + " working OVPN files");
			} else {
				guiUpdater.accept("No working OVPN files found");
			}
		} catch (IOException e) {
			guiUpdater.accept("Error scanning directory: " + e.getMessage());
		}

		guiUpdater.accept("Testing complete");
		return workingFiles;
	}

	private boolean testOvpnFile(Path file, Consumer<String> guiUpdater) throws SQLException, Exception {
		Process process = null;
		int activeWifiProfileId = WifiProfileDAO.getActiveWifiProfileId();
		LocalDateTime now = LocalDateTime.now();
		try {
			ProcessBuilder pb = new ProcessBuilder(
					"openvpn",
					file.toString());

			pb.redirectErrorStream(true); // Merge stdout and stderr
			process = pb.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			long startTime = System.currentTimeMillis();
			String line;

			while (true) {
				if (reader.ready()) {
					line = reader.readLine();
					if (line != null) {
						// way too much for a normal user
						// guiUpdater.accept(line);
						System.out.println(line);
						if (line.contains("Initialization Sequence Completed")) {
							VPNConfigDAO.insertOvpnFilePaths(file.toString(), activeWifiProfileId,
									IPInfoFetcher.getIPAddress(),
									IPInfoFetcher.getCountry(), now);
							return true;
						}
					}
				}

				if (System.currentTimeMillis() - startTime >= TIMEOUT_SECONDS * 1000) {
					guiUpdater.accept(
							"Timeout: " + file.getFileName() + " failed within " + TIMEOUT_SECONDS + " seconds");
					return false;
				}

				Thread.sleep(100);
			}

		} catch (IOException | InterruptedException e) {
			guiUpdater.accept("Error testing " + file.getFileName() + ": " + e.getMessage());
			return false;
		} finally {
			if (process != null && process.isAlive()) {
				process.destroy();
				try {
					process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					process.destroyForcibly();
				}
			}
		}
	}

	private void saveWorkingFiles(List<String> workingFiles, Consumer<String> guiUpdater) {
		try {
			List<String> lines = new ArrayList<>();
			lines.add("Working VPN Configuration Files:");
			lines.add("----------------------------------------");
			lines.addAll(workingFiles);
			Files.write(Paths.get("working_vpn_files.txt"), lines);
			guiUpdater.accept("Results saved to working_vpn_files.txt");
		} catch (IOException e) {
			guiUpdater.accept("Error saving results: " + e.getMessage());
		}
	}
}