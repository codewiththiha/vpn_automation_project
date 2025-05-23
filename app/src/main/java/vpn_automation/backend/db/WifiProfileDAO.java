package vpn_automation.backend.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WifiProfileDAO {
	public static void makeWifiProfileForLoggedInUser(int userID, String wifiBrand, String currentIpAddress,
			String originalCountry, String wifiName)
			throws SQLException {
		String insertQuery = "INSERT INTO WifiProfile (user_id, wifi_brand, current_ip_address, original_location, wifi_name) VALUES (?, ?, ?, ?, ?)";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

			pstmt.setInt(1, userID);
			pstmt.setString(2, wifiBrand);
			pstmt.setString(3, currentIpAddress);
			pstmt.setString(4, originalCountry);
			pstmt.setString(5, wifiName);

			// Execute the insert
			pstmt.executeUpdate();

			System.out.println("Wifi profile added Successfully!");
		} catch (SQLException e) {
			System.err.println("Error making wifi profile!");
			e.printStackTrace();
		}
	}

	public static List<Integer> getWifiProfileIds(int userId) {
		List<Integer> profileIds = new ArrayList<>();

		String query = "SELECT wifi_profile_id FROM WifiProfile WHERE user_id = ?";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setInt(1, userId);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				int profileId = rs.getInt("wifi_profile_id");
				profileIds.add(profileId);
			}

		} catch (SQLException e) {
			System.err.println("Error retrieving wifi_profile_ids:");
			e.printStackTrace();
		}

		return profileIds;
	}

	public static List<String> getWifiNames(int userId) {
		List<String> wifiNames = new ArrayList<>();

		String query = "SELECT wifi_name from WifiProfile where user_id = ?";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setInt(1, userId);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				String wifiName = rs.getString("wifi_name");
				wifiNames.add(wifiName);
			}

		} catch (SQLException e) {
			System.err.println("Error adding wifiName");
			e.printStackTrace();
		}

		return wifiNames;
	}

	public static int getActiveWifiProfileId() {
		String query = "SELECT wifi_profile_id FROM WifiProfile WHERE active_profile =1;";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query);
				ResultSet rs = pstmt.executeQuery()) {

			if (rs.next()) {
				// Return the first active wifiProfileID found
				return rs.getInt("wifi_profile_id");
			} else {
				return -1;
			}
		} catch (SQLException e) {
			System.err.println("Error retrieving wifiProfileID:");
			e.printStackTrace();
			return -1;
		}
	}

	public static void setSelectedWifiNameActive(String wifiName) {
		String deactivateQuery = "UPDATE WifiProfile SET active_profile = 0";
		String activateQuery = "UPDATE WifiProfile SET active_profile = 1 WHERE wifi_name = ?";

		try (Connection conn = DBConnection.getConnection()) {
			// Deactivate all profiles first
			try (PreparedStatement pstmt1 = conn.prepareStatement(deactivateQuery)) {
				pstmt1.executeUpdate();
			}

			// Activate only the selected profile
			try (PreparedStatement pstmt2 = conn.prepareStatement(activateQuery)) {
				pstmt2.setString(1, wifiName);
				pstmt2.executeUpdate();
			}

		} catch (SQLException e) {
			System.err.println("Error updating active_profile:");
			e.printStackTrace();
		}
	}

	public static String getActiveWifiProfileName() {
		String query = "SELECT wifi_name from WifiProfile WHERE active_profile = 1";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query);
				ResultSet rs = pstmt.executeQuery()) {

			if (rs.next()) {
				// Return the first active wifiProfileID found
				return rs.getString("wifi_name");
			} else {
				return "WifiProfile";
			}
		} catch (SQLException e) {
			System.err.println("Error retrieving wifiProfileID:");
			e.printStackTrace();
			return "WifiProfile";
		}
	}

	public static String GetCurrentIpAddress() {
		String query = "SELECT current_ip_address FROM WifiProfile where active_profile = 1";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {

					return rs.getString("current_ip_address");
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

	public static String GetCurrentCountry() {
		String query = "SELECT original_location FROM WifiProfile where active_profile = 1";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {

					return rs.getString("original_location");
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
}
