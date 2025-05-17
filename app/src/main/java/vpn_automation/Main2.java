package vpn_automation;

import vpn_automation.backend.IPInfoFetcher;

public class Main2 {
	public static void main(String[] args) throws Exception {
		String ip = IPInfoFetcher.getIPAddress();
		String country = IPInfoFetcher.getCountry();
		System.out.println("IP: " + ip + "\n" + "Country: " + country);
	}
}
