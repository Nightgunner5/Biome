package net.llamaslayers.minecraft.biome;

import net.minecraft.server.World;

import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

public class BiomeCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}
		Player player = (Player) sender;

		if (args.length == 0)
			return false;

		if (!(((CraftWorld) player.getWorld()).getHandle().worldProvider.b instanceof BiomeChunkManager)) {
			Class<?> chunkClass = ((CraftWorld) player.getWorld()).getHandle().worldProvider.b
					.getClass();
			sender.sendMessage("Please report the following error to the Biome plugin thread: "
					+ chunkClass.getPackage().getName()
					+ "."
					+ chunkClass.getName());
			return true;
		}

		if (args[0].equals("get")) {
			if (args.length != 1)
				return false;
			if (!BiomePlugin.hasPermissionTo(player, BiomePermission.GET))
				return true;

			CraftWorld world = (CraftWorld) player.getWorld();
			World innerWorld = world.getHandle();
			String biomeName = innerWorld.worldProvider.b.getBiome(player
					.getLocation().getBlockX(), player.getLocation()
					.getBlockZ()).n;
			sender.sendMessage("The biome you are standing in is "
					+ biomeName.charAt(0)
					+ biomeName.substring(1).toLowerCase());
			return true;
		} else if (args[0].equals("set")) {
			if (args.length < 2)
				return false;
			if (!BiomePlugin.hasPermissionTo(player, BiomePermission.SET_CHUNK))
				return true;

			StringBuilder sb = new StringBuilder(args[1].toUpperCase());
			for (int i = 2; i < args.length; i++) {
				sb.append('_').append(args[i].toUpperCase());
			}

			try {
				Biome biome = Biome.valueOf(sb.toString());
				BiomePlugin.setBiomeForChunk(player.getWorld().getName(),
						player.getLocation().getBlockX() / 16, player
								.getLocation().getBlockZ() / 16, biome, player);
				sender.sendMessage("Set this chunk's biome to " + sb.charAt(0)
						+ sb.substring(1).toLowerCase().replace('_', ' '));
			} catch (IllegalArgumentException ex) {
				sender.sendMessage("Unknown biome.");
			}
			return true;
		} else if (args[0].equals("clear")) {
			if (args.length != 1)
				return false;
			if (!BiomePlugin.hasPermissionTo(player,
					BiomePermission.CLEAR_CHUNK))
				return true;

			BiomePlugin.clearBiomeForChunk(player.getWorld().getName(), player
					.getLocation().getBlockX() / 16, player.getLocation()
					.getBlockZ() / 16, player);
			sender.sendMessage("Set this chunk to use its natural biomes.");
			return true;
		} else if (args[0].equals("list")) {
			if (args.length != 1)
				return false;
			if (!BiomePlugin.hasPermissionTo(player, BiomePermission.LIST))
				return true;

			StringBuilder sb = new StringBuilder("Biomes: ");
			boolean first = true;
			for (Biome biome : Biome.values()) {
				if (!first) {
					sb.append(", ");
				}
				first = false;
				String name = biome.name().toLowerCase().replace('_', ' ');
				sb.append(Character.toUpperCase(name.charAt(0))).append(
						name.substring(1));
			}
			sender.sendMessage(sb.toString());
			return true;
		} else if (args[0].equals("set-selection")) {
			if (args.length < 2)
				return false;
			if (!BiomePlugin.hasPermissionTo(player,
					BiomePermission.SET_SELECTION))
				return true;

			StringBuilder sb = new StringBuilder(args[1].toUpperCase());
			for (int i = 2; i < args.length; i++) {
				sb.append('_').append(args[i].toUpperCase());
			}

			try {
				Biome biome = Biome.valueOf(sb.toString());
				if (BiomePlugin.getWorldEdit() == null) {
					sender.sendMessage("This server does not have the WorldEdit plugin installed.");
					return true;
				}
				BiomePlugin
						.setBiomeForRegion(player.getWorld().getName(),
								BiomePlugin.getWorldEdit().getSelection(player)
										.getRegionSelector().getRegion(),
								biome, player);
				sender.sendMessage("Set your selection's biome to "
						+ sb.charAt(0)
						+ sb.substring(1).toLowerCase().replace('_', ' '));
			} catch (IllegalArgumentException ex) {
				sender.sendMessage("Unknown biome.");
			} catch (Exception ex) {
				sender.sendMessage("You have not defined a selection in WorldEdit.");
			}
			return true;
		} else if (args[0].equals("clear-selection")) {
			if (args.length != 1)
				return false;
			if (!BiomePlugin.hasPermissionTo(player,
					BiomePermission.CLEAR_SELECTION))
				return true;

			if (BiomePlugin.getWorldEdit() == null) {
				sender.sendMessage("This server does not have the WorldEdit plugin installed.");
				return true;
			}

			try {
				BiomePlugin.clearBiomeForRegion(player.getWorld().getName(),
						BiomePlugin.getWorldEdit().getSelection(player)
								.getRegionSelector().getRegion(), player);
				sender.sendMessage("Set your selection to use its natural biomes.");
			} catch (Exception ex) {
				sender.sendMessage("You have not defined a selection in WorldEdit.");
			}
			return true;
		}

		return false;
	}
}
