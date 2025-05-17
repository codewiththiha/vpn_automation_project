package vpn_automation.backend;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONObject;

public class IPInfoFetcher {

	private static JSONObject fetchIPInfo() throws Exception {
		ProcessBuilder processBuilder = new ProcessBuilder("curl", "-s", "ipinfo.io");
		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();

		StringBuilder output = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line);
			}
		}

		int exitCode = process.waitFor();
		if (exitCode != 0) {
			System.err.println("cURL failed to fetch IP info (exit code: " + exitCode + ")");
			// Return a default JSON object instead of throwing
			return new JSONObject();
		}

		return new JSONObject(output.toString());
	}

	public static String getIPAddress() throws Exception {
		JSONObject info = fetchIPInfo();
		return info.getString("ip");
	}

	public static String getCountry() throws Exception {
		JSONObject info = fetchIPInfo();
		return info.optString("country", "unknown");
	}
}