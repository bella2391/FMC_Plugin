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

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import septogeddon.pluginquery.PluginQuery;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryMessenger;

import org.bukkit.configuration.file.FileConfiguration;

public final class EventListener implements Listener
{
    public Connection conn;
    public String host, database, username, password, server, discord_webhook_url;
    public int port;
    public FileConfiguration config;
    public Main plugin;
    
	public EventListener(Main plugin)
	{
		this.plugin = plugin;
		Database db = new Database();
		try
		{
			db.openConnection();
		}
		catch (ClassNotFoundException | SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		Player p = e.getPlayer();
		String name = p.getName();
		UUID uuid = p.getUniqueId();
	}
	
	//PluginQuery
    @org.bukkit.event.EventHandler
    public void event(septogeddon.pluginquery.spigot.event.QueryMessageEvent event)
    {
    	this.plugin.getLogger().info("EventListener.event");
        String channel = event.getChannel();
        byte[] message = event.getMessage();
        QueryConnection connection = event.getConnection();
    	ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String data1 = in.readUTF();
        this.plugin.getLogger().info("channel: "+channel);
        this.plugin.getLogger().info("data1: "+data1);
    }
    
	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
	{
		this.plugin.getLogger().info("EventListener.onPlayerJoin");
        e.getPlayer().sendMessage("Welcome to my world!");
    	Player p = e.getPlayer();
    	String name = p.getName();
    	UUID uuid = p.getUniqueId();
    	e.setJoinMessage(ChatColor.YELLOW+name+"がサーバーに参加したゾお......オイコラなにこのチャット欄見てんねん。いてこますｿﾞ！！！！");
    	ByteArrayDataOutput out = ByteStreams.newDataOutput();
    	out.writeUTF("あいうえおマスター");
    	sendPluginMessage("La_Test2",out.toByteArray());
    	this.plugin.getLogger().info("EventListener.onPlayerJoin.PluginQueryでsendしました。");
        try
        {
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
            if(yuyu.next())
            {
            	String mine_name = yuyu.getString("name");
            	String mine_uuid = yuyu.getString("uuid");
            	sql = "UPDATE minecraft SET server=? WHERE uuid=?;";
            	ps = conn.prepareStatement(sql);
            	ps.setString(1, SetConfig.server.toString());
            	ps.setString(2, uuid.toString());
            	ps.executeUpdate();
            	
            	if(yuyu.getBoolean("ban"))
            	{
            		//
            	}
            	else
            	{
            		if(name.equals(mine_name))
            		{
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
                    		try
                    		{
                    		    webhook.execute(); //Handle exception
                    		}    		    
                    		catch (java.io.IOException e1)
                    		{
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
            
        }
        catch (SQLException e3)
        {
            e3.printStackTrace();
        }
    }
	
	//PluginQueryのoutput
    public void sendPluginMessage(String channel, byte[] message)
    {
        QueryMessenger messenger = PluginQuery.getMessenger();
        /*
         * A spigot server can be connected with multiple connection,
         * it can be a bungeecord server or another standalone program
         */
        if (!messenger.broadcastQuery(channel, message))
        {
            // it will return false if there is no active connections
            throw new IllegalStateException("no active connections");
        }
    }
}
