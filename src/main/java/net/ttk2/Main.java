package net.ttk2;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.StringUtil;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import septogeddon.pluginquery.PluginQuery;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryMessageListener;
import septogeddon.pluginquery.api.QueryMessenger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

//public class Main extends JavaPlugin implements PluginMessageListener
public class Main extends JavaPlugin
{
	private List<String> subcommands = new ArrayList<>(Arrays.asList("reload","potion","medic","fly"));
    public String host, database, username, password,server,discord_webhook_url;
    public int port;
    public FileConfiguration config;
    @Override
    public void onEnable()
    {
    	saveDefaultConfig();
    	FileConfiguration config = getConfig();
    	new SetConfig(config);
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
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
	    
	    //PluginQuery
	    registerListener();
    }
    
    @Override
    public void onDisable()
    {
        getLogger().info("プラグインが無効になりました。");
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }
    
	@Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
    	if (args.length == 0 || !subcommands.contains(args[0].toLowerCase()))
    	{
    		sender.sendMessage(args.length == 0 ? "" : args[0].toLowerCase());
    		return true;
    	}

    	if (!sender.hasPermission("fmc." + args[0]))
    	{
    		sender.sendMessage("access-denied");
    		return true;
    	}

      switch (args[0].toLowerCase())
      {
      case "reload":
    	  //SetConfig();
    	  //saveDefaultConfig();
    	  reloadConfig();
    	  FileConfiguration config = getConfig();
    	  new SetConfig(config);
    	  sender.sendMessage(ChatColor.GREEN+"コンフィグをリロードしました。");
    	  return true;
      case "potion":
    	  if(!(sender instanceof Player))
    	  {
    		  sender.sendMessage(ChatColor.RED+"このコマンドはプレイヤーにしか実行できません！");
    		  return true;
    	  }
    	  Player player = (Player) sender;
    	  //エフェクト名を入れてあるか
    	  if(args[1] == null)
    	  {
    		  player.sendMessage(ChatColor.RED+"エフェクト名を入力してください。");
    		  return true;
    	  }
    	  if(containsPotionEffectType(args[1]))
    	  {
    		  for(Entity entity : player.getNearbyEntities(10,10,10))
    		  {
    			  if(entity instanceof LivingEntity)
    			  {
    				  ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.getByName(args[1]),200,5));
    			  }
    		  }
    		  return true;
    	  }
    	  else
    	  {
    		  player.sendMessage(ChatColor.RED + "正しいエフェクト名を入力してください。");
    		  return true;
    	  }
      case "medic":
    	  if(!(sender instanceof Player))
    	  {
    		  sender.sendMessage(ChatColor.GREEN + "このプラグインはプレイヤーでなければ実行できません。");
    		  return true;
    	  }
    	  player = (Player) sender;
    	  //we see sender is player, by doing so, it can substitute player variable for sender
    	  player.setHealth(20.0);
    	  player.sendMessage(ChatColor.GREEN+"傷の手当てが完了しました。");
    	  return true;
      case "fly":
    	  if(!(sender instanceof Player))
    	  {
    		  sender.sendMessage(ChatColor.GREEN + "このプラグインはプレイヤーでなければ実行できません。");
    		  return true;
    	  }
    	  player = (Player) sender;
    	  //we see sender is player, by doing so, it can substitute player variable for sender
    	  player.setAllowFlight(true);
    	  player.sendMessage(ChatColor.GREEN+"サバイバルで飛べるようになりました。");
    	  return true;
      }
      return true;
    }

    @Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
    {
    	List<String> ret = new ArrayList<>();

    	switch (args.length)
    	{
    	case 1:
    		for (String subcmd : subcommands)
    		{
    			if (!sender.hasPermission("fmc." + subcmd)) continue;
      
    			ret.add(subcmd);
    		}
    		return StringUtil.copyPartialMatches(args[0].toLowerCase(), ret, new ArrayList<String>());
    	
    	case 2:
    		if (!sender.hasPermission("fmc." + args[0].toLowerCase())) return Collections.emptyList();
    		switch (args[0].toLowerCase())
    		{
    		case "potion":
    			for (PotionEffectType potion : PotionEffectType.values())
    			{
    				if (!sender.hasPermission("fmc.potion." + potion.getName().toLowerCase())) continue;    		   
    		        ret.add(potion.getName());
    			}
    			return StringUtil.copyPartialMatches(args[1].toLowerCase(), ret, new ArrayList<String>());
    		}
    	}
    	return Collections.emptyList();
    }
  
    private boolean containsPotionEffectType(String string)
    {
    	@SuppressWarnings("deprecation")
		PotionEffectType effectType = PotionEffectType.getByName(string);
    	if(effectType == null) return false;
    	return true;
    }

    //PluginQueryのinput
    public void registerListener() {
    	getLogger().info("Main.registerListener");
        QueryMessenger messenger = PluginQuery.getMessenger();
        messenger.getEventBus().registerListener(new ExampleListener());
        net.md_5.bungee.api.plugin.Plugin plugin = null; // YOUR BUNGEECORD PLUGIN INSTANCE
    }
     
    //Listen only to Message event
    public class ExampleListener implements QueryMessageListener {
        @Override
        public void onQueryReceived(QueryConnection connection, String channel, byte[] message) {
        	getLogger().info("Main.ExampleListener.onQueryReceived");
        	ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String data1 = in.readUTF();
            getLogger().info("data1: "+data1);
        }
    }
}
