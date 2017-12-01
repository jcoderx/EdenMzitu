package cn.xudaodao.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbManager {
	private static volatile DbManager instance = null;
	private static final String URI = "jdbc:mysql://123.56.17.161:3306/Eden_mzitu?user=root&password=123456&useUnicode=true&characterEncoding=UTF8";

	private Connection connection = null;

	private DbManager() {
	}

	public static DbManager getInstance() {
		if (instance == null) {
			synchronized (DbManager.class) {
				if (instance == null) {
					instance = new DbManager();
				}
			}
		}
		return instance;
	}

	public void init() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection(URI);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		try {
			if (connection == null || connection.isClosed()) {
				init();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			init();
		}
		return connection;
	}
}
