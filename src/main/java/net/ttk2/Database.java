package net.ttk2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database{
    public static Connection conn;
    public String host, database, username, password, server;
    public int port;
    public Database() {
        host = SetConfig.host;
        port = SetConfig.port;
        database = SetConfig.database;
        username = SetConfig.username;
        password = SetConfig.password;
        server = SetConfig.server;
    }

	public void openConnection() throws SQLException, ClassNotFoundException {
        if (conn != null && !conn.isClosed()) {
            return;
        }

        synchronized (this) {
            if (conn != null && !conn.isClosed()) {
                return;
            }
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
        }
    }
}


