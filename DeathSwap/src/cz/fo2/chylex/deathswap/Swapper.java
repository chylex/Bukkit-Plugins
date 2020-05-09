package cz.fo2.chylex.deathswap;
import cz.fo2.chylex.util.BukkitUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Swapper{
	private DeathSwap plugin;
	private Set<String> players;
	private Map<String, Byte> disconnected;
	private Random rand;
	private int taskId = -1;
	private int time;
	private PotionEffect resistanceEffect;
	public boolean spawnProtection;
	
	public Swapper(DeathSwap plugin){
		this.plugin = plugin;
		players = new HashSet<String>();
		disconnected = new HashMap<String, Byte>();
		rand = new Random();
		spawnProtection = false;
	}
	
	public Set<String> getPlayers(){
		return Collections.unmodifiableSet(players);
	}
	
	public void start(){
		start(true);
	}
	
	public void start(boolean resetPlayers){
		if (isPlaying()){
			return;
		}
		renewTimer();
		time += 8; // cooldown
		resistanceEffect = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * plugin.config.resistance_duration, 5, true);
		
		spawnProtection = true;
		plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){
			@Override
			public void run(){
				spawnProtection = false;
			}
		}, 120L);
		
		players.clear();
		disconnected.clear();
		List<ItemStack> startItems = new ArrayList<ItemStack>();
		for(String ar : plugin.config.spawn_items.split(",")){
			try{
				String[] data = ar.split(":");
				String[] star = data[0].split("\\*");
				int id = Integer.parseInt(star.length > 1 ? star[1] : star[0]);
				int am = star.length > 1 ? Integer.parseInt(star[0]) : 1;
				int dur = data.length > 1 ? Integer.parseInt(data[1]) : 0;
				startItems.add(new ItemStack(id, am, (short)dur));
			}catch(NumberFormatException e){
			}
		}
		ItemStack[] startItemsArray = startItems.toArray(new ItemStack[startItems.size()]);
		
		PotionEffect startResistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 5, true);
		for(Player p : plugin.getServer().getOnlinePlayers()){
			players.add(p.getName());
			if (resetPlayers){
				BukkitUtil.resetPlayer(p);
				p.addPotionEffect(startResistance);
				p.getInventory().addItem(startItemsArray);
			}
		}
		
		taskId = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable(){
			@Override
			public void run(){
				if (disconnected.size() > 0){
					for(String s : new HashSet<String>(disconnected.keySet())){
						byte b = (byte)(disconnected.get(s) + 1);
						if (b >= 60){
							plugin.getServer().broadcastMessage(Config.hl(s) + " was disqualified");
							onPlayerDeath(s);
						}
						else{
							disconnected.put(s, b);
						}
					}
				}
				
				if (--time <= 0){
					swap();
					renewTimer();
				}
			}
		}, 20L, 20L).getTaskId();
	}
	
	public boolean isPlaying(){
		return taskId != -1;
	}
	
	public boolean canPlayerJoin(String player){
		return !isPlaying() || players.contains(player);
	}
	
	public void onPlayerJoin(String player){
		if (!isPlaying()){
			return;
		}
		disconnected.remove(player);
	}
	
	public void onPlayerDisconnect(String player){
		if (!isPlaying()){
			return;
		}
		disconnected.put(player, (byte)0);
	}
	
	public void onPlayerDeath(String player){
		if (!isPlaying()){
			return;
		}
		players.remove(player);
		updateGameState();
	}
	
	public void updateGameState(){
		int sz = players.size();
		if (sz == 0){
			plugin.getServer().broadcastMessage("Well, there are no players in the game, this shouldn't have happened... Awkward.");
			end();
		}
		else if (sz == 1){
			for(String s : plugin.spectating.getSpectators()){
				Player p = plugin.getServer().getPlayerExact(s);
				if (p != null){
					plugin.spectating.removeSpectator(p);
				}
			}
			String name = players.iterator().next();
			plugin.getServer().broadcastMessage(Config.hl(name) + " is the winner!");
			Location spawn = plugin.getServer().getWorlds().get(0).getSpawnLocation().clone();
			spawn.setY(spawn.getWorld().getHighestBlockYAt(spawn.getBlockX(), spawn.getBlockZ()));
			for(Player p : plugin.getServer().getOnlinePlayers()){
				p.teleport(spawn);
				if (p.getName().equals(name)){
					BukkitUtil.resetPlayer(p);
				}
			}
			end();
		}
	}
	
	public void end(){
		plugin.getServer().getScheduler().cancelTask(taskId);
		players.clear();
		disconnected.clear();
		taskId = -1;
		spawnProtection = false;
	}
	
	private void swap(){
		List<Player> pls = new ArrayList<Player>();
		for(String s : players){
			Player p = plugin.getServer().getPlayerExact(s);
			if (p != null){
				pls.add(p);
			}
		}
		int size = pls.size();
		if (size < 2){
			return;
		}
		else if (size == 2){
			Player p1 = pls.get(0), p2 = pls.get(1);
			Location l1 = p1.getLocation().clone(), l2 = p2.getLocation().clone();
			preTeleport(p1);
			preTeleport(p2);
			p1.teleport(l2);
			p2.teleport(l1);
			postTeleport(p1);
			postTeleport(p2);
			p1.sendMessage("You were swapped with " + Config.hl(p2.getName()) + "!");
			p2.sendMessage("You were swapped with " + Config.hl(p1.getName()) + "!");
		}
		else{
			Collections.shuffle(pls, rand);
			Player[] arrPlr = pls.toArray(new Player[size]);
			Location[] arrLoc = new Location[size];
			for(int i = 0; i < size; i++){
				arrLoc[i] = arrPlr[i].getLocation().clone();
			}
			for(int i = 0; i < size; i++){
				Player next = arrPlr[i + 1 == size ? 0 : i + 1], prev = arrPlr[i == 0 ? size - 1 : i - 1];
				arrPlr[i].sendMessage("You're now at " + Config.hl(next.getName()) + "'s location, and " + Config.hl(prev.getName()) + " is on yours!");
				preTeleport(arrPlr[i]);
				arrPlr[i].teleport(arrLoc[i + 1 == size ? 0 : i + 1]);
				postTeleport(arrPlr[i]);
			}
		}
		for(String s : plugin.spectating.getSpectators()){
			Player p = plugin.getServer().getPlayerExact(s);
			if (p != null){
				p.sendMessage("Players were swapped!");
			}
		}
	}
	
	private void preTeleport(Player p){
		if (p.isInsideVehicle()){
			p.leaveVehicle();
		}
	}
	
	private void postTeleport(Player p){
		p.addPotionEffect(resistanceEffect);
	}
	
	private void renewTimer(){
		time = rand.nextInt(Math.max(1, plugin.config.max_swap_time - plugin.config.min_swap_time)) + plugin.config.min_swap_time;
	}
}
