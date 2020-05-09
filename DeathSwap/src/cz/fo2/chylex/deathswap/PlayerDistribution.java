package cz.fo2.chylex.deathswap;
import cz.fo2.chylex.util.BukkitUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PlayerDistribution{
	public static void distributeOnGrid(Collection<Player> players, World world, int distance){
		int gridSize = 1, gridPls = players.size() - 9;
		while(gridPls > 0){
			gridPls -= (++gridSize) * 8;
		}
		List<Vector> locs = new ArrayList<Vector>();
		for(int a = -gridSize; a <= gridSize; a++){
			for(int b = -gridSize; b <= gridSize; b++){
				locs.add(new Vector(a * distance, 0, b * distance));
			}
		}
		Collections.shuffle(locs);
		
		for(Player p : players){
			Location l = locs.get(0).toLocation(world);
			Chunk c = world.getChunkAt(l);
			if (!c.isLoaded()){
				c.load(true);
			}
			BukkitUtil.getHighestBlockYAt(l);
			p.teleport(l);
			locs.remove(0);
		}
	}
}
