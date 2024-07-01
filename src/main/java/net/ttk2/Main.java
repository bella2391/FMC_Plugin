package net.ttk2;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import java.sql.SQLException;

public class Main extends JavaPlugin implements PluginMessageListener 
{
    public String host, database, username, password,server,discord_webhook_url;
    public int port;
    public FileConfiguration config;
    private Command commands = new Command(this);
    
	@Override
    public void onEnable()
    {
    	saveDefaultConfig();
    	FileConfiguration config = getConfig();
    	new SetConfig(config);
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        
        this.getCommand("fmc").setExecutor(commands);
        
		Database m = new Database();
		try
		{
			m.openConnection();
		}
		catch (ClassNotFoundException | SQLException e)
		{
			e.printStackTrace();
		}
		
        getLogger().info("プラグインが有効になりました。");
        
		DiscordWebhook webhook = new DiscordWebhook(SetConfig.discord_webhook_url);
	    webhook.addEmbed(new DiscordWebhook.EmbedObject().setDescription("サーバーがまもなく起動します。"));
	    try
	    {
	    	webhook.execute();
	    }
	    catch (java.io.IOException e)
	    {
	    	getLogger().severe(e.getStackTrace().toString());
	    }
	    
        checkIfBungee();
        if(!getServer().getPluginManager().isPluginEnabled( this ))
        {
            return;
        }
        getServer().getMessenger().registerIncomingPluginChannel( this, "my:channel", this );
    }
    
    @Override
    public void onDisable()
    {
        getLogger().info("プラグインが無効になりました。");
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }
    
    private void checkIfBungee()
    {
        if ( !getServer().spigot().getConfig().getConfigurationSection("settings").getBoolean( "bungeecord" ) )
        {
            getLogger().severe( "This server is not BungeeCord." );
            getLogger().severe( "If the server is already hooked to BungeeCord, please enable it into your spigot.yml aswell." );
            getLogger().severe( "Plugin disabled!" );
            getServer().getPluginManager().disablePlugin( this );
        }
    }
    
    @Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message)
	{
    	getLogger().info("Main.onPluginMessageReceived");
        if( !channel.equalsIgnoreCase( "my:channel" ) )
        {
        	getLogger().info("メッセージ内容に、my:channelが含まれていません。");
            return;
        }
	    ByteArrayDataInput in = ByteStreams.newDataInput(message);
	    String subchannel = in.readUTF();
	    if (subchannel.equalsIgnoreCase("MySubChannel"))
	    {
            String data1 = in.readUTF();
            String data2 = in.readUTF();
            getLogger().info("data1: "+data1);
            getLogger().info("data2: "+data2);
	    }
	}
}
