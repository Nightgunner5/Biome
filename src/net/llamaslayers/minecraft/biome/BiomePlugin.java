package net.llamaslayers.minecraft.biome;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.BiomeBase;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;

public class BiomePlugin extends JavaPlugin {
	private static BiomePlugin instance = null;
	private static Map<String, Map<LocationSet, Biome>> cache = new HashMap<String, Map<LocationSet, Biome>>();

	@Override
	public void onDisable() {
		for (World _world : getServer().getWorlds()) {
			CraftWorld world = (CraftWorld) _world;
			if (world.getHandle().worldProvider.b instanceof BiomeChunkManager) {
				world.getHandle().worldProvider.b = ((BiomeChunkManager) world
						.getHandle().worldProvider.b).inner;
			}
		}
		instance = null;
	}

	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.WORLD_INIT, new WorldListener() {
			@Override
			public void onWorldInit(WorldInitEvent event) {
				CraftWorld world = (CraftWorld) event.getWorld();
				world.getHandle().worldProvider.b = new BiomeChunkManager(world
						.getHandle(), world.getHandle().worldProvider.b);
			}
		}, Event.Priority.Normal, this);
		instance = this;
		getCommand("biome").setExecutor(new BiomeCommand());
	}

	public static void setBiomeForChunk(String world, int x, int z, Biome biome) {
		getBiomeForLocation(world, 0, 0); // Load world into cache
		LocationSet chunk = new LocationSet(x, z);
		for (LocationSet locations : cache.get(world).keySet()) {
			boolean dirty = false;
			for (int _x = x << 4; _x < x << 16 + 16; _x++) {
				for (int _z = z << 4; _z < z << 16 + 16; _z++) {
					if (locations.in(_x, _z)) {
						dirty = true;
					}
				}
			}
			if (dirty) {
				Biome _biome = cache.get(world).get(locations);
				LocationSet newLocations = locations.remove(chunk);
				cache.get(world).remove(locations);
				if (!newLocations.isEmpty()) {
					cache.get(world).put(newLocations, _biome);
				}
			}
		}
		if (biome != null) {
			cache.get(world).put(chunk, biome);
		}
		saveBiomeCache(world);
	}

	public static void clearBiomeForChunk(String world, int x, int z) {
		setBiomeForChunk(world, x, z, null);
	}

	public static void setBiomeForRegion(String world, Region region,
			Biome biome) {
		getBiomeForLocation(world, 0, 0); // Load world into cache
		LocationSet chunk = new LocationSet(region);
		for (LocationSet locations : cache.get(world).keySet()) {
			Biome _biome = cache.get(world).get(locations);
			LocationSet newLocations = locations.remove(chunk);
			if (!newLocations.equals(locations)) {
				cache.get(world).remove(locations);
				if (!newLocations.isEmpty()) {
					cache.get(world).put(newLocations, _biome);
				}
			}
		}
		if (biome != null) {
			cache.get(world).put(chunk, biome);
		}
		saveBiomeCache(world);
	}

	public static void clearBiomeForRegion(String world, Region region) {
		setBiomeForRegion(world, region, null);
	}

	private static void saveBiomeCache(String world) {
		instance.getDataFolder().mkdirs();
		try {
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(new File(instance.getDataFolder(),
							world + ".dat")));
			out.writeObject(cache.get(world));
			out.flush();
			out.close();
		} catch (IOException ex) {
		}
	}

	@SuppressWarnings("unchecked")
	public static Biome getBiomeForLocation(String world, int x, int z) {
		if (!cache.containsKey(world)) {
			cache.put(world, new HashMap<LocationSet, Biome>());

			try {
				ObjectInputStream in = new ObjectInputStream(
						new FileInputStream(new File(instance.getDataFolder(),
								world + ".dat")));
				@SuppressWarnings("rawtypes")
				Map worldData = (Map) in.readObject();
				Map<LocationSet, Biome> data;
				in.close();
				if (worldData.isEmpty())
					return null;
				if (worldData.keySet().iterator().next() instanceof Integer) {
					// Convert v0.1 data
					data = new HashMap<LocationSet, Biome>();
					for (Object _chunkID : worldData.keySet()) {
						int chunkID = ((Integer) _chunkID).intValue();
						int chunkX = chunkID >> 16;
						int chunkZ = chunkID & 0xffff;
						if ((chunkZ & 0x8000) != 0) {
							chunkZ ^= 0x8000;
							chunkZ *= -1;
						}
						data.put(new LocationSet(chunkX, chunkZ),
								(Biome) worldData.get(_chunkID));
					}
				} else {
					data = worldData;
				}

				cache.get(world).putAll(data);
			} catch (FileNotFoundException ex) {
				return null;
			} catch (IOException ex) {
				return null;
			} catch (ClassNotFoundException ex) {
				return null;
			}
		}

		for (LocationSet locations : cache.get(world).keySet()) {
			if (locations.in(x, z))
				return cache.get(world).get(locations);
		}
		return null;
	}

	public static BiomeBase getBiomeBaseForLocation(String world, int x, int z) {
		Biome biome = getBiomeForLocation(world, x, z);
		if (biome == null)
			return null;
		switch (biome) {
		case DESERT:
			return BiomeBase.DESERT;
		case FOREST:
			return BiomeBase.FOREST;
		case ICE_DESERT:
			return BiomeBase.ICE_DESERT;
		case PLAINS:
			return BiomeBase.PLAINS;
		case RAINFOREST:
			return BiomeBase.RAINFOREST;
		case SAVANNA:
			return BiomeBase.SAVANNA;
		case SEASONAL_FOREST:
			return BiomeBase.SEASONAL_FOREST;
		case SHRUBLAND:
			return BiomeBase.SHRUBLAND;
		case SWAMPLAND:
			return BiomeBase.SWAMPLAND;
		case TAIGA:
			return BiomeBase.TAIGA;
		case TUNDRA:
			return BiomeBase.TUNDRA;
		case HELL:
			return BiomeBase.HELL;
		case SKY:
			return BiomeBase.SKY;
		}
		return null;
	}

	protected static WorldEditPlugin getWorldEdit() {
		Plugin worldEdit = instance.getServer().getPluginManager()
				.getPlugin("WorldEdit");
		if (worldEdit == null)
			return null;

		if (worldEdit instanceof WorldEditPlugin)
			return (WorldEditPlugin) worldEdit;
		else
			return null;
	}
}
