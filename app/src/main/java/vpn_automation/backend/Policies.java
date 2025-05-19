package vpn_automation.backend;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Policies {

	// Check if password contains at least one uppercase letter
	public static boolean hasUpperCase(String password) {
		for (char c : password.toCharArray()) {
			if (Character.isUpperCase(c)) {
				return true;
			}
		}
		return false;
	}

	// Check if password contains at least one lowercase letter
	public static boolean hasLowerCase(String password) {
		for (char c : password.toCharArray()) {
			if (Character.isLowerCase(c)) {
				return true;
			}
		}
		return false;
	}

	// Check if password contains at least one digit
	public static boolean hasDigit(String password) {
		for (char c : password.toCharArray()) {
			if (Character.isDigit(c)) {
				return true;
			}
		}
		return false;
	}

	// Check if password is at least 8 characters long
	public static boolean hasMinLength(String password) {
		return password.length() >= 8;
	}

	// Main password checker that uses all the above
	public static boolean isPasswordValid(String password) {
		return hasUpperCase(password) &&
				hasLowerCase(password) &&
				hasMinLength(password);
	}

	public static boolean isValidEmail(String email) {
		// Supported domains
		List<String> allowedDomains = Arrays.asList(
				"@gmail.com",
				"@proton.me",
				"@email.com");

		// Basic email regex pattern (simple version)
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
		Pattern pattern = Pattern.compile(emailRegex);
		Matcher matcher = pattern.matcher(email);

		if (!matcher.matches()) {
			return false;
		}

		// Check if the domain part is in the allowed list
		for (String domain : allowedDomains) {
			if (email.endsWith(domain)) {
				return true;
			}
		}

		return false;
	}
}