package cz.fo2.chylex.deathswap;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Spectating implements Listener{
	private DeathSwap plugin;
	private Map<String, Integer> spectators = new ConcurrentHashMap<String, Integer>();
	
	public Spectating(DeathSwap plugin){
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	public Set<String> getSpectators(){
		return Collections.unmodifiableSet(spectators.keySet());
	}
	
	public void addSpectator(Player p){
		final String name = p.getName();
		spectators.put(name, -1);
		p.setMetadata("DeathSwap_Spec", new FixedMetadataValue(plugin, true));
		
		plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){
			@Override
			public void run(){
				Player p = plugin.getServer().getPlayerExact(name);
				if (p == null){
					return;
				}
				p.setAllowFlight(true);
				p.setFlying(true);
			}
		}, 5L);
		
		p.setGameMode(GameMode.ADVENTURE);
		for(Player pl : plugin.getServer().getOnlinePlayers()){
			pl.hidePlayer(p);
		}
		
		p.sendMessage("You're spectating. You can only chat with other spectators. Click to teleport between players.");
	}
	
	public void removeSpectator(Player p){
		spectators.remove(p.getName());
		p.removeMetadata("DeathSwap_Spec", plugin);
		
		p.setAllowFlight(false);
		p.setFlying(false);
		p.setGameMode(GameMode.SURVIVAL);
		for(Player pl : plugin.getServer().getOnlinePlayers()){
			pl.showPlayer(p);
		}
	}
	
	public boolean isSpectator(Player p){
		return p.hasMetadata("DeathSwap_Spec");
	}
	
	public boolean isSpectator(String s){
		return spectators.containsKey(s);
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerJoinEvent e){
		for(String s : spectators.keySet()){
			Player p = plugin.getServer().getPlayerExact(s);
			if (p != null){
				e.getPlayer().hidePlayer(p);
			}
		}
	}
	
	@EventHandler
	public void onPlayerDisconnect(PlayerQuitEvent e){
		removeSpectator(e.getPlayer());
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e){
		if (!isSpectator(e.getPlayer())){
			return;
		}
		for(Iterator<Player> iter = e.getRecipients().iterator(); iter.hasNext(); ){
			if (!isSpectator(iter.next())){
				iter.remove();
			}
		}
		e.setFormat("[Spectating] " + e.getFormat());
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e){
		if (isSpectator(e.getPlayer())){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e){
		if (isSpectator(e.getPlayer())){
			e.setCancelled(true);
			e.getPlayer().getInventory().clear();
		}
	}
	
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent e){
		if (isSpectator(e.getPlayer())){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e){
		if (isSpectator(e.getPlayer())){
			e.setCancelled(true);
			e.getPlayer().getInventory().clear();
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent e){
		if (e.getEntity() instanceof Player && isSpectator((Player)e.getEntity())){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e){
		if (e.getDamager() instanceof Player && isSpectator((Player)e.getDamager())){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e){
		if (isSpectator((Player)e.getEntity())){
			e.setFoodLevel(20);
		}
	}
	
	@EventHandler
	public void onEntityTarget(EntityTargetEvent e){
		if (e.getTarget() instanceof Player && isSpectator((Player)e.getTarget())){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e){
		if (isSpectator(e.getPlayer())){
			e.setCancelled(true);
			int add = (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) ? 1 : (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) ? -1 : 0;
			if (add == 0){
				return;
			}
			
			List<Player> list = new ArrayList<Player>();
			for(String player : plugin.swapper.getPlayers()){
				Player p = plugin.getServer().getPlayerExact(player);
				if (p != null){
					list.add(p);
				}
			}
			int size = list.size();
			if (size == 0){
				return;
			}
			
			Integer cur = spectators.get(e.getPlayer().getName());
			if (cur == null){
				cur = -1;
			}
			if (add == 1){
				cur = cur + 1 < size ? cur + 1 : 0;
			}
			else if (add == -1){
				cur = cur - 1 >= 0 ? cur - 1 : size - 1;
			}
			cur = Math.max(0, Math.min(size - 1, cur));
			spectators.put(e.getPlayer().getName(), cur);
			
			Player target = list.get(cur);
			e.getPlayer().teleport(target);
			e.getPlayer().sendMessage("You were teleported to " + Config.hl(target.getName()) + "!");
		}
	}
}
