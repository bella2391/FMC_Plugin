package net.ttk2;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.StringUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class Test extends JavaPlugin {
	private List<String> subcommands = new ArrayList<>(Arrays.asList("potion","medic"));
	//private static final String[] COMMANDS = { "minecraft", "spigot", "bukkit", "google" };
	//public List<String> subcommands = new ArrayList<>(Arrays.asList("fly","potion","reload"));
	//	getConfig().set("count",1);
	//	saveConfig();
    public static String host, database, username, password,server,discord_webhook_url;
    public static int port;
    @Override
    public void onEnable() {
    	// config.ymlが存在しない場合はファイルに出力します。
    	saveDefaultConfig();
    	// config.ymlを読み込みます。
    	SetConfig();
    	// この2つは殆ど定型文の様な物なので覚えておきましょう。
        //ログインメッセージの表示設定
        getServer().getPluginManager().registerEvents(new LoginMsg(), this);
        //開始時のログ出力
        getLogger().info("プラグインが有効になりました。");
    }
    
    @Override
    public void onDisable() {
        //停止時のログ出力
        getLogger().info("プラグインが無効になりました。");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	if (args.length == 0 || !subcommands.contains(args[0].toLowerCase())) {
    		sender.sendMessage(args.length == 0 ? "" : args[0].toLowerCase());
    		return true;
    	}

    	if (!sender.hasPermission("fmc." + args[0])) {
    		sender.sendMessage("access-denied");
    		return true;
    	}

      switch (args[0].toLowerCase()) {
      case "potion":
    	  if(!(sender instanceof Player)) {
    		  sender.sendMessage(ChatColor.RED+"このコマンドはプレイヤーにしか実行できません！");
    		  return true;
    	  }
    	  Player player = (Player) sender;
    	  //エフェクト名を入れてあるか
    	  if(args[1] == null) {
    		  player.sendMessage(ChatColor.RED+"エフェクト名を入力してください。");
    		  return true;
    	  }
    	  if(containsPotionEffectType(args[1])) {
    		  for(Entity entity : player.getNearbyEntities(10,10,10)) {
    			  if(entity instanceof LivingEntity) {
    				  ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.getByName(args[1]),200,5));
    			  }
    		  }
    		  return true;
    	  }
    	  else {
    		  player.sendMessage(ChatColor.RED + "正しいエフェクト名を入力してください。");
    		  return true;
    	  }
      case "medic":
    	  if(!(sender instanceof Player)) {
    		  //if sender is not player
    		  //error occurs if executing on console
    		  sender.sendMessage(ChatColor.GREEN + "このプラグインはプレイヤーでなければ実行できません。");
    		  return true;
    	  }
    	  player = (Player) sender;
    	  //we see sender is player, by doing so, it can substitute player variable for sender
    	  player.setHealth(20.0);
    	  player.sendMessage(ChatColor.GREEN+"傷の手当てが完了しました。");
    	  return true;
      case "fly":
    	  if(!(sender instanceof Player)) {
    		  //if sender is not player
    		  //error occurs if executing on console
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

    @SuppressWarnings("deprecation")
	@Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
    	List<String> ret = new ArrayList<>();

    	switch (args.length) {
    	case 1:
    		for (String subcmd : subcommands) {
    			if (!sender.hasPermission("fmc." + subcmd)) continue;
      
    			ret.add(subcmd);
    		}
    		return StringUtil.copyPartialMatches(args[0].toLowerCase(), ret, new ArrayList<String>());
    	
    	case 2:
    		if (!sender.hasPermission("fmc." + args[0].toLowerCase())) return Collections.emptyList();
    		switch (args[0].toLowerCase()) {
    		case "potion":
    			for (PotionEffectType potion : PotionEffectType.values()) {
    				if (!sender.hasPermission("myplugin.gm." + potion.getName().toLowerCase())) continue;    		   
    		        	ret.add(potion.getName());
    			}
    			return StringUtil.copyPartialMatches(args[1].toLowerCase(), ret, new ArrayList<String>());
    		}
    	}
    	return Collections.emptyList();
    }
  
    //新しくメソッドを定義
    private boolean containsPotionEffectType(String string) {
    	@SuppressWarnings("deprecation")
		PotionEffectType effectType = PotionEffectType.getByName(string);
    	if(effectType == null) return false;
    	return true;
    }

    class LoginMsg implements Listener {
    	@EventHandler
        public void onPlayerJoin(PlayerJoinEvent e) {
            e.getPlayer().sendMessage("Welcome to my world!");
        	Player p = e.getPlayer();
        	e.setJoinMessage(ChatColor.YELLOW+p.getName()+"がサーバーに参加したゾお......オイコラなにこのチャット欄見てんねん。いてかますｿﾞ！！！！");
        	//UUID u = p.getUniqueId();
        	Player player = e.getPlayer();
        	StatusRecord statusRecord = new StatusRecord();
			String joinstatus = statusRecord.savePlayer(player);
        }
    }
    
    public void SetConfig() {
    	FileConfiguration config = getConfig();
        host = config.getString("host");
        port = config.getInt("port");
        database = config.getString("database");
        username = config.getString("user");
        password = config.getString("pass");
        server = config.getString("server");
        discord_webhook_url = config.getString("discord_webhook_url");
    }
}
