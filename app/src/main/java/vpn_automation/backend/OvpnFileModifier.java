package vpn_automation.backend;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Class to modify .ovpn files
public class OvpnFileModifier {
	private static final String OLD_CIPHER = "cipher AES-128-CBC";
	private static final String NEW_CIPHER = "data-ciphers AES-256-GCM:AES-128-GCM:CHACHA20-POLY1305:AES-128-CBC";

	public void modifyOvpnFiles(String directory) {
		System.out.println("Starting OVPN cipher replacement...");
		System.out.println("Scanning directory: " + directory);

		try {
			List<Path> ovpnFiles = FileUtils.getOvpnFiles(directory);
			if (ovpnFiles.isEmpty()) {
				System.out.println("No .ovpn files found in the current directory");
				return;
			}

			for (Path file : ovpnFiles) {
				System.out.println("Processing: " + file.getFileName());
				try {
					List<String> lines = FileUtils.readLines(file);
					boolean modified = false;
					List<String> newLines = new ArrayList<>();

					for (String line : lines) {
						if (line.trim().equals(OLD_CIPHER)) {
							newLines.add(NEW_CIPHER);
							modified = true;
						} else {
							newLines.add(line);
						}
					}

					if (modified) {
						FileUtils.writeLines(file, newLines);
						System.out.println("Updated: " + file.getFileName());
					} else {
						System.out.println("No changes needed in: " + file.getFileName());
					}
				} catch (IOException e) {
					System.out.println("Error processing " + file.getFileName() + ": " + e.getMessage());
				}
			}
		} catch (IOException e) {
			System.out.println("Error scanning directory: " + e.getMessage());
		}
		System.out.println("Processing complete");
	}
}
