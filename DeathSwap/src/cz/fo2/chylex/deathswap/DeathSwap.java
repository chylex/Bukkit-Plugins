package cz.fo2.chylex.deathswap;
import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class DeathSwap extends JavaPlugin implements Listener{
	public Config config;
	public Swapper swapper;
	public Spectating spectating;
	
	@Override
	public void onEnable(){
		config = new Config(this).load();
		swapper = new Swapper(this);
		spectating = new Spectating(this);
		new EventListener(this);
	}
	
	@Override
	public void onDisable(){
	}
	
	@Override
	public boolean onCommand(CommandSender s, Command cmd, String alias, String[] args){
		if (s instanceof Player && ((Player)s).isOp() == false){
			s.sendMessage("You don't have permissions!");
			return true;
		}
		if (args.length == 0){
			s.sendMessage(ChatColor.GREEN + "[DeathSwap commands]");
			String a = "/" + alias + " ";
			s.sendMessage(a + "start - start the game");
			s.sendMessage(a + "resume - resume after server crash");
			s.sendMessage(a + "reload - reload configuration file");
		}
		else if (args[0].equals("start")){
			if (swapper.isPlaying()){
				s.sendMessage("There's already a game running!");
			}
			else if (getServer().getOnlinePlayers().length < 2){
				s.sendMessage("Not enough players to start the game!");
			}
			else{
				getServer().getWorlds().get(0).setTime(0L);
				getServer().broadcastMessage("Let the game begin!");
				PlayerDistribution.distributeOnGrid(Arrays.asList(getServer().getOnlinePlayers()), getServer().getWorlds().get(0), config.spawn_distance);
				swapper.start();
			}
		}
		else if (args[0].equals("resume")){
			if (swapper.isPlaying()){
				s.sendMessage("There's already a game running!");
			}
			else if (getServer().getOnlinePlayers().length < 2){
				s.sendMessage("Not enough players to start the game!");
			}
			else{
				getServer().broadcastMessage("Let the game continue!");
				swapper.start(false);
			}
		}
		else if (args[0].equals("reload")){
			reloadConfig();
			config.load();
			s.sendMessage("Configuration file was reloaded!");
		}
		else{
			return false;
		}
		return true;
	}
}
