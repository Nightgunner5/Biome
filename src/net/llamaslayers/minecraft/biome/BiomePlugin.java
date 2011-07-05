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
import net.minecraft.server.ChunkCoordIntPair;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BiomePlugin extends JavaPlugin {
	private static BiomePlugin instance = null;
	private static Map<String, Map<Integer, Biome>> cache = new HashMap<String, Map<Integer, Biome>>();

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
		if (getBiomeForChunk(world, x, z) != biome) {
			if (biome != null) {
				cache.get(world).put(ChunkCoordIntPair.a(x, z), biome);
			} else {
				cache.get(world).remove(ChunkCoordIntPair.a(x, z));
			}
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
	}

	public static void clearBiomeForChunk(String world, int x, int z) {
		setBiomeForChunk(world, x, z, null);
	}

	@SuppressWarnings("unchecked")
	public static Biome getBiomeForChunk(String world, int x, int z) {
		if (!cache.containsKey(world)) {
			cache.put(world, new HashMap<Integer, Biome>());

			try {
				ObjectInputStream in = new ObjectInputStream(
						new FileInputStream(new File(instance.getDataFolder(),
								world + ".dat")));
				cache.get(world).putAll((Map<Integer, Biome>) in.readObject());
				in.close();
			} catch (FileNotFoundException ex) {
				return null;
			} catch (IOException ex) {
				return null;
			} catch (ClassNotFoundException ex) {
				return null;
			}
		}

		return cache.get(world).get(ChunkCoordIntPair.a(x, z));
	}

	public static BiomeBase getBiomeBaseForChunk(String world, int x, int z) {
		Biome biome = getBiomeForChunk(world, x, z);
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
}
