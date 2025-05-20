package vpn_automation.backend.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
	public static void registerUser(String userName, String password, String email)
			throws SQLException {
		String insertQuery = "INSERT INTO User (username, password, email) VALUES (?, ?, ?)";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

			pstmt.setString(1, userName);
			pstmt.setString(2, password);
			pstmt.setString(3, email);

			// Execute the insert
			pstmt.executeUpdate();

			System.out.println("Registration Succeed!");
		} catch (SQLException e) {
			System.err.println("Error registering user:");
			e.printStackTrace();
		}
	}

	// Method to get the active user ID
	public static int getActiveUserId() throws SQLException {
		String selectQuery = "SELECT user_id FROM User WHERE login_or_logout = 1";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(selectQuery);
				ResultSet rs = pstmt.executeQuery()) {

			if (rs.next()) {
				// Return the first active user ID found
				return rs.getInt("user_id");
			} else {
				// No active user found
				return -1;
			}
		} catch (SQLException e) {
			System.err.println("Error retrieving active user ID:");
			e.printStackTrace();
			return -1;
		}
	}
}
