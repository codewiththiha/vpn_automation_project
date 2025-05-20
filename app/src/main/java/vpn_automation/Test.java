package vpn_automation;

import java.sql.SQLException;

import vpn_automation.backend.db.UserDAO;

public class Test {
	public static void main(String[] args) throws SQLException {
		int i = UserDAO.getActiveUserId();
		System.out.println(i);
	}
}
