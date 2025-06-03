package vpn_automation.backend.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBConnection {
	private static final String URL = "jdbc:mysql://localhost:3306/vpn_automation_project";
	private static final String USER = "vpn_automation";
	private static final String PASSWORD = "1234";

	private static final DataSource dataSource;

	static {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(URL);
		config.setUsername(USER);
		config.setPassword(PASSWORD);
		config.setMaximumPoolSize(10);
		config.setMinimumIdle(2);
		config.setIdleTimeout(30000);
		config.setMaxLifetime(2000000);
		config.setConnectionTestQuery("SELECT 1");

		dataSource = new HikariDataSource(config);
	}

	public static Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	public static boolean testConnection() {
		try (Connection conn = dataSource.getConnection()) {
			return conn != null && !conn.isClosed();
		} catch (SQLException e) {
			return false;
		}
	}
}