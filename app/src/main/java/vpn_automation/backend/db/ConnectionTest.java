package vpn_automation.backend.db;

public class ConnectionTest {
	public static void main(String[] args) {
		if (DBConnection.testConnection()) {
			System.out.println("Successful");
		} else {
			System.out.println("Failed!");
		}
	}
}
