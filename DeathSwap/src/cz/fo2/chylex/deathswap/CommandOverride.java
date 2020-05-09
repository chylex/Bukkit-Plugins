package cz.fo2.chylex.deathswap;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandOverride{
	public static boolean listCommand(DeathSwap plugin, CommandSender s){
		if (plugin.swapper.isPlaying()){
			StringBuilder alive = new StringBuilder(), dead = new StringBuilder();
			for(String str : plugin.swapper.getPlayers()){
				alive.append(str).append(", ");
			}
			for(String str : plugin.spectating.getSpectators()){
				dead.append(str).append(", ");
			}
			if (alive.length() > 0){
				s.sendMessage(ChatColor.GREEN + "[Alive] " + ChatColor.RESET + alive.substring(0, alive.length() - 2));
			}
			if (dead.length() > 0){
				s.sendMessage(ChatColor.GREEN + "[Spectating] " + ChatColor.RESET + dead.substring(0, dead.length() - 2));
			}
			return true;
		}
		return false;
	}
}
