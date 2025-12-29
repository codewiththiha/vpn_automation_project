# VPN Automation Project

> **⚠️ Project Deprecation Notice**
> This project has been superseded and replaced by a significantly improved version utilizing better technology stacks. Please visit the new repository here: **[https://github.com/codewiththiha/vmate-cli.git](https://github.com/codewiththiha/vmate-cli.git)**

---

## Overview

This project was a Java-based desktop application designed to automate the management, testing, and connection of VPN configurations. It utilized a graphical user interface (GUI) to batch-test OpenVPN files scraped from public sources (specifically VPNGate), verify their connectivity, and manage user profiles via a local database.

## Key Features

*   **Automated VPN Testing:** Automatically iterates through directories of `.ovpn` files, tests their connectivity using the local `openvpn` client, and identifies working configurations within a set timeout.
*   **Web Scraping (Selenium):** Integrates with **Selenium WebDriver** to automate the process of discovering and downloading VPN configurations from VPNGate using the Firefox browser.
*   **Configuration Optimization:** Includes a utility to modify and update cipher suites in `.ovpn` files to modern standards (e.g., replacing AES-128-CBC with AES-256-GCM).
*   **Profile Management:** A GUI-driven system to manage WiFi profiles and associate specific VPN configurations with different network environments.
*   **Real-time IP Monitoring:** Fetches and validates current IP address and geolocation data upon successful connection using external APIs.
*   **Responsive GUI:** Built with **JavaFX**, featuring a responsive scaling layout to adapt to different window sizes.

## Architecture & Tech Stack

*   **Language:** Java
*   **GUI Framework:** JavaFX (with FXML)
*   **Automation:** Selenium WebDriver (Firefox)
*   **Database:** SQLite (via JDBC)
*   **Build Tool:** Gradle
*   **External Dependencies:**
    *   System `openvpn` binary
    *   `curl` for IP fetching
    *   GeckoDriver (for Selenium)

## Project Structure

The application is divided into three main layers:

1.  **Presentation Layer (`vpn_automation.gui`):**
    *   Handles the User Interface logic.
    *   Controllers for the main dashboard, registration forms, and progress tracking.
    *   Manages responsive scaling of UI components.

2.  **Business Logic Layer (`vpn_automation.backend`):**
    *   `VPNManager`: Manages the lifecycle of VPN connections (connect, disconnect, monitor logs).
    *   `OvpnFileTester`: Core logic for batch testing VPN files with timeouts and error handling.
    *   `SeleniumTest`: Headless or visible browser automation to scrape VPN lists.
    *   `OvpnFileModifier`: String manipulation to update security parameters in config files.
    *   `IPInfoFetcher`: Network utility to verify connection changes.
    *   `FileUtils`: File I/O operations.

3.  **Data Access Layer (`vpn_automation.backend.db`):**
    *   `UserDAO`: Manages user registration and authentication.
    *   `WifiProfileDAO`: Handles network profiles.
    *   `VPNConfigDAO`: Stores working VPN paths, IP history, and connection statuses.

## Prerequisites (For Legacy Use)

If you are attempting to run this specific version of the code, ensure the following are installed on your machine:

1.  **Java Development Kit (JDK)** (Version 11 or higher recommended).
2.  **OpenVPN:** The `openvpn` command must be available in the system PATH.
3.  **Mozilla Firefox:** Required for the Selenium scraping component.
4.  **GeckoDriver:** Must match the installed Firefox version and be in the PATH.

## Installation & Usage (Legacy)

1.  **Clone the Repository:**
    ```bash
    git clone [repository-url]
    cd vpn_automation_project
    ```

2.  **Build with Gradle:**
    ```bash
    ./gradlew build
    ```

3.  **Run the Application:**
    The main entry point is `vpn_automation.Main`.
    ```bash
    ./gradlew run
    ```

4.  **Workflow:**
    *   **Register:** Create a user account via the GUI.
    *   **Create Profile:** Add a WiFi profile for your current network.
    *   **Search/Download:** Use the integrated tools to scrape `.ovpn` files.
    *   **Test:** Run the batch tester to filter out non-responsive servers.
    *   **Connect:** Select a working config from the list to establish a secure connection.

## Ethical Note

This tool was developed for educational and network management purposes. The web scraping module targets `vpngate.net`. Users are responsible for ensuring that their use of automated scraping tools complies with the Terms of Service of the target websites and local regulations regarding VPN usage.

---

*For continued support and updated features, please switch to the [vmate-cli](https://github.com/codewiththiha/vmate-cli.git) project.*
