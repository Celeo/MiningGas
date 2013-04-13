package com.darktidegames.celeo.mininggas;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Effect;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * <b>MiningGas</b><br>
 * Mining gives a percentage chance to put potion effects on the miner
 * 
 * @author Celeo
 */
public class MiningGas extends JavaPlugin implements Listener
{

	private Map<PotionEffectType, Integer> effects = new HashMap<PotionEffectType, Integer>();
	private int duration = 30;

	@Override
	public void onLoad()
	{
		getDataFolder().mkdirs();
		if (!new File(getDataFolder(), "config.yml").exists())
			saveDefaultConfig();
	}

	@Override
	public void onEnable()
	{
		load();
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("Enabled");
	}

	private void load()
	{
		duration = getConfig().getInt("settings.time", 30);
		String p = "";
		for (PotionEffectType effect : PotionEffectType.values())
		{
			if (effect == null)
				continue;
			p = "effects." + effect.getName().toLowerCase();
			if (getConfig().isSet(p))
				effects.put(effect, Integer.valueOf(getConfig().getInt(p)));
		}
		getLogger().info("Settings loaded from the configuration file");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (args == null || args.length != 1
				|| !args[0].equalsIgnoreCase("reload"))
			return false;
		if (!(sender instanceof Player))
		{
			load();
			return true;
		}
		Player player = (Player) sender;
		if (player.isOp() || player.hasPermission("mgas.reload"))
		{
			load();
			player.sendMessage("§aReloaded from configuration file");
			player.sendMessage("§aEffects loaded into memory:");
			for (PotionEffectType effect : effects.keySet())
				player.sendMessage(String.format("§7Effect: §6%s§7, Chance: §6%d", effect.getName().toLowerCase(), effects.get(effect)));
			player.sendMessage("§7Duration: §6" + duration);
		}
		else
			player.sendMessage("§cYou cannot use that command");
		return true;
	}

	@Override
	public void onDisable()
	{
		getLogger().info("Disabled");
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		final Player player = event.getPlayer();
		int draw = new Random().nextInt(101);
		player.sendMessage("Draw: " + draw);
		List<PotionEffectType> ret = new ArrayList<PotionEffectType>();
		for (PotionEffectType effect : effects.keySet())
			if (draw < effects.get(effect).intValue())
				ret.add(effect);
		if (ret.isEmpty())
			return;
		PotionEffectType add = ret.get(new Random().nextInt(ret.size()));
		player.sendMessage("§7Adding potion effect type "
				+ add.getName().toLowerCase() + " for " + (duration * 20)
				+ " ticks");
		PotionEffect fin = add.createEffect(duration * 20
				* (int) (1 / add.getDurationModifier()), 1);
		event.getBlock().getWorld().playEffect(event.getBlock().getLocation(), Effect.POTION_BREAK, 2);
		player.addPotionEffect(fin, true);
	}

}