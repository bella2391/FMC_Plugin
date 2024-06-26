package net.ttk2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.bukkit.entity.Player;

public class StatusRecord{
    public Connection conn;
    public String host, database, username, password, server;
    public int port;
    public StatusRecord() {
    	host = Test.host;
    	port = Test.port;
    	database = Test.database;
    	username = Test.username;
    	password = Test.password;
    	server = Test.server;
    }
    public String savePlayer(Player p) {
        try {
            openConnection();
            String name = p.getName();
            UUID uuid = p.getUniqueId();
            String sql = "SELECT * FROM minecraft WHERE uuid=? LIMIT 1;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, uuid.toString());
            ResultSet yuyu = ps.executeQuery();
            
            sql = "SELECT * FROM minecraft WHERE name=? LIMIT 1;";
            ps = conn.prepareStatement(sql);
            ps.setString(1, name.toString());
            ResultSet yu = ps.executeQuery();
			//一番最初に登録された名前と一致したら
            if(yuyu.next()) {
            	String mine_name = yuyu.getString("name");
            	String mine_uuid = yuyu.getString("uuid");
            	sql = "UPDATE minecraft SET server=? WHERE uuid=?;";
            	ps = conn.prepareStatement(sql);
            	ps.setString(1, server.toString());
            	ps.setString(2, uuid.toString());
            	ps.executeUpdate();
            	
            	if(yuyu.getBoolean("ban")) {
            		return "ban";
            	}else {
            		if(name==mine_name) {
            			//一番最初に登録された名前と一致したら
            			
            			//ログ追加
            			sql = "INSERT into mine_log (name,uuid,server,`join`) VALUES (?,?,?,?);";
            			ps = conn.prepareStatement(sql);
            			ps.setString(1, name.toString());
            			ps.setString(2, uuid.toString());
            			ps.setString(3, server.toString());
            			ps.setBoolean(4, true);
            			ps.executeUpdate();
            			return "pass";
            		}
            		return "pass";
            	}
            }
            
            //テスト: playersテーブルにname,uuidのセットを入れる
            sql = "INSERT INTO players (name, uuid) VALUES (?, ?);";
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
		return null;
    }
    //MySQLを用いるどのクラスでも、openConnection();を書けば
    //たとえ、MySQLサーバーが落ちた後でも、つなぎなおしてくれる。
    //そういう意味で、on enableクラスに、conn=を定義せんくてもいいかも。
    //まぁ最後の方に、サーバーがついたときにMySQLにアクセスすることになるけど、各サーバー名のカラムにオンラインであることを知らせるTrueをね
	private void openConnection() throws SQLException, ClassNotFoundException {
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


