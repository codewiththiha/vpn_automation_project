package vpn_automation.backend.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class VPNProfileDAO {
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
}
