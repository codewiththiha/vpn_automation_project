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
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.time.LocalDateTime;

import vpn_automation.backend.db.VPNConfigDAO;
import vpn_automation.backend.db.WifiProfileDAO;
import vpn_automation.gui.control.MainGuiController;

public class OvpnFileTester {
	private final static int TIMEOUT_SECONDS = 20;
	private static volatile Process currentOvpnProcess = null;

	public List<String> testOvpnFiles(String directory, Consumer<String> guiUpdater, int Limit)
			throws SQLException, Exception {
		guiUpdater.accept("Starting OVPN file testing...");
		guiUpdater.accept("Scanning directory: " + directory);

		List<String> workingFiles = new ArrayList<>();

		try {
			List<Path> ovpnFiles = FileUtils.getOvpnFiles(directory);
			if (ovpnFiles.isEmpty()) {
				guiUpdater.accept("No .ovpn files found in the current directory");
				return workingFiles;
			}

			for (Path file : ovpnFiles) {
				int SearchStatus = WifiProfileDAO.GetSearchStatus();
				try {
					if (workingFiles.size() == Limit || (SearchStatus == 0)) { // you can specify
																				// here for the user
						// limit
						System.out.println("Stopped");
						break;
					}
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

	public List<String> testOvpnFiles(String directory, Consumer<String> guiUpdater, int Limit,
			MainGuiController guiController)
			throws SQLException, Exception {
		guiUpdater.accept("Starting OVPN file testing...");
		guiUpdater.accept("Scanning directory: " + directory);

		List<String> workingFiles = new ArrayList<>();

		try {
			List<Path> ovpnFiles = FileUtils.getOvpnFiles(directory);
			if (ovpnFiles.isEmpty()) {
				guiUpdater.accept("No .ovpn files found in the current directory");
				return workingFiles;
			}

			for (Path file : ovpnFiles) {
				int SearchStatus = WifiProfileDAO.GetSearchStatus();
				try {
					if (workingFiles.size() == Limit || (SearchStatus == 0)) { // you can specify
																				// here for the user
						// limit
						System.out.println("Stopped");
						break;
					}
					if (FileUtils.isChecked(file.toString())) {
						guiUpdater.accept("Skipping already checked file: " + file.getFileName());
						continue;
					}

					guiUpdater.accept(
							"Testing: " + file.getFileName() + "\n" + workingFiles.size() + " vpn configs found.");

					if (testOvpnFile(file, guiUpdater)) {
						workingFiles.add(file.toString());
						guiUpdater.accept("Success: " + file.getFileName() + " connected successfully");
						guiController.RefreshMain();
						guiController.Refresh();
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
							System.out.println("Adding");
							IPInfoFetcher.clearCache();
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

	private static boolean testOvpnFileForDebug(String file) throws SQLException, Exception {
		Process process = null;
		int activeWifiProfileId = WifiProfileDAO.getActiveWifiProfileId();
		LocalDateTime now = LocalDateTime.now();

		try {
			ProcessBuilder pb = new ProcessBuilder("openvpn", file);
			pb.redirectErrorStream(true); // Merge stdout and stderr
			process = pb.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			long startTime = System.currentTimeMillis();
			String line;

			while ((line = reader.readLine()) != null) {
				System.out.println(line);

				if (line.contains("Initialization Sequence Completed")) {
					System.out.println("Adding");
					IPInfoFetcher.clearCache();
					String ip = IPInfoFetcher.getIPAddress();
					String country = IPInfoFetcher.getCountry();
					VPNConfigDAO.UpdateOvpnFilePaths(file, activeWifiProfileId, ip, country, now);
					return true;
				}

				// Check for errors or timeout every iteration
				for (String errorKeyword : VPNManager.errorKeywords) {
					if (line.contains(errorKeyword)) {
						System.out.println("Error detected: " + errorKeyword);
						return false;
					}
				}

				if (System.currentTimeMillis() - startTime >= 25 * 1000) {
					System.out.println("Timeout reached.");
					return false;
				}
			}

			// If we exit loop without returning, assume failure
			return false;

		} catch (IOException | InterruptedException e) {
			System.out.println("Error testing " + file + ": " + e.getMessage());
			return false;
		} finally {
			if (process != null && process.isAlive()) {
				process.destroy();
				try {
					process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					process.destroyForcibly();
					Thread.currentThread().interrupt(); // Restore interrupt status
				}
			}
		}
	}

	public static int testOvpnFileReCheck(String file) throws SQLException, Exception {
		Process process = null;

		try {
			ProcessBuilder pb = new ProcessBuilder("openvpn", file);
			pb.redirectErrorStream(true); // Merge stdout and stderr
			process = pb.start();
			currentOvpnProcess = process;
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			long startTime = System.currentTimeMillis();
			String line;

			while ((line = reader.readLine()) != null) {
				System.out.println(line);

				if (line.contains("Initialization Sequence Completed")) {
					System.out.println("Adding");
					IPInfoFetcher.clearCache();

					return 1;
				}

				for (String errorKeyword : VPNManager.errorKeywords) {
					if (line.contains(errorKeyword)) {
						System.out.println("Error detected: " + errorKeyword);
						return -1;
					}
				}

				if (System.currentTimeMillis() - startTime >= 25 * 1000) {
					System.out.println("Timeout reached.");
					return 0;
				}
			}

			return 0;

		} catch (IOException e) {
			System.out.println("Error testing " + file + ": " + e.getMessage());
			return -1;
		} finally {
			if (process != null && process.isAlive()) {
				process.destroy();
				try {
					process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					process.destroyForcibly();
					Thread.currentThread().interrupt();
				}
				currentOvpnProcess = null;
			}
		}
	}

	public static void cancelOvpnProcess() {
		if (currentOvpnProcess != null && currentOvpnProcess.isAlive()) {
			currentOvpnProcess.destroy();
			System.out.println("Canceled ovpn Process");
			try {
				currentOvpnProcess.waitFor(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				currentOvpnProcess.destroyForcibly();
				Thread.currentThread().interrupt();
			}
			currentOvpnProcess = null;
		}
	}

	// should add at the end of each tested conditions
	public static void fixUnknownOvpns() throws SQLException, Exception {
		int activeWifiProfileId = WifiProfileDAO.getActiveWifiProfileId();
		System.out.println(activeWifiProfileId);
		while (true) {
			List<String> files = VPNConfigDAO.getUnknownOvpnPaths(activeWifiProfileId);
			System.out.println("Size: " + files.size());
			if (files.size() == 0) {
				System.out.println("Quitting");
				// VPNManager.disconnectVpn();

				break;
			}
			for (String file : files) {
				System.out.println("Here0009");
				if (testOvpnFileForDebug(file)) {

					System.out.println("Success " + file);

				} else {
					VPNConfigDAO.DeleteUnresponsiveOvpnByPath(activeWifiProfileId, file);
					// VPNConfigDAO.refreshAndGenerateEncodedCountries(activeWifiProfileId);
				}
			}

		}

	}

}