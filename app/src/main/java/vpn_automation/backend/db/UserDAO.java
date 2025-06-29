package vpn_automation.backend.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
	public static void registerUser(String userName, String password, String email)
			throws SQLException {
		String insertQuery = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";

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
		String selectQuery = "SELECT user_id FROM user WHERE login_or_logout = 1";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(selectQuery);
				ResultSet rs = pstmt.executeQuery()) {

			if (rs.next()) {
				// Return the first active user ID found
				return rs.getInt("user_id");
			} else {
				// No active user found for case -1 - i need to check whether sql int support
				// negative values!
				return -1;
			}
		} catch (SQLException e) {
			System.err.println("Error retrieving active user ID:");
			e.printStackTrace();
			return -1;
		}
	}

	public static String getActiveUserName() throws SQLException {
		String selectQuery = "SELECT username FROM user WHERE login_or_logout = 1";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(selectQuery);
				ResultSet rs = pstmt.executeQuery()) {

			if (rs.next()) {
				// Return the first active user ID found
				return rs.getString("username");
			} else {
				// No active user found for case -1 - i need to check whether sql int support
				// negative values!
				return null;
			}
		} catch (SQLException e) {
			System.err.println("Error retrieving active user name:");
			e.printStackTrace();
			return null;
		}
	}

	public static String getActiveUserPass() throws SQLException {
		String selectQuery = "SELECT password FROM vpn_automation_project.user WHERE login_or_logout = 1";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(selectQuery);
				ResultSet rs = pstmt.executeQuery()) {

			if (rs.next()) {
				// Return the first active user ID found
				return rs.getString("password");
			} else {
				// No active user found for case -1 - i need to check whether sql int support
				// negative values!
				return null;
			}
		} catch (SQLException e) {
			System.err.println("Error retrieving active user pass");
			e.printStackTrace();
			return null;
		}
	}

	public static int checkLoginCred(String email, String password) throws SQLException {
		String selectQuery = "SELECT user_id FROM vpn_automation_project.user WHERE password = ? AND email = ?";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(selectQuery)) {

			// Set parameters before executing the query
			pstmt.setString(1, password);
			pstmt.setString(2, email);

			// Now execute the query
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("user_id");
				} else {
					return -1; // No user found
				}
			}
		} catch (SQLException e) {
			System.err.println("Error retrieving active user ID:");
			e.printStackTrace();
			return -1;
		}
	}

	public static void setLoginUser(int userID) {
		String query = "UPDATE user SET login_or_logout = 1 where user_id = ?";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setInt(1, userID);

			// Execute the update
			int rowsUpdated = pstmt.executeUpdate();

			if (rowsUpdated > 0) {
				System.out.println("Set user to " + userID);
			} else {
				System.out.println("Set user Failed");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void logoutUser() throws SQLException {
		String query = "UPDATE user SET login_or_logout = 0 where login_or_logout = 1";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {
			int rowsUpdated = pstmt.executeUpdate();

			if (rowsUpdated > 0) {
				System.out.println("Logout Succeeded");

			} else {
				System.out.println("Logout Failed");
			}
		}
	}

	public static List<String> getRegisteredEmails() {
		List<String> registeredEmails = new ArrayList<>();
		String query = "SELECT email FROM user";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				String country = rs.getString("email");
				registeredEmails.add(country);
			}

		} catch (SQLException e) {
			System.err.println("Error retrieving Emails");
			e.printStackTrace();
		}

		return registeredEmails;
	}

	public static void ChangeName(String name) {
		String query = "UPDATE vpn_automation_project.user SET username = ? WHERE login_or_logout = 1";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setString(1, name);
			int rowsUpdated = pstmt.executeUpdate();
			if (rowsUpdated > 0) {
				System.out.println("Change Succeeded");
			} else {
				System.out.println("Change Failed");
			}
		} catch (SQLException e) {
			System.err.println("Error updating username");
			e.printStackTrace();
		}
	}

	public static void ChangePassword(String password) {
		String query = "UPDATE vpn_automation_project.user SET password = ? WHERE login_or_logout = 1";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setString(1, password);
			int rowsUpdated = pstmt.executeUpdate();
			if (rowsUpdated > 0) {
				System.out.println("Change Succeeded");
			} else {
				System.out.println("Change Failed");
			}
		} catch (SQLException e) {
			System.err.println("Error updating password");
			e.printStackTrace();
		}
	}

}
