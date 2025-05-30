package vpn_automation.backend.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import vpn_automation.backend.CountryCodeConverter;
import vpn_automation.gui.control.MainGuiController;

import java.time.LocalDateTime;

public class VPNConfigDAO {

	// Method to insert OVPN file paths into the database
	public static void insertOvpnFilePaths(String ovpnFilePath, int wifiProfileId, String ipAddress, String country,
			LocalDateTime now)
			throws SQLException {
		String insertQuery = "INSERT INTO vpnconfig (wifi_profile_id, ovpn_file_path, ip_address, country, last_checked) VALUES (?, ?, ?, ?, ?)";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

			pstmt.setInt(1, wifiProfileId); // wifi_profile_id
			pstmt.setString(2, ovpnFilePath); // ovpn_file_path
			pstmt.setString(3, ipAddress);
			pstmt.setString(4, country);
			pstmt.setObject(5, now);

			// Execute the insert
			pstmt.executeUpdate();

			System.out.println("OVPN file paths inserted successfully.");
		} catch (SQLException e) {
			System.err.println("Error inserting OVPN file paths:");
			e.printStackTrace();
		}
	}

	public static List<Integer> giveWifiProfileIdGetvpnConfigIds(int wifiProfileID) {
		List<Integer> vpnConfigIds = new ArrayList<>();
		String query = "SELECT config_id FROM vpnconfig WHERE wifi_profile_id = ?";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setInt(1, wifiProfileID);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				int vpnConfigId = rs.getInt("config_id");
				vpnConfigIds.add(vpnConfigId);
			}

		} catch (SQLException e) {
			System.err.println("Error retrieving config_ids:");
			e.printStackTrace();
		}

		return vpnConfigIds;
	}

	public static List<String> giveWifiProfileIdCheckedGetOvpnFilesPaths(int wifiProfileID) {
		List<String> ovpnFilePaths = new ArrayList<>();
		String query = "SELECT ovpn_file_path FROM vpnconfig WHERE wifi_profile_id = ?  AND last_checked > NOW() - INTERVAL 3 DAY";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setInt(1, wifiProfileID);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				String ovpnFilePath = rs.getString("ovpn_file_path");
				ovpnFilePaths.add(ovpnFilePath);
			}

		} catch (SQLException e) {
			System.err.println("Error retrieving config_ids:");
			e.printStackTrace();
		}

		return ovpnFilePaths;
	}

	public static List<String> getCountries(int wifiProfileID) {
		List<String> countries = new ArrayList<>();
		String query = "SELECT country FROM vpnconfig WHERE wifi_profile_id = ?";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setInt(1, wifiProfileID);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				String country = rs.getString("country");
				countries.add(country);
			}

		} catch (SQLException e) {
			System.err.println("Error retrieving config_ids:");
			e.printStackTrace();
		}

		return countries;
	}

	public static void refreshAndGenerateEncodedCountries(int wifiProfileId) {
		List<String> countryCodes = getCountries(wifiProfileId);
		CountryCodeConverter.resetCounter();
		replaceWithNULLToEncoded(wifiProfileId);

		for (String countryCode : countryCodes) {
			String Encodedcountry = CountryCodeConverter.counter(countryCode);
			setEncodedCountries(Encodedcountry, countryCode.toUpperCase(), wifiProfileId);
		}
	}

	public static void replaceWithNULLToEncoded(int wifiProfileId) {
		String query = "UPDATE vpnconfig SET encoded_names = NULL where wifi_profile_id = ?";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setInt(1, wifiProfileId);
			int rowsUpdated = pstmt.executeUpdate();
			if (rowsUpdated > 0) {
				System.out.println("NULL replaced");
			} else {
				System.out.println("Null replace process failed!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void setEncodedCountries(String encodedName, String countryCode, int wifiProfileId) {
		String query = "UPDATE vpnconfig SET encoded_names = ? WHERE country = ? AND wifi_profile_id = ? AND encoded_names IS NULL LIMIT 1";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setString(1, encodedName);
			pstmt.setString(2, countryCode);
			pstmt.setInt(3, wifiProfileId);

			// Execute the update
			int rowsUpdated = pstmt.executeUpdate();

			if (rowsUpdated > 0) {
				System.out.println("Successfully updated a row with encoded name: " + encodedName);
			} else {
				System.out.println("No matching row found for country: " + countryCode +
						", wifi_profile_id: " + wifiProfileId);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static List<String> getEncodedCountries(int wifiProfileID) {
		List<String> countries = new ArrayList<>();
		String query = "SELECT encoded_names FROM vpnconfig WHERE wifi_profile_id = ?";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setInt(1, wifiProfileID);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				String country = rs.getString("encoded_names");
				countries.add(country);
			}

		} catch (SQLException e) {
			System.err.println("Error retrieving encoded_names");
			e.printStackTrace();
		}

		return countries;
	}

	public static String getOvpnConnection(int activeWifiProfileId, String encodedName) {
		String query = "SELECT ovpn_file_path FROM vpnconfig WHERE wifi_profile_id = ? AND encoded_names = ?";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setInt(1, activeWifiProfileId);
			pstmt.setString(2, encodedName);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					// Return the file path if a match is found
					return rs.getString("ovpn_file_path");
				} else {
					// No matching row found
					return null;
				}
			}

		} catch (SQLException e) {
			System.err.println("Error retrieving ovpn_file_path:");
			e.printStackTrace();
			return null;
		}
	}

	public static void SetConnection(int activeWifiProfileId, String encodedName) {
		String query = "UPDATE vpnconfig SET is_connected = 1 WHERE wifi_profile_id = ? AND encoded_names = ?";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setInt(1, activeWifiProfileId);
			pstmt.setString(2, encodedName);

			// Execute the update
			int rowsUpdated = pstmt.executeUpdate();

			if (rowsUpdated > 0) {
				System.out.println("Set connection to " + encodedName);
			} else {
				System.out.println("Set connection Failed");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void SetVpnDisconnect() {
		// TODO this logic is incorrect need fix
		String query = "UPDATE vpnconfig SET is_connected = 0";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			// Execute the update
			int rowsUpdated = pstmt.executeUpdate();

			if (rowsUpdated > 0) {
				System.out.println("Every Role set to 0");
			} else {
				System.out.println("Set Disconnect Failed");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static String GetConnectedCountry() {
		String query = "SELECT country from vpnconfig WHERE is_connected = 1";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {

					return rs.getString("country");
				} else {
					// No matching row found
					return null;
				}
			}

		} catch (SQLException e) {
			System.err.println("Error retrieving country");
			e.printStackTrace();
			return null;
		}
	}

	public static String GetConnectedIpAddress() {
		String query = "SELECT ip_address from vpnconfig WHERE is_connected = 1";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {

					return rs.getString("ip_address");
				} else {

					return null;
				}
			}

		} catch (SQLException e) {
			System.err.println("Error retrieving Ip");
			e.printStackTrace();
			return null;
		}
	}

	public static String GetConnectedOvpnPath() {
		String query = "SELECT ovpn_file_path FROM vpnconfig WHERE wifi_profile_id = ? AND is_connected = 1";
		int activeWifiProfileId = WifiProfileDAO.getActiveWifiProfileId();
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setInt(1, activeWifiProfileId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {

					return rs.getString("ovpn_file_path");
				} else {

					return null;
				}
			}

		} catch (SQLException e) {
			System.err.println("Error retrieving Ip");
			e.printStackTrace();
			return null;
		}
	}

	public static void DeleteUnresponsiveOvpn(int activeWifiProfileId, String ovpnPath) {
		String query = "DELETE FROM vpnconfig WHERE wifi_profile_id = ? AND ovpn_file_path = ?";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setInt(1, activeWifiProfileId);
			pstmt.setString(2, ovpnPath);

			// Execute the update
			int rowsUpdated = pstmt.executeUpdate();

			if (rowsUpdated > 0) {
				System.out.println("Deleted: " + ovpnPath);
			} else {
				System.out.println("Delete Failed");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void DeleteUnresponsiveOvpnByPath(int activeWifiProfileId, String ovpnPath) {
		String query = "DELETE FROM vpnconfig WHERE wifi_profile_id = ? AND ovpn_file_path = ?";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setInt(1, activeWifiProfileId);
			pstmt.setString(2, ovpnPath);

			// Execute the update
			int rowsUpdated = pstmt.executeUpdate();

			if (rowsUpdated > 0) {
				System.out.println("Deleted: " + ovpnPath);
			} else {
				System.out.println("Delete Failed");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void UpdateOvpnFilePaths(String ovpnFilePath, int wifiProfileId, String ipAddress, String country,
			LocalDateTime now)
			throws SQLException {
		String insertQuery = "UPDATE vpnconfig SET ip_address = ?, country = ?, last_checked = ? WHERE wifi_profile_id = ? AND ovpn_file_path =?";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

			pstmt.setString(1, ipAddress);
			pstmt.setString(2, country);
			pstmt.setObject(3, now);
			pstmt.setInt(4, wifiProfileId);
			pstmt.setString(5, ovpnFilePath);

			int rowsAffected = pstmt.executeUpdate();

			if (rowsAffected > 0) {
				System.out.println(rowsAffected + " row(s) updated successfully.");
			} else {
				System.out.println("No matching row found for update.");
			}

		} catch (SQLException e) {
			System.err.println("Error updating OVPN file paths:");
			e.printStackTrace();
			throw e;
		}
	}

	public static void DeleteAll() {
		String query = "DELETE FROM wifiprofile where wifi_profile_id = 8";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			// Execute the update
			int rowsUpdated = pstmt.executeUpdate();

			if (rowsUpdated > 0) {
				System.out.println("Deleted");
			} else {
				System.out.println("Delete Failed");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static List<String> getUnknownOvpnPaths(int activeWifiProfileId) {
		List<String> unknownOvpnPaths = new ArrayList<>();

		String query = "SELECT ovpn_file_path FROM vpnconfig WHERE wifi_profile_id = ? AND (country = 'unknown' OR ip_address = 'unknown')";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setInt(1, activeWifiProfileId);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				String unknownOvpn = rs.getString("ovpn_file_path");
				unknownOvpnPaths.add(unknownOvpn);
			}

		} catch (SQLException e) {
			System.err.println("Error adding Ovpns");
			e.printStackTrace();
		}

		return unknownOvpnPaths;
	}

	public static List<String> getActiveProfileOvpnPaths(int activeWifiProfileId) {
		List<String> ovpnPaths = new ArrayList<>();

		String query = "SELECT ovpn_file_path FROM vpnconfig WHERE wifi_profile_id = ?";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setInt(1, activeWifiProfileId);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				String ovpnPath = rs.getString("ovpn_file_path");
				ovpnPaths.add(ovpnPath);
			}

		} catch (SQLException e) {
			System.err.println("Error adding Ovpns");
			e.printStackTrace();
		}

		return ovpnPaths;
	}

	public static void refreshAndGenerateEncodedCountries(int wifiProfileId, MainGuiController guiController)
			throws SQLException {
		List<String> countryCodes = getCountries(wifiProfileId);
		CountryCodeConverter.resetCounter();
		replaceWithNULLToEncoded(wifiProfileId);

		for (String countryCode : countryCodes) {
			String Encodedcountry = CountryCodeConverter.counter(countryCode);
			setEncodedCountries(Encodedcountry, countryCode.toUpperCase(), wifiProfileId);
		}
		guiController.Refresh();
	}

}