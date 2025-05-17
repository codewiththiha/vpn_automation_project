package vpn_automation.backend;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

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

	public static boolean isChecked(Path path) throws IOException {
		List<String> lines = readLines(path);

		for (int i = lines.size() - 1; i >= 0; i--) {
			String line = lines.get(i).trim();
			if (!line.isEmpty()) {
				return line.equals("# checked");
			}
		}

		return false;
	}
}