package net.ttk2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.configuration.file.FileConfiguration;
//public class EventListener extends JavaPlugin implements Listener,Plugin{
//public final class EventListener extends Main implements Listener{
public final class EventListener implements Listener{
    public Connection conn;
    public String host, database, username, password, server, discord_webhook_url;
    public int port;
    public FileConfiguration config;
    public Main plugin;
    
	public EventListener(Main plugin){
		this.plugin = plugin;
		Database db = new Database();
		try {
			db.openConnection();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}
    
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		String name = p.getName();
		UUID uuid = p.getUniqueId();
	}
	
	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
    	
        e.getPlayer().sendMessage("Welcome to my world!");
    	Player p = e.getPlayer();
    	String name = p.getName();
    	UUID uuid = p.getUniqueId();
    	e.setJoinMessage(ChatColor.YELLOW+name+"がサーバーに参加したゾお......オイコラなにこのチャット欄見てんねん。いてかますｿﾞ！！！！");
    	
        try {
        	conn = Database.conn; 
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
            	ps.setString(1, SetConfig.server.toString());
            	ps.setString(2, uuid.toString());
            	ps.executeUpdate();
            	
            	if(yuyu.getBoolean("ban")) {
            		//
            	}else {
            		if(name.equals(mine_name)) {
            			//一番最初に登録された名前と一致したら
            			
            			//ログ追加
            			sql = "INSERT into mine_log (name,uuid,server,`join`) VALUES (?,?,?,?);";
            			ps = conn.prepareStatement(sql);
            			ps.setString(1, name.toString());
            			ps.setString(2, uuid.toString());
            			ps.setString(3, SetConfig.server.toString());
            			ps.setBoolean(4, true);
            			ps.executeUpdate();
            			if(!SetConfig.discord_webhook_url.isEmpty()) {
                    		DiscordWebhook webhook = new DiscordWebhook(SetConfig.discord_webhook_url);
                    		webhook.setContent(name+"が参加したぜよ！(uuidは"+uuid+")");
                    		try {
                    		    webhook.execute(); //Handle exception
                    		}    		    
                    		catch (java.io.IOException e1){
                    			this.plugin.getLogger().severe(e1.getStackTrace().toString());
                    		}      				
            			}
            		}
            	}
            }
            
            sql = "INSERT INTO players (name, uuid) VALUES (?, ?);";
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            
        } catch (SQLException e3) {
            e3.printStackTrace();
        }
    }
}
