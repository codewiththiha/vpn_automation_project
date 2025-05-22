package vpn_automation.backend.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class VPNConfigDAO {

	// Method to insert OVPN file paths into the database
	public static void insertOvpnFilePaths(String ovpnFilePath, int wifiProfileId, String ipAddress, String country,
			LocalDateTime now)
			throws SQLException {
		String insertQuery = "INSERT INTO VPNConfig (wifi_profile_id, ovpn_file_path, ip_address, country, last_checked) VALUES (?, ?, ?, ?, ?)";

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
		String query = "SELECT config_id FROM VPNConfig WHERE wifi_profile_id = ?";
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
		String query = "SELECT ovpn_file_path FROM vpn_automation_project.VPNConfig WHERE wifi_profile_id = ?  AND last_checked > NOW() - INTERVAL 3 DAY";
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
}