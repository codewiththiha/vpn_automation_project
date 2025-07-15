package vpn_automation.backend;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;

public class SeleniumTest {
	public static void main(String[] args) {
		// Set up Firefox profile with download preferences
		FirefoxProfile profile = new FirefoxProfile();
		profile.setPreference("browser.download.dir", System.getProperty("user.home") + "/Downloads/Selenium");
		profile.setPreference("browser.download.folderList", 2);
		profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/x-openvpn-profile");

		// Configure Firefox options
		FirefoxOptions options = new FirefoxOptions();
		options.setProfile(profile);
		// options.addArguments("--headless"); // Uncomment for headless mode
		options.addArguments("--width=1920");
		options.addArguments("--height=1080");

		// Initialize Firefox driver
		WebDriver driver = new FirefoxDriver(options);
		driver.manage().window().maximize();

		try {
			// Track clicked links
			Set<String> clickedLinks = new HashSet<>();

			// Open VPNGate
			driver.get("https://www.vpngate.net/en/");
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

			while (true) { // Loop to handle stale elements by refreshing the link list
				// Find all target <a> tags
				List<WebElement> links = driver
						.findElements(By.xpath("//a[starts-with(@href, 'do_openvpn.aspx?fqdn=')]"));
				if (links.isEmpty()) {
					System.out.println("No more links found, exiting.");
					break;
				}
				System.out.println("Found " + links.size() + " links.");

				for (WebElement link : links) {
					try {
						// Get href and check for staleness
						String href;
						try {
							href = link.getAttribute("href");
						} catch (StaleElementReferenceException e) {
							System.out.println("Stale element detected, refreshing link list.");
							break; // Exit inner loop to refresh links
						}

						if (href == null || clickedLinks.contains(href)) {
							System.out.println("Skipping null or duplicate link: " + href);
							continue;
						}

						// Scroll into view
						((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
						wait.until(ExpectedConditions.elementToBeClickable(link));

						// Open link in new tab
						new Actions(driver)
								.keyDown(Keys.CONTROL)
								.click(link)
								.keyUp(Keys.CONTROL)
								.perform();

						// Switch to new tab
						ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
						if (tabs.size() < 2) {
							System.out.println("No new tab opened for link: " + href);
							clickedLinks.add(href); // Mark as processed to avoid retrying
							continue;
						}
						driver.switchTo().window(tabs.get(tabs.size() - 1));

						// Wait for download link
						try {
							WebElement downloadLink = wait.until(ExpectedConditions.presenceOfElementLocated(
									By.xpath("//a[starts-with(@href, '/common/openvpn_download.aspx?sid=')]")));
							String downloadUrl = downloadLink.getAttribute("href");
							wait.until(ExpectedConditions.elementToBeClickable(downloadLink)).click();
							System.out.println("Download started: " + downloadUrl);

							// Wait for download to start
							Thread.sleep(2000); // Adjust if needed
						} catch (TimeoutException e) {
							System.out.println("Download link not found for: " + href);
						}

						// Close current tab
						driver.close();

						// Switch back to original tab
						driver.switchTo().window(tabs.get(0));
						clickedLinks.add(href);

					} catch (Exception e) {
						System.out.println("Error processing link: " + (link != null ? link.toString() : "null") + " - "
								+ e.getMessage());
						// Close any new tab if open
						ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
						if (tabs.size() > 1) {
							driver.switchTo().window(tabs.get(tabs.size() - 1));
							driver.close();
							driver.switchTo().window(tabs.get(0));
						}
					}
				}

				// Check if all links are processed
				if (links.stream().allMatch(link -> {
					try {
						return clickedLinks.contains(link.getAttribute("href"));
					} catch (StaleElementReferenceException e) {
						return true; // Treat stale elements as processed to avoid infinite loops
					}
				})) {
					break; // Exit outer loop if all links are processed
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			driver.quit();
		}
	}
}