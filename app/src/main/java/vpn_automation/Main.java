package vpn_automation;

import java.sql.SQLException;
import java.time.LocalDateTime;

import vpn_automation.backend.OvpnFileTester;
import vpn_automation.backend.db.VPNConfigDAO;

// Main class to run the program
public class Main {

	public static void main(String[] args) throws SQLException, Exception {
		// String currentDir =
		// "/home/thiha/Developer/vpn_automation/app/src/main/resources/ovpn_files";
		// OvpnFileModifier modifier = new OvpnFileModifier();
		// OvpnFileTester tester = new OvpnFileTester();

		// modifier.modifyOvpnFiles(currentDir);
		// tester.testOvpnFiles(currentDir);
		// VPNConfigDAO.refreshAndGenerateEncodedCountries(5);
		// String country = IPInfoFetcher.getIPAddress();
		// String ip = IPInfoFetcher.getCountry();
		// System.out.println(country + " " + ip);
		// System.out.println(WifiProfileDAO.GetSearchStatus());
		// System.out.println(WifiProfileDAO.getActiveWifiProfileId());
		// System.out.println(WifiProfileDAO.GetCurrentCountry());
		// WifiProfileDAO.SetSearchStatus();
		// WifiProfileDAO.ResetSearchStatus();
		// VPNConfigDAO.DeleteAll();
		OvpnFileTester.fixUnknownOvpns();
		// VPNConfigDAO.insertOvpnFilePaths("test", 8,
		// IPInfoFetcher.getIPAddress(),
		// "unknown", now);
	}
}
