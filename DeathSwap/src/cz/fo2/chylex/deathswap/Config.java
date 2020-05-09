package cz.fo2.chylex.deathswap;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class Config{
	private DeathSwap plugin;
	public int min_swap_time, max_swap_time, resistance_duration, spawn_distance;
	public boolean allow_drops;
	public String spawn_items;
	
	public Config(DeathSwap plugin){
		this.plugin = plugin;
		plugin.saveDefaultConfig();
	}
	
	public Config load(){
		FileConfiguration conf = plugin.getConfig();
		min_swap_time = conf.getInt("game.minSwapTime", 25);
		max_swap_time = conf.getInt("game.maxSwapTime", 90);
		resistance_duration = conf.getInt("game.resistanceDuration", 6);
		spawn_distance = conf.getInt("game.spawnDistance", 1000);
		allow_drops = conf.getBoolean("game.allowDrops", true);
		spawn_items = conf.getString("game.startKit", "");
		return this;
	}
	
	public static String hl(String msg){
		return new StringBuilder().append(ChatColor.RED).append(msg).append(ChatColor.RESET).toString();
	}
}
