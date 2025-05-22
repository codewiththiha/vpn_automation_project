package vpn_automation.backend;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

import vpn_automation.backend.db.VPNConfigDAO;
import vpn_automation.backend.db.WifiProfileDAO;

public class FileUtils {

	public static List<Path> getOvpnFiles(String directory) throws IOException {
		return Files.list(Paths.get(directory))
				.filter(path -> path.toString().endsWith(".ovpn"))
				.collect(Collectors.toList());
	}

	public static List<String> readLines(Path path) throws IOException {
		return Files.readAllLines(path);
	}

	public static void writeLines(Path path, List<String> lines) throws IOException {
		Files.write(path, lines, StandardOpenOption.TRUNCATE_EXISTING);
	}

	public static void appendToFile(Path path, String content) throws IOException {
		Files.write(path, (content + "\n").getBytes(), StandardOpenOption.APPEND);
	}

	// new mechanism needed here
	public static boolean isChecked(String pathString) throws IOException {
		int activeWifiProfileId = WifiProfileDAO.getActiveWifiProfileId();
		List<String> CheckedOvpnPaths = VPNConfigDAO.giveWifiProfileIdCheckedGetOvpnFilesPaths(activeWifiProfileId);
		if (CheckedOvpnPaths.contains(pathString)) {
			System.out.println("Checked Skipped");
			return true;
		}

		return false;
	}
}