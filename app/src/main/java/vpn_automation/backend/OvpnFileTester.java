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

import vpn_automation.backend.db.VPNConfigDAO;

public class OvpnFileTester {
	private final static int TIMEOUT_SECONDS = 20;

	public List<String> testOvpnFiles(String directory) throws SQLException, Exception {
		System.out.println("Starting OVPN file testing...");
		System.out.println("Scanning directory: " + directory);

		List<String> workingFiles = new ArrayList<>();

		try {
			List<Path> ovpnFiles = FileUtils.getOvpnFiles(directory);
			if (ovpnFiles.isEmpty()) {
				System.out.println("No .ovpn files found in the current directory");
				return workingFiles;
			}

			for (Path file : ovpnFiles) {
				try {
					if (FileUtils.isChecked(file)) {
						System.out.println("Skipping already checked file: " + file.getFileName());
						continue;
					}

					if (testOvpnFile(file)) {
						workingFiles.add(file.toString()); // will return with the absolute path don't know how to
															// return relative tho. getFileName() for file name only
						// VPNConfigDAO.insertOvpnFilePaths(file.toString(), 1,
						// IPInfoFetcher.getIPAddress(),
						// IPInfoFetcher.getCountry());
					}
					// markAsChecked(file);
				} catch (IOException e) {
					System.out.println("Error processing " + file.getFileName() + ": " + e.getMessage());
				}
				System.out.println("--------------------------------------------------");
			}

			if (!workingFiles.isEmpty()) {
				saveWorkingFiles(workingFiles);
				System.out.println("Found " + workingFiles.size() + " working OVPN files");
			} else {
				System.out.println("No working OVPN files found");
			}
		} catch (IOException e) {
			System.out.println("Error scanning directory: " + e.getMessage());
		}

		System.out.println("Testing complete");
		return workingFiles;
	}

	private boolean testOvpnFile(Path file) throws SQLException, Exception {
		System.out.println("Testing: " + file.getFileName());
		Process process = null;

		try {
			// ProcessBuilder pb = new ProcessBuilder("sudo", "openvpn", file.toString());
			// // originally it was only 'openvpnc'
			ProcessBuilder pb = new ProcessBuilder(
					"sudo",
					"openvpn",
					"--dev", "tun",
					"--config", file.toString());

			pb.redirectErrorStream(true); // Merge stdout and stderr
			process = pb.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			long startTime = System.currentTimeMillis();
			String line;

			while (true) {
				if (reader.ready()) {
					line = reader.readLine();
					if (line != null) {
						System.out.println(line);
						if (line.contains("Initialization Sequence Completed")) {
							// Successfully connected
							VPNConfigDAO.insertOvpnFilePaths(file.toString(), 1,
									IPInfoFetcher.getIPAddress(),
									IPInfoFetcher.getCountry());
							System.out.println(file.getFileName() + " connected successfully");

							// Optionally terminate the connection after success detection
							// Or let it run in background
							return true;
						}
					}
				}

				// If we haven't returned yet, check if we should time out
				if (System.currentTimeMillis() - startTime >= TIMEOUT_SECONDS * 1000) {
					System.out
							.println(file.getFileName() + " failed to connect within " + TIMEOUT_SECONDS + " seconds");
					return false;
				}

				Thread.sleep(100); // Sleep 100ms
			}

		} catch (IOException | InterruptedException e) {
			System.out.println("Error testing " + file.getFileName() + ": " + e.getMessage());
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

	// private void markAsChecked(Path file) {
	// try {
	// FileUtils.appendToFile(file, "# checked");
	// System.out.println("Marked as checked: " + file.getFileName());
	// } catch (IOException e) {
	// System.out.println("Error marking " + file.getFileName() + " as checked: " +
	// e.getMessage());
	// }
	// }

	private void saveWorkingFiles(List<String> workingFiles) {
		try {
			List<String> lines = new ArrayList<>();
			lines.add("Working VPN Configuration Files:");
			lines.add("----------------------------------------");
			lines.addAll(workingFiles);
			Files.write(Paths.get("working_vpn_files.txt"), lines);
			System.out.println("Results saved to working_vpn_files.txt");
		} catch (IOException e) {
			System.out.println("Error saving results: " + e.getMessage());
		}
	}
}