package vpn_automation.backend;

import java.util.HashMap;
import java.util.Map;

public class CountryCodeConverter {

	// Map of ISO 2-letter country codes to full country names
	private static final Map<String, String> countryNameMap = new HashMap<>();
	private static final Map<String, String> countryVideoMap = new HashMap<>();

	private static final Map<String, Integer> COUNTER = new HashMap<>();
	static {
		countryNameMap.put("AF", "Afghanistan");
		countryNameMap.put("AL", "Albania");
		countryNameMap.put("DZ", "Algeria");
		countryNameMap.put("AS", "American Samoa");
		countryNameMap.put("AD", "Andorra");
		countryNameMap.put("AO", "Angola");
		countryNameMap.put("AI", "Anguilla");
		countryNameMap.put("AQ", "Antarctica");
		countryNameMap.put("AG", "Antigua and Barbuda");
		countryNameMap.put("AR", "Argentina");
		countryNameMap.put("AM", "Armenia");
		countryNameMap.put("AW", "Aruba");
		countryNameMap.put("AU", "Australia");
		countryNameMap.put("AT", "Austria");
		countryNameMap.put("AZ", "Azerbaijan");
		countryNameMap.put("BS", "Bahamas");
		countryNameMap.put("BH", "Bahrain");
		countryNameMap.put("BD", "Bangladesh");
		countryNameMap.put("BB", "Barbados");
		countryNameMap.put("BY", "Belarus");
		countryNameMap.put("BE", "Belgium");
		countryNameMap.put("BZ", "Belize");
		countryNameMap.put("BJ", "Benin");
		countryNameMap.put("BM", "Bermuda");
		countryNameMap.put("BT", "Bhutan");
		countryNameMap.put("BO", "Bolivia");
		countryNameMap.put("BA", "Bosnia and Herzegovina");
		countryNameMap.put("BW", "Botswana");
		countryNameMap.put("BV", "Bouvet Island");
		countryNameMap.put("BR", "Brazil");
		countryNameMap.put("IO", "British Indian Ocean Territory");
		countryNameMap.put("BN", "Brunei Darussalam");
		countryNameMap.put("BG", "Bulgaria");
		countryNameMap.put("BF", "Burkina Faso");
		countryNameMap.put("BI", "Burundi");
		countryNameMap.put("KH", "Cambodia");
		countryNameMap.put("CM", "Cameroon");
		countryNameMap.put("CA", "Canada");
		countryNameMap.put("CV", "Cape Verde");
		countryNameMap.put("KY", "Cayman Islands");
		countryNameMap.put("CF", "Central African Republic");
		countryNameMap.put("TD", "Chad");
		countryNameMap.put("CL", "Chile");
		countryNameMap.put("CN", "China");
		countryNameMap.put("CX", "Christmas Island");
		countryNameMap.put("CC", "Cocos (Keeling) Islands");
		countryNameMap.put("CO", "Colombia");
		countryNameMap.put("KM", "Comoros");
		countryNameMap.put("CG", "Congo");
		countryNameMap.put("CD", "Congo, Democratic Republic");
		countryNameMap.put("CK", "Cook Islands");
		countryNameMap.put("CR", "Costa Rica");
		countryNameMap.put("CI", "Cote D'Ivoire");
		countryNameMap.put("HR", "Croatia");
		countryNameMap.put("CU", "Cuba");
		countryNameMap.put("CY", "Cyprus");
		countryNameMap.put("CZ", "Czech Republic");
		countryNameMap.put("DK", "Denmark");
		countryNameMap.put("DJ", "Djibouti");
		countryNameMap.put("DM", "Dominica");
		countryNameMap.put("DO", "Dominican Republic");
		countryNameMap.put("EC", "Ecuador");
		countryNameMap.put("EG", "Egypt");
		countryNameMap.put("SV", "El Salvador");
		countryNameMap.put("GQ", "Equatorial Guinea");
		countryNameMap.put("ER", "Eritrea");
		countryNameMap.put("EE", "Estonia");
		countryNameMap.put("ET", "Ethiopia");
		countryNameMap.put("FK", "Falkland Islands (Malvinas)");
		countryNameMap.put("FO", "Faroe Islands");
		countryNameMap.put("FJ", "Fiji");
		countryNameMap.put("FI", "Finland");
		countryNameMap.put("FR", "France");
		countryNameMap.put("GF", "French Guiana");
		countryNameMap.put("PF", "French Polynesia");
		countryNameMap.put("TF", "French Southern Territories");
		countryNameMap.put("GA", "Gabon");
		countryNameMap.put("GM", "Gambia");
		countryNameMap.put("GE", "Georgia");
		countryNameMap.put("DE", "Germany");
		countryNameMap.put("GH", "Ghana");
		countryNameMap.put("GI", "Gibraltar");
		countryNameMap.put("GR", "Greece");
		countryNameMap.put("GL", "Greenland");
		countryNameMap.put("GD", "Grenada");
		countryNameMap.put("GP", "Guadeloupe");
		countryNameMap.put("GU", "Guam");
		countryNameMap.put("GT", "Guatemala");
		countryNameMap.put("GN", "Guinea");
		countryNameMap.put("GW", "Guinea-Bissau");
		countryNameMap.put("GY", "Guyana");
		countryNameMap.put("HT", "Haiti");
		countryNameMap.put("HM", "Heard Island and McDonald Islands");
		countryNameMap.put("VA", "Holy See (Vatican City State)");
		countryNameMap.put("HN", "Honduras");
		countryNameMap.put("HK", "Hong Kong");
		countryNameMap.put("HU", "Hungary");
		countryNameMap.put("IS", "Iceland");
		countryNameMap.put("IN", "India");
		countryNameMap.put("ID", "Indonesia");
		countryNameMap.put("IR", "Iran, Islamic Republic of");
		countryNameMap.put("IQ", "Iraq");
		countryNameMap.put("IE", "Ireland");
		countryNameMap.put("IL", "Israel");
		countryNameMap.put("IT", "Italy");
		countryNameMap.put("JM", "Jamaica");
		countryNameMap.put("JP", "Japan");
		countryNameMap.put("JO", "Jordan");
		countryNameMap.put("KZ", "Kazakhstan");
		countryNameMap.put("KE", "Kenya");
		countryNameMap.put("KI", "Kiribati");
		countryNameMap.put("KP", "Korea");
		countryNameMap.put("KR", "SKorea");
		countryNameMap.put("KW", "Kuwait");
		countryNameMap.put("KG", "Kyrgyzstan");
		countryNameMap.put("LA", "Lao People's Democratic Republic");
		countryNameMap.put("LV", "Latvia");
		countryNameMap.put("LB", "Lebanon");
		countryNameMap.put("LS", "Lesotho");
		countryNameMap.put("LR", "Liberia");
		countryNameMap.put("LY", "Libyan Arab Jamahiriya");
		countryNameMap.put("LI", "Liechtenstein");
		countryNameMap.put("LT", "Lithuania");
		countryNameMap.put("LU", "Luxembourg");
		countryNameMap.put("MO", "Macao");
		countryNameMap.put("MK", "North Macedonia (formerly Macedonia)");
		countryNameMap.put("MG", "Madagascar");
		countryNameMap.put("MW", "Malawi");
		countryNameMap.put("MY", "Malaysia");
		countryNameMap.put("MV", "Maldives");
		countryNameMap.put("ML", "Mali");
		countryNameMap.put("MT", "Malta");
		countryNameMap.put("MH", "Marshall Islands");
		countryNameMap.put("MQ", "Martinique");
		countryNameMap.put("MR", "Mauritania");
		countryNameMap.put("MU", "Mauritius");
		countryNameMap.put("YT", "Mayotte");
		countryNameMap.put("MX", "Mexico");
		countryNameMap.put("FM", "Micronesia, Federated States of");
		countryNameMap.put("MD", "Moldova, Republic of");
		countryNameMap.put("MC", "Monaco");
		countryNameMap.put("MN", "Mongolia");
		countryNameMap.put("MS", "Montserrat");
		countryNameMap.put("MA", "Morocco");
		countryNameMap.put("MZ", "Mozambique");
		countryNameMap.put("MM", "Myanmar");
		countryNameMap.put("NA", "Namibia");
		countryNameMap.put("NR", "Nauru");
		countryNameMap.put("NP", "Nepal");
		countryNameMap.put("NL", "Netherlands");
		countryNameMap.put("AN", "Netherlands Antilles");
		countryNameMap.put("NC", "New Caledonia");
		countryNameMap.put("NZ", "New Zealand");
		countryNameMap.put("NI", "Nicaragua");
		countryNameMap.put("NE", "Niger");
		countryNameMap.put("NG", "Nigeria");
		countryNameMap.put("NU", "Niue");
		countryNameMap.put("NF", "Norfolk Island");
		countryNameMap.put("MP", "Northern Mariana Islands");
		countryNameMap.put("NO", "Norway");
		countryNameMap.put("OM", "Oman");
		countryNameMap.put("PK", "Pakistan");
		countryNameMap.put("PW", "Palau");
		countryNameMap.put("PS", "Palestinian Territory, Occupied");
		countryNameMap.put("PA", "Panama");
		countryNameMap.put("PG", "Papua New Guinea");
		countryNameMap.put("PY", "Paraguay");
		countryNameMap.put("PE", "Peru");
		countryNameMap.put("PH", "Philippines");
		countryNameMap.put("PN", "Pitcairn");
		countryNameMap.put("PL", "Poland");
		countryNameMap.put("PT", "Portugal");
		countryNameMap.put("PR", "Puerto Rico");
		countryNameMap.put("QA", "Qatar");
		countryNameMap.put("RE", "Reunion");
		countryNameMap.put("RO", "Romania");
		countryNameMap.put("RU", "Russian Federation");
		countryNameMap.put("RW", "Rwanda");
		countryNameMap.put("SH", "Saint Helena");
		countryNameMap.put("KN", "Saint Kitts and Nevis");
		countryNameMap.put("LC", "Saint Lucia");
		countryNameMap.put("PM", "Saint Pierre and Miquelon");
		countryNameMap.put("VC", "Saint Vincent and the Grenadines");
		countryNameMap.put("WS", "Samoa");
		countryNameMap.put("SM", "San Marino");
		countryNameMap.put("ST", "Sao Tome and Principe");
		countryNameMap.put("SA", "Saudi Arabia");
		countryNameMap.put("SN", "Senegal");
		countryNameMap.put("SC", "Seychelles");
		countryNameMap.put("SL", "Sierra Leone");
		countryNameMap.put("SG", "Singapore");
		countryNameMap.put("SK", "Slovakia");
		countryNameMap.put("SI", "Slovenia");
		countryNameMap.put("SB", "Solomon Islands");
		countryNameMap.put("SO", "Somalia");
		countryNameMap.put("ZA", "South Africa");
		countryNameMap.put("GS", "South Georgia and the South Sandwich Islands");
		countryNameMap.put("ES", "Spain");
		countryNameMap.put("LK", "Sri Lanka");
		countryNameMap.put("SD", "Sudan");
		countryNameMap.put("SR", "Suriname");
		countryNameMap.put("SJ", "Svalbard and Jan Mayen");
		countryNameMap.put("SZ", "Swaziland");
		countryNameMap.put("SE", "Sweden");
		countryNameMap.put("CH", "Switzerland");
		countryNameMap.put("SY", "Syrian Arab Republic");
		countryNameMap.put("TW", "Taiwan, Province of China");
		countryNameMap.put("TJ", "Tajikistan");
		countryNameMap.put("TZ", "Tanzania, United Republic of");
		countryNameMap.put("TH", "Thailand");
		countryNameMap.put("TL", "Timor-Leste");
		countryNameMap.put("TG", "Togo");
		countryNameMap.put("TK", "Tokelau");
		countryNameMap.put("TO", "Tonga");
		countryNameMap.put("TT", "Trinidad and Tobago");
		countryNameMap.put("TN", "Tunisia");
		countryNameMap.put("TR", "Turkey");
		countryNameMap.put("TM", "Turkmenistan");
		countryNameMap.put("TC", "Turks and Caicos Islands");
		countryNameMap.put("TV", "Tuvalu");
		countryNameMap.put("UG", "Uganda");
		countryNameMap.put("UA", "Ukraine");
		countryNameMap.put("AE", "United Arab Emirates");
		countryNameMap.put("GB", "United Kingdom");
		countryNameMap.put("US", "United States");
		countryNameMap.put("UM", "United States Minor Outlying Islands");
		countryNameMap.put("UY", "Uruguay");
		countryNameMap.put("UZ", "Uzbekistan");
		countryNameMap.put("VU", "Vanuatu");
		countryNameMap.put("VE", "Venezuela");
		countryNameMap.put("VN", "Vietnam");
		countryNameMap.put("VG", "Virgin Islands, British");
		countryNameMap.put("VI", "Virgin Islands, U.S.");
		countryNameMap.put("WF", "Wallis and Futuna");
		countryNameMap.put("EH", "Western Sahara");
		countryNameMap.put("YE", "Yemen");
		countryNameMap.put("ZM", "Zambia");
		countryNameMap.put("ZW", "Zimbabwe");
		countryNameMap.put("UNKNOWN", "Unknown");
		countryVideoMap.put("JP", "/assets/videos/japan.mp4");
		countryVideoMap.put("RU", "/assets/videos/russia.mp4");
		countryVideoMap.put("KR", "/assets/videos/south_korea.mp4");
		countryVideoMap.put("MM", "/assets/videos/myanmar.mp4");
		countryVideoMap.put("VN", "/assets/videos/vietnam.mp4");
		countryVideoMap.put("TH", "/assets/videos/thailand.mp4");
		countryVideoMap.put("US", "/assets/videos/united_states.mp4");
		countryVideoMap.put("LOAD", "/assets/videos/loading_map.mp4");

	}

	/**
	 * Converts a 2-letter country code to its full name.
	 *
	 * @param code The ISO 3166-1 alpha-2 country code (e.g., "MM").
	 * @return The full country name (e.g., "Myanmar"), or null if not found.
	 */
	public static String getCountryName(String code) {
		return countryNameMap.get(code.toUpperCase());
	}

	/**
	 * Returns a unique country name with an incrementing number for each call.
	 *
	 * @param code The country code (e.g., "JP")
	 * @return A string like "Japan1", "Japan2", etc.
	 */
	public static String counter(String code) {
		String fullName = getCountryName(code);
		if (fullName == null) {
			fullName = "Unknown";
		}

		// Get current count or default to 0, then add 1
		int count = COUNTER.getOrDefault(fullName, 0) + 1;

		// Update the counter
		COUNTER.put(fullName, count);

		// Return the result
		return fullName + count;
	}

	/**
	 * Resets all counters (useful between test runs or reprocessing)
	 */
	public static void resetCounter() {
		COUNTER.clear();
	}

	public static String getCountryVideo(String code) {
		return countryVideoMap.get(code.toUpperCase());
	}
}