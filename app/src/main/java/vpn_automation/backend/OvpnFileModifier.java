package vpn_automation.backend;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// Class to modify .ovpn files
public class OvpnFileModifier {
	private static final String OLD_CIPHER = "cipher AES-128-CBC";
	private static final String NEW_CIPHER = "data-ciphers AES-256-GCM:AES-128-GCM:CHACHA20-POLY1305:AES-128-CBC";

	public void modifyOvpnFiles(String directory, Consumer<String> guiUpdater) {
		guiUpdater.accept("Starting OVPN cipher replacement...");
		guiUpdater.accept("Scanning directory: " + directory);

		try {
			List<Path> ovpnFiles = FileUtils.getOvpnFiles(directory);
			if (ovpnFiles.isEmpty()) {
				guiUpdater.accept("No .ovpn files found in the current directory");
				return;
			}

			for (Path file : ovpnFiles) {
				guiUpdater.accept("Processing: " + file.getFileName());
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
						guiUpdater.accept("✅ Updated: " + file.getFileName());
					} else {
						guiUpdater.accept("ℹ️ No changes needed in: " + file.getFileName());
					}
				} catch (IOException e) {
					guiUpdater.accept("⚠ Error processing " + file.getFileName() + ": " + e.getMessage());
				}
			}
		} catch (IOException e) {
			guiUpdater.accept("⚠ Error scanning directory: " + e.getMessage());
		}
		guiUpdater.accept("Processing complete");
	}
}