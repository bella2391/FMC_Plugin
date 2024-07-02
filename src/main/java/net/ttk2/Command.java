package net.ttk2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.StringUtil;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class Command implements CommandExecutor,TabExecutor{
	
	private List<String> subcommands = new ArrayList<>(Arrays.asList("reload","potion","medic","fly","test"));
	public Main plugin;
	
	public Command(Main plugin)
	{
		this.plugin = plugin;
	}
	
	@Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args)
	{
    	if (args.length == 0 || !subcommands.contains(args[0].toLowerCase()))
    	{
    		BaseComponent[] component =
    			    new ComponentBuilder(ChatColor.YELLOW+"FMC COMMANDS LIST").bold(true).underlined(true)
    			    	.append(ChatColor.DARK_AQUA+"\nYou Can Click And Paste!!")
    			    	.append(ChatColor.AQUA+"\n\n/fmc potion <effect type>")
    			        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/fmc potion " ))
    			        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("半径10マスのエンティティにエフェクト付与します！")))
    			        .append(ChatColor.AQUA+"\n\n/fmc fly")
    			        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/fmc fly"))
    			        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("空、飛べるよ！")))
    			        .append(ChatColor.AQUA+"\n\n/fmc reload")
    			        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/fmc reload"))
    			        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("コンフィグ、リロードします！！")))
    			        .append(ChatColor.AQUA+"\n\n/fmc test <arg-1>")
    			        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/fmc test "))
    			        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("第一引数を返します！")))
    			        .append(ChatColor.AQUA+"\n\n/fmc medic")
    			        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/fmc medic"))
    			        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("ライフが回復します！")))
    			        .create();
    		sender.spigot().sendMessage(component);
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
    	  this.plugin.reloadConfig();
    	  FileConfiguration config = this.plugin.getConfig();
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
    	  if(args.length == 1 || Objects.isNull(args[1]) || args[1].isEmpty())
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
      case "test":
    	  if(args.length == 1 || Objects.isNull(args[1]) || args[1].isEmpty())
    	  {
    		  sender.sendMessage("引数を入力してください。");
    		  return true;
    	  }
    	  sender.sendMessage("第1引数: "+args[1]);
    	  return true;  
      }
      return true;
    }

    @Override
	public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args)
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
    			case "test":
    				
    				return StringUtil.copyPartialMatches(args[1].toLowerCase(), ret, new ArrayList<String>());
    		}
    	}
    	return Collections.emptyList();
    }
  
    private boolean containsPotionEffectType(String string)
    {
  	  	if(Objects.isNull(string) || string.isEmpty())
  	  	{
  		  return false;
  	  	}
    	@SuppressWarnings("deprecation")
		PotionEffectType effectType = PotionEffectType.getByName(string);
    	if(effectType == null) return false;
    	return true;
    }
}
