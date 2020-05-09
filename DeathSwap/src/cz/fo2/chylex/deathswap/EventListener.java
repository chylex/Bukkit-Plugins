package cz.fo2.chylex.deathswap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServerCommandEvent;
import cz.fo2.chylex.util.BukkitUtil;

public class EventListener implements Listener{
	private DeathSwap plugin;
	
	public EventListener(DeathSwap plugin){
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerJoinEvent e){
		if (!plugin.swapper.canPlayerJoin(e.getPlayer().getName())){
			plugin.spectating.addSpectator(e.getPlayer());
		}
		else if (plugin.swapper.isPlaying()){
			plugin.swapper.onPlayerJoin(e.getPlayer().getName());
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e){
		if (plugin.swapper.isPlaying()){
			plugin.spectating.addSpectator(e.getPlayer());
		}
	}
	
	@EventHandler
	public void onPlayerDisconnect(PlayerQuitEvent e){
		if (plugin.swapper.canPlayerJoin(e.getPlayer().getName()) && plugin.swapper.isPlaying()){
			plugin.swapper.onPlayerDisconnect(e.getPlayer().getName());
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e){
		if (plugin.swapper.isPlaying()){
			plugin.swapper.onPlayerDeath(e.getEntity().getName());
			if (!plugin.swapper.isPlaying() || !plugin.config.allow_drops){
				e.setDroppedExp(0);
				e.getDrops().clear();
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent e){ // I HATE SPAWN SUFFOCATION!!!!!
		if (e.getCause() == DamageCause.SUFFOCATION && e instanceof Player && plugin.swapper.canPlayerJoin(((Player)e.getEntity()).getName()) && plugin.swapper.isPlaying() && plugin.swapper.spawnProtection){
			Location loc = e.getEntity().getLocation().clone();
			BukkitUtil.getHighestBlockYAt(loc);
			e.getEntity().teleport(loc);
		}
	}
	
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e){
		if (e.getMessage().startsWith("/list")){
			e.setCancelled(CommandOverride.listCommand(plugin, e.getPlayer()));
		}
	}
	
	@EventHandler
	public void onServerCommand(ServerCommandEvent e){
		if (e.getCommand().startsWith("list")){
			e.setCommand(CommandOverride.listCommand(plugin, e.getSender()) ? "" : "list");
		}
	}
}
