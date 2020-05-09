package cz.fo2.chylex.util;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import java.util.Arrays;

public class BukkitUtil{
	public static ItemStack setNameAndLore(ItemStack is, String name, String... lore){
		ItemMeta meta = is.getItemMeta();
		if (name != null){
			meta.setDisplayName(ChatColor.RESET + name);
		}
		if (lore != null && lore.length > 0 && !lore[0].isEmpty()){
			meta.setLore(Arrays.asList(lore));
		}
		is.setItemMeta(meta);
		return is;
	}
	
	public static void resetPlayer(Player p){
		p.setFallDistance(0f);
		p.setFireTicks(0);
		p.setFoodLevel(20);
		p.setGameMode(GameMode.SURVIVAL);
		p.setHealth(20);
		for(PotionEffect eff : p.getActivePotionEffects()){
			p.removePotionEffect(eff.getType());
		}
		PlayerInventory inv = p.getInventory();
		inv.clear();
		inv.setHelmet(null);
		inv.setChestplate(null);
		inv.setLeggings(null);
		inv.setBoots(null);
	}
	
	public static void getHighestBlockYAt(Location loc){
		loc.setY(128);
		while(loc.getBlock().getTypeId() == 0){
			loc.setY(loc.getY() - 1);
		}
		loc.setY(loc.getY() + 2D);
	}
}
