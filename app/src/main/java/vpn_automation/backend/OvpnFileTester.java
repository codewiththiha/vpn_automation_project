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

import vpn_automation.backend.db.VPNConfigDAO;

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

			for (Path file : ovpnFiles) {
				try {
					if (FileUtils.isChecked(file)) {
						guiUpdater.accept("Skipping already checked file: " + file.getFileName());
						continue;
					}

					guiUpdater.accept("Testing: " + file.getFileName());

					if (testOvpnFile(file, guiUpdater)) {
						workingFiles.add(file.toString());
						guiUpdater.accept("✅ Success: " + file.getFileName() + " connected successfully");
					} else {
						guiUpdater.accept("❌ Failed: " + file.getFileName());
					}
				} catch (IOException e) {
					guiUpdater.accept("⚠ Error processing " + file.getFileName() + ": " + e.getMessage());
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
			guiUpdater.accept("⚠ Error scanning directory: " + e.getMessage());
		}

		guiUpdater.accept("Testing complete");
		return workingFiles;
	}

	private boolean testOvpnFile(Path file, Consumer<String> guiUpdater) throws SQLException, Exception {
		Process process = null;

		try {
			ProcessBuilder pb = new ProcessBuilder(
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
						guiUpdater.accept(line);
						if (line.contains("Initialization Sequence Completed")) {
							VPNConfigDAO.insertOvpnFilePaths(file.toString(), 1,
									IPInfoFetcher.getIPAddress(),
									IPInfoFetcher.getCountry());
							return true;
						}
					}
				}

				if (System.currentTimeMillis() - startTime >= TIMEOUT_SECONDS * 1000) {
					guiUpdater.accept(
							"⏰ Timeout: " + file.getFileName() + " failed within " + TIMEOUT_SECONDS + " seconds");
					return false;
				}

				Thread.sleep(100);
			}

		} catch (IOException | InterruptedException e) {
			guiUpdater.accept("⚠ Error testing " + file.getFileName() + ": " + e.getMessage());
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
			guiUpdater.accept("💾 Results saved to working_vpn_files.txt");
		} catch (IOException e) {
			guiUpdater.accept("⚠ Error saving results: " + e.getMessage());
		}
	}
}