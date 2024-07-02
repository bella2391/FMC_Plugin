package net.ttk2;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

public class Main extends JavaPlugin
{
    public String host, database, username, password,server,discord_webhook_url;
    public int port;
    public FileConfiguration config;
    private Command commands = new Command(this);
    private Thread socketThread;
    private volatile boolean running = true;
    
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
	    
        startSocketServer();
    }
    
    @Override
    public void onDisable()
    {
        getLogger().info("プラグインが無効になりました。");
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
        stopSocketServer();
    }
    
    private void startSocketServer() {
        socketThread = new Thread(() -> {
            int port = 8765;

            try (ServerSocket serverSocket = new ServerSocket(port)) {
                getLogger().info("Server is listening on port " + port);

                while (running) {
                    Socket socket = serverSocket.accept();
                    getLogger().info("New client connected");

                    new SocketServerThread(socket).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        socketThread.start();
    }

    private void stopSocketServer() {
        running = false;
        try {
            if (socketThread != null && socketThread.isAlive()) {
                socketThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
