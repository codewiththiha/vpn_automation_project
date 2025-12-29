# VPN‑Automation‑Project  

*Automate the discovery, testing and management of OpenVPN configurations with a friendly Java‑FX UI.*  

---  
> **⚠️ Project Deprecation Notice**
> This project has been superseded and replaced by a significantly improved version utilizing better technology stacks. Please visit the new repository here: **[https://github.com/codewiththiha/vmate-cli.git](https://github.com/codewiththiha/vmate-cli.git)**
---


## Table of Contents  

1. [Overview](#overview)  
2. [Features](#features)  
3. [Architecture](#architecture)  
4. [Prerequisites](#prerequisites)  
5. [Installation & Setup](#installation--setup)  
   - 5.1 [Database schema](#database-schema)  
   - 5.2 [Configuration file (optional)](#configuration-file-optional)  
6. [Building & Running](#building--running)  
7. [Usage Guide](#usage-guide)  
   - 7.1 [First start – Register / Login](#first-start---register--login)  
   - 7.2 [Create a Wi‑Fi profile](#create-a-wifi-profile)  
   - 7.3 [Populate VPN configs (Progress screen)]#populate-vpn-configs-progress-screen)  
   - 7.4 [Main GUI – Connect / Re‑check / Export](#main-gui---connect--re‑check--export)  
8. [Key source files](#key-source-files)  
9. [Security & Ethical considerations](#security--ethical-considerations)  
10. [Known limitations & Future work](#known-limitations--future-work)  
11. [Contributing](#contributing)  
12. [License](#license)  
13. [Acknowledgements](#acknowledgements)  

---  

## Overview  

`vpn_automation_project` is a **desktop Java application** (Java 11+, JavaFX) that helps a user:

* **Download** OpenVPN configuration files (`*.ovpn`) from the public service *VPNGate* (via Selenium).  
* **Upgrade** the cipher line inside each file to the modern `data-ciphers` syntax.  
* **Test** each configuration by launching the system `openvpn` binary, checking whether the tunnel is established, and capturing the public IP / country (via `curl https://api.ipinfo.io`).  
* **Persist** all information – user accounts, Wi‑Fi profiles, VPN file paths, IP, country, and connection status – in a MySQL database (`vpn_automation_project`).  
* **Present** a responsive UI where the user can:  
  * Register / log in.  
  * Manage multiple Wi‑Fi profiles (brand, name, original location).  
  * Choose a VPN by country, connect, disconnect, re‑check its health, or export selected files.  
  * Enjoy a small visual cue (country‑specific video) while a VPN is active.  

The program is deliberately split into three logical layers:

| Layer | Package | Responsibility |
|------|---------|----------------|
| **GUI** | `vpn_automation.gui` & `vpn_automation.gui.control` | JavaFX views (FXML) and controllers, navigation, dialog windows. |
| **Backend** | `vpn_automation.backend` | File handling, OpenVPN process orchestration, external API calls, Selenium web‑scraping. |
| **Database Access** | `vpn_automation.backend.db` | Thin DAO objects that talk to MySQL using HikariCP connection pooling. |

---  

## Features  

| Feature | Description |
|---------|-------------|
| **User management** | Register, login, change name/password. |
| **Wi‑Fi profile handling** | Store brand, SSID, original public IP & country; switch active profile. |
| **Automatic OVPN retrieval** | Selenium + Firefox driver scrapes *vpngate.net* for free OpenVPN files. |
| **Cipher upgrade** | Replaces deprecated `cipher AES-128-CBC` with modern `data‑ciphers …`. |
| **Connection testing** | Spins up `openvpn` for each file, verifies “Initialization Sequence Completed”, records IP & country. |
| **Result persistence** | All successful configs are saved in `vpnconfig` table with timestamps. |
| **Re‑check** | Periodically re‑test stored configs; delete unresponsive ones. |
| **Export utility** | Copy selected OVPN files to a user‑chosen directory with a descriptive filename (`<encoded_name>_<wifi_name>_<brand>.ovpn`). |
| **Media feedback** | Shows a short video/animation corresponding to the current country (or a loading video). |
| **Responsive UI** | StackPane scaling keeps the interface usable on any screen size. |
| **Graceful shutdown** | Cancels background tasks, kills any running `openvpn` processes, and resets search flags. |

---  

## Architecture  

```
src/main/java
 └─ vpn_automation
     ├─ App.java                // tiny demo entry point
     ├─ Gui.java                // simple JavaFX demo (unused by the real flow)
     ├─ Main.java               // first launch entry – decides which FXML to load
     ├─ RESMain.java            // same logic as Main but in a different package
     ├─ PopUp.java              // tiny demo for a popup dialog
     ├─ Test.java                // prints the active user id (debug)
     ├─ backend/
     │    ├─ CountryCodeConverter.java   // ISO‑2 ↔ name, video mapping, counter
     │    ├─ FileUtils.java              // helper for .ovpn file discovery & I/O
     │    ├─ IPInfoFetcher.java          // curl → ipinfo.io, cached result
     │    ├─ OvpnFileModifier.java        // cipher replacement
     │    ├─ OvpnFileTester.java         // connection testing + result storage
     │    ├─ Policies.java              // password / email validation
     │    ├─ SeleniumTest.java           // raw Selenium demo (not used by UI)
     │    └─ VPNManager.java             // connect / disconnect / re‑check logic
     ├─ backend/db/
     │    ├─ DBConnection.java           // HikariCP pool (hard‑coded credentials)
     │    ├─ ConnectionTest.java         // quick pool test
     │    ├─ UserDAO.java                // CRUD for `user`
     │    ├─ WifiProfileDAO.java         // CRUD for `wifiprofile`
     │    └─ VPNConfigDAO.java          // CRUD for `vpnconfig`
     └─ gui/
          ├─ NavigationUtils.java         // simple scene switching helper
          ├─ Main2.java                  // an older version of the entry point
          └─ control/
               ├─ ChangeName.java
               ├─ ChangePassword.java
               ├─ ErrorDialog.java
               ├─ ExportUIController.java
               ├─ LoginController.java
               ├─ LoginOrRegisterController.java
               ├─ MainGuiController.java
               ├─ NewProfile.java
               ├─ RegisterController.java
               ├─ StartupOnlyGui.java
               ├─ StartupOnlyProgressingGui.java
               └─ WarningDialog.java
```

All **FXML files** live under `src/main/resources/fxml_files/` and **media assets** under
`src/main/resources/assets/`.  

---  

## Prerequisites  

| Item | Minimum version / notes |
|------|------------------------|
| **Java** | JDK 11 or newer (JavaFX modules are required). |
| **JavaFX** | Use the `javafx-controls`, `javafx-fxml`, `javafx-media`, `javafx-web` modules (Gradle will pull them automatically). |
| **MySQL** | Server reachable at `localhost:3306` (default in `DBConnection`). Create a schema named `vpn_automation_project`. |
| **Database user** | `vpn_automation` with password `1234` (change in `DBConnection` or provide a custom config – see *Configuration file* below). |
| **OpenVPN** | The `openvpn` binary must be in the system `$PATH` (Linux/macOS). On Windows you may need to give the full path in `VPNManager`. |
| **cURL** | Required by `IPInfoFetcher` (`curl -s https://api.ipinfo.io/...`). |
| **Firefox + geckodriver** | Needed by Selenium (`SeleniumTest`). Ensure `geckodriver` is on `$PATH` or set `webdriver.gecko.driver` JVM property. |
| **Gradle** | 7.x (wrapper scripts `gradlew`/`gradlew.bat` are included). |

---  

## Installation & Setup  

1. **Clone the repository**  

   ```bash
   git clone https://github.com/your‑username/vpn_automation_project.git
   cd vpn_automation_project
   ```

2. **Create the MySQL schema**  

   ```sql
   CREATE DATABASE vpn_automation_project CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

   USE vpn_automation_project;

   CREATE TABLE user (
       user_id          INT AUTO_INCREMENT PRIMARY KEY,
       username         VARCHAR(100) NOT NULL,
       password         VARCHAR(255) NOT NULL,   -- ⚠ stored plaintext in this version
       email            VARCHAR(150) NOT NULL UNIQUE,
       login_or_logout  TINYINT DEFAULT 0,       -- 1 = logged‑in
       ovpn_path       VARCHAR(500),
       export_path      VARCHAR(500)
   );

   CREATE TABLE wifiprofile (
       wifi_profile_id   INT AUTO_INCREMENT PRIMARY KEY,
       user_id           INT NOT NULL,
       wifi_brand        VARCHAR(100),
       current_ip_address VARCHAR(45),
       original_location VARCHAR(100),
       wifi_name         VARCHAR(100),
       active_profile    TINYINT DEFAULT 0,
       search_status     TINYINT DEFAULT 0,  -- 0=idle, 1=searching, 2=re‑checking
       FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
   );

   CREATE TABLE vpnconfig (
       config_id        INT AUTO_INCREMENT PRIMARY KEY,
       wifi_profile_id  INT NOT NULL,
       ovpn_file_path  VARCHAR(500) NOT NULL,
       ip_address      VARCHAR(45),
       country         VARCHAR(5),
       last_checked    DATETIME,
       encoded_names   VARCHAR(100),
       is_connected    TINYINT DEFAULT 0,
       FOREIGN KEY (wifi_profile_id) REFERENCES wifiprofile(wifi_profile_id) ON DELETE CASCADE
   );
   ```

3. **(Optional) Provide a configuration file**  

   The project currently hard‑codes DB credentials and the `ipinfo.io` token in source files.  
   For a more secure setup, create a `src/main/resources/application.properties` (or any file you prefer) containing:

   ```properties
   db.url=jdbc:mysql://localhost:3306/vpn_automation_project
   db.user=vpn_automation
   db.password=1234
   ipinfo.token=YOUR_TOKEN_HERE
   ```

   Then modify `DBConnection` and `IPInfoFetcher` to read those properties via `java.util.Properties`. This is highly recommended before distribution.  

4. **Install required system tools**  

   ```bash
   # Debian/Ubuntu example
   sudo apt-get install openvpn curl firefox-esr
   # Install geckodriver (available from the Ubuntu package or from Mozilla)
   sudo apt-get install geckodriver
   ```

5. **Import the Gradle wrapper** (already in the repo). No extra step needed unless you want to use your own Gradle version.  

---  

## Building & Running  

```bash
# Build the JAR (includes runtime dependencies)
./gradlew clean build

# Run directly from Gradle (uses the Main class)
./gradlew run
```

If you prefer a packaged executable:

```bash
java -jar build/libs/app.jar
```

> **Tip:** On macOS/Linux you may need to add `--module-path` and `--add-modules` flags to expose JavaFX modules if you are not using a bundled JDK that already includes them. The Gradle `application` plugin (included in the provided `build.gradle`) takes care of that when you use `./gradlew run`.  

---  

## Usage Guide  

### First start – Register / Login  

1. **Register** (first‑time user):  
   * Fill *First name*, *Last name*, *Email* (must be `@gmail.com`, `@proton.me` or `@email.com`), *Password* (≥ 8 chars, at least one upper‑case & one lower‑case letter).  
   * After successful registration you are sent to the *Startup* page.  

2. **Login** (subsequent runs):  
   * Enter the email and password you used at registration.  
   * If the credentials are correct, the system marks the user as `login_or_logout = 1` and selects the first Wi‑Fi profile (if any).  

---  

### Create a Wi‑Fi profile  

A Wi‑Fi profile records the **brand** (e.g., “asus”) and a **human readable name** (your SSID).  
When you click **Create New Profile** (or the “Add new profile” entry in the combo box):

* The UI automatically fetches your **public IP** and **country** via the `IPInfoFetcher`.  
* It writes a row into `wifiprofile`.  
* The profile becomes the *active* one (`active_profile = 1`).  

You can later switch between profiles via the drop‑down list.  

---  

### Populate VPN configs (Progress screen)  

If the active Wi‑Fi profile has **no VPN configurations** yet, the app shows the *progressing startup* screen:

1. **Cipher upgrade** – each discovered `*.ovpn` file is opened, the old line  
   `cipher AES-128-CBC` is replaced by   
   `data-ciphers AES-256-GCM:AES-128-GCM:CHACHA20-POLY1305:AES-128-CBC`.  

2. **Testing** – each file is launched with `openvpn`. If the output contains `Initialization Sequence Completed` within **20 s**, the file is deemed *working*. The program then:  
   * Calls `IPInfoFetcher` again (clears cache) to obtain the VPN‑exit IP & country.  
   * Inserts a row into `vpnconfig` (stores path, IP, country, timestamp).  

3. **Result** – after all files are processed the “Continue” button becomes visible. Clicking it brings you to the **Main GUI**.  

---  

### Main GUI – Connect / Re‑check / Export  

| UI Element | What it does |
|------------|---------------|
| **Active Wi‑Fi profile label** | Shows the name of the currently selected Wi‑Fi profile. |
| **ComboBoxes** (Wi‑Fi profile / VPN country) | Choose which Wi‑Fi profile to use, and which VPN (by encoded country name) to connect to. |
| **Connect / Disconnect button** | Starts a background task that runs `openvpn` with the selected config. UI updates with “Connecting…”, “Connected”, or error messages. |
| **Re‑check button** | Re‑tests every stored OVPN file for the active profile. Unresponsive files are removed from the DB. |
| **Export button** | Opens a file‑chooser, then copies the selected OVPN files (chosen via check‑boxes) to the target directory, renaming them to `<encoded>_<wifi_name>_<brand>.ovpn`. |
| **MediaView** | Plays a short video for the current country (or a generic loading video while testing). |
| **Change name / password** | Opens dedicated dialogs that update the `user` table. |
| **Log out** | Disconnects any active VPN, clears the `login_or_logout` flag, and returns to the Login/Registration screen. |

All long‑running actions (search, re‑check, connect) are performed on **JavaFX `Task`s** to keep the UI responsive.  

---  

## Key source files  

| Class | Responsibility |
|-------|----------------|
| `vpn_automation.backend.IPInfoFetcher` | Executes `curl` to the *ipinfo.io* API, caches the JSON, supplies `ip` and `country_code`. |
| `vpn_automation.backend.FileUtils` | Scans a directory for `*.ovpn`, reads/writes text files, checks whether a file has already been *checked* for the current Wi‑Fi profile. |
| `vpn_automation.backend.OvpnFileModifier` | Performs the cipher line replacement on each file. |
| `vpn_automation.backend.OvpnFileTester` | Starts an OpenVPN process per file, watches stdout for success, stores results, handles timeout, supports re‑check and cancellation. |
| `vpn_automation.backend.VPNManager` | High‑level API used by the UI: `connectVpn`, `disconnectVpn`, `recheckTheOvpns`, plus cancellation flags. |
| `vpn_automation.backend.db.*DAO` | Thin wrappers around the MySQL tables (`user`, `wifiprofile`, `vpnconfig`). |
| `vpn_automation.gui.control.MainGuiController` | The heart of the GUI – wires UI components to the backend, launches background tasks, updates status labels, manipulates the media player. |
| `vpn_automation.gui.NavigationUtils` | Simple scene‑switching helper (loads FXML into the existing stage). |
| `vpn_automation.backend.Policies` | Password and email validation utilities (used during registration and password changes). |
| `vpn_automation.backend.CountryCodeConverter` | Maps ISO‑2 country codes to full names, to video assets, and provides a per‑run counter to generate unique *encoded* names (`Japan1`, `Japan2`, …). |

---  

## Security & Ethical considerations  

### 1. **User credentials are stored in plain text**  
The current `UserDAO` writes the raw password to the `user.password` column. This is **not suitable for production**.  

- **Mitigation** – store a salted hash with a strong algorithm such as `bcrypt` or `argon2`.  
- **Recommendation** – replace the `registerUser`, `ChangePassword` and login query with libraries like `jBCrypt`.  

### 2. **Hard‑coded database credentials & API token**  
`DBConnection` contains a clear‑text username/password. `IPInfoFetcher` contains an `ipinfo.io` token.  

- **Mitigation** – externalise all secrets to environment variables or a properties file that is **not** committed to version control.  
- Use a `java.security`‑compatible `KeyStore` if higher assurance is needed.  

### 3. **Use of external binaries (`openvpn`, `curl`, `killall`)**  
- The application invokes system commands via `ProcessBuilder`.  
- On Windows you must adapt the kill command (e.g., `taskkill /F /IM openvpn.exe`).  
- Ensure the binary you call is **trusted** and that the user understands the permissions required (e.g., root on Linux for routing).  

### 4. **Legal & policy compliance**  
- **VPNGate** provides free public VPN servers for research and personal use. The terms of service state that the service **must not** be used for illegal activities or for mass‑distribution of the configurations.  
- This tool **downloads** those configurations automatically – you must **respect the original providers** and **only use the files for lawful personal traffic**.  
- Do *not* redistribute the `.ovpn` files or the IP information to third parties.  

### 5. **Network safety**  
- Automatically connecting to arbitrary public VPN endpoints can expose you to malicious exit nodes. Users should verify trustworthiness of a server (e.g., check DNS leak protection).  
- The tool does **not** perform any validation of the certificate chains in the `.ovpn` files; it merely checks that the tunnel can be established.  

### 6. **Responsible disclosure**  
If you discover a vulnerable server or a configuration that leaks data, consider contacting the operator or the VPNGate maintainers.  

---  

## Known limitations & Future work  

| Limitation | Suggested improvement |
|------------|-----------------------|
| Passwords stored plain‑text. | Implement BCrypt hashing, add password‑reset flow with salted tokens. |
| DB credentials & API token hard‑coded. | Load from external config, support multiple environments (dev/prod). |
| `killall openvpn` works only on Unix‑like OSes. | Detect OS at runtime and issue the appropriate command (`taskkill` for Windows). |
| No unit‑tests for most DAO / service classes (only `AppTest`). | Add integration tests with an in‑memory H2/MySQL docker container. |
| UI only works with the default OpenVPN installation path. | Allow the user to specify the exact path to `openvpn` binary. |
| Selenium driver is Firefox‑only and requires a visible browser. | Switch to headless mode by default, provide a fallback to Chrome/Chromium. |
| Connection timeout is a fixed 20 s. | Make timeout configurable, expose to UI. |
| No DNS leak check after a tunnel is up. | Use a simple external IP resolver (e.g., `https://dnsleaktest.com`) to verify isolation. |
| Country video assets limited to a few countries. | Expand the `countryVideoMap` or make videos optional. |
| No logging framework (uses `System.out.println`). | Integrate `SLF4J` + `Logback` for configurable log levels. |
| No graceful handling of `openvpn` password prompts. | Detect `auth-user-pass` directive and feed a password through Process I/O if needed. |
| The UI does not prevent the user from closing the window while a VPN is active. | Add a confirmation dialog on `stage.setOnCloseRequest`. |

---  

## Contributing  

1. **Fork** the repository.  
2. Create a feature branch (`git checkout -b feature/awesome‑thing`).  
3. Make your changes, **add unit / integration tests** where appropriate.  
4. Run `./gradlew test` – the test suite must pass.  
5. Open a **Pull Request** with a clear description of the change and any required migration steps.  

Please follow the existing coding style (spaces, Javadoc for public methods, `snake_case` for DB columns, `camelCase` for Java).  

---  

## License  

The project is released under the **MIT License** – see the `LICENSE` file in the repository.  

---  

## Acknowledgements  

* **VPNGate** – source of the free OpenVPN configuration files.  
* **ipinfo.io** – provides the public IP / country lookup API (used under the free tier).  
* **OpenVPN** – the core tunnelling software.  
* **JavaFX** – UI toolkit.  
* **Selenium** – for web‑scraping the VPNGate site.  
* **HikariCP** – efficient JDBC connection pooling.  
* **geckodriver** – Firefox WebDriver for Selenium.  

---  

*Happy (and responsible) VPN automation!*  
