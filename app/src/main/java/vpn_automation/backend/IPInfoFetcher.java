package vpn_automation.backend;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONObject;

public class IPInfoFetcher {

	private static JSONObject cachedIPInfo = null;

	private static JSONObject fetchIPInfo() throws Exception {
		if (cachedIPInfo != null) {
			return cachedIPInfo;
		}

		ProcessBuilder processBuilder = new ProcessBuilder("curl", "-s",
				"https://api.ipinfo.io/lite/me?token=44936a1f60206d");
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
			return new JSONObject();
		}

		String jsonResponse = output.toString();
		try {
			cachedIPInfo = new JSONObject(jsonResponse);
		} catch (Exception e) {
			System.err.println("Failed to parse JSON response: " + jsonResponse);
			return new JSONObject();
		}

		return cachedIPInfo;
	}

	public static String getIPAddress() throws Exception {
		JSONObject info = fetchIPInfo();
		return info.optString("ip", "unknown");
	}

	public static String getCountry() throws Exception {
		JSONObject info = fetchIPInfo();
		return info.optString("country_code", "unknown");
	}

	public static void clearCache() {
		cachedIPInfo = null;
	}
}