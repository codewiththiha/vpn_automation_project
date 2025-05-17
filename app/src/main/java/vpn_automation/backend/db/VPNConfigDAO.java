package vpn_automation.backend.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class VPNConfigDAO {

	// Method to insert OVPN file paths into the database
	public static void insertOvpnFilePaths(String ovpnFilePath, int wifiProfileId, String ipAddress, String country)
			throws SQLException {
		String insertQuery = "INSERT INTO VPNConfig (wifi_profile_id, ovpn_file_path, ip_address, country) VALUES (?, ?, ?, ?)";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

			pstmt.setInt(1, wifiProfileId); // wifi_profile_id
			pstmt.setString(2, ovpnFilePath); // ovpn_file_path
			pstmt.setString(3, ipAddress);
			pstmt.setString(4, country);

			// Execute the insert
			pstmt.executeUpdate();

			System.out.println("OVPN file paths inserted successfully.");
		} catch (SQLException e) {
			System.err.println("Error inserting OVPN file paths:");
			e.printStackTrace();
		}
	}
}