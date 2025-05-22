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
}
