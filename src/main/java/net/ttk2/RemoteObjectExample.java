package net.ttk2;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
 
import org.bukkit.plugin.java.JavaPlugin;
 
import net.md_5.bungee.api.plugin.Plugin;
import septogeddon.pluginquery.PluginQuery;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;
import septogeddon.pluginquery.library.remote.ClassRegistry;
import septogeddon.pluginquery.library.remote.RemoteObject;
import septogeddon.pluginquery.library.remote.Represent;
import septogeddon.pluginquery.library.remote.UnknownObject;
import septogeddon.pluginquery.utils.QueryUtil;

public class RemoteObjectExample
{
    
    public void spigotSide(JavaPlugin plugin)
    {
        // get the only one active bungeecord connection
        QueryConnection connection = QueryUtil.first(PluginQuery.getMessenger().getActiveConnections());
        // create the remote
        RemoteObject<SimpleProxyServer> remoteObject = new RemoteObject<SimpleProxyServer>
        (
        	/* Parameter */
        	QueryContext.REMOTEOBJECT_BUNGEESERVER_CHANNEL, 
        	connection, 
        	SimpleProxyServer.class, 
        	new ClassRegistry()
        );
         
        // get the object from the bungeecord
        try
        {
            SimpleProxyServer server = remoteObject.getObject();
            // use UnknownObject interface, so you don't have to create another empty interface
            UnknownObject lobby = server.getServerInfo("lobby");
            for (SimpleProxiedPlayer player : server.getPlayers())
            {
                player.connect(lobby);
                plugin.getLogger().log(Level.INFO, "Transfered "+player.getName()+" ("+player.getUniqueId()+") to lobby server");
            }
        }
        catch (TimeoutException e)
        {
            // The connection took too long :(
            // perhaps network problem?
            e.printStackTrace();
        }
    }
     
    public void bungeecordSide(Plugin plugin)
    {
        ClassRegistry registry = new ClassRegistry();
        for (QueryConnection connection : PluginQuery.getMessenger().getActiveConnections())
        {
            RemoteObject<SimpleServer> remoteObject= new RemoteObject<>
            (
            		QueryContext.REMOTEOBJECT_BUKKITSERVER_CHANNEL,
                    connection,
                    SimpleServer.class,
                    registry
            );
            try
            {
                SimpleServer server = remoteObject.getObject();
                for (SimplePlayer player : server.getOnlinePlayers())
                {
                    player.kickPlayer("Kicked, bye!");
                }
            }
            catch (TimeoutException e)
            {
                // The connection took too long :(
                // perhaps network problem?
                e.printStackTrace();
            }
        }
    }
     
    @Represent("org.bukkit.Server")
    public static interface SimpleServer
    {
        public String getVersion();
        public Collection<? extends SimplePlayer> getOnlinePlayers();
    }
     
    @Represent("org.bukkit.entity.Player")
    public static interface SimplePlayer
    {
        public void kickPlayer(String kickMessage);
    }
     
    @Represent("net.md_5.bungee.api.ProxyServer")
    public static interface SimpleProxyServer
    {
        public String getVersion();
        public SimpleProxiedPlayer getPlayer(String playerName);
        public SimpleProxiedPlayer getPlayer(UUID playerUUID);
        public Collection<SimpleProxiedPlayer> getPlayers();
        public UnknownObject getServerInfo(String name);
    }
     
    @Represent("net.md_5.bungee.api.connection.ProxiedPlayer")
    public static interface SimpleProxiedPlayer
    {
        public String getName();
        public int getPing();
        public UUID getUniqueId();
        public String getDisplayName();
        public void disconnect(String kickMessage);
        public void connect(UnknownObject server);
    }
}