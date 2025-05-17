package vpn_automation;

import java.sql.SQLException;

import vpn_automation.backend.OvpnFileModifier;
import vpn_automation.backend.OvpnFileTester;

// Main class to run the program
public class Main {

	public static void main(String[] args) throws SQLException, Exception {
		String currentDir = "/home/thiha/Developer/vpn_automation/app/src/main/resources/ovpn_files";
		OvpnFileModifier modifier = new OvpnFileModifier();
		OvpnFileTester tester = new OvpnFileTester();

		modifier.modifyOvpnFiles(currentDir);
		tester.testOvpnFiles(currentDir);
	}
}
