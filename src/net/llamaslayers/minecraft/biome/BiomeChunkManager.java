package net.llamaslayers.minecraft.biome;

import java.util.Random;

import net.minecraft.server.BiomeBase;
import net.minecraft.server.ChunkCoordIntPair;
import net.minecraft.server.NoiseGeneratorOctaves2;
import net.minecraft.server.World;
import net.minecraft.server.WorldChunkManager;

public class BiomeChunkManager extends WorldChunkManager {
	private final World world;
	private final NoiseGeneratorOctaves2 randTemperature;
	private final NoiseGeneratorOctaves2 randRain;
	private final NoiseGeneratorOctaves2 randRand;
	public final WorldChunkManager inner;

	public BiomeChunkManager(World world, WorldChunkManager inner) {
		randTemperature = new NoiseGeneratorOctaves2(new Random(
				world.getSeed() * 9871L), 4);
		randRain = new NoiseGeneratorOctaves2(new Random(
				world.getSeed() * 39811L), 4);
		randRand = new NoiseGeneratorOctaves2(new Random(
				world.getSeed() * 543321L), 2);

		this.world = world;
		this.inner = inner;
	}

	@Override
	public BiomeBase[] a(BiomeBase[] buffer, int x, int z, int rx, int rz) {
		if (buffer == null || buffer.length < rx * rz) {
			buffer = new BiomeBase[rx * rz];
		}

		temperature = randTemperature.a(temperature, x, z, rx, rx, 0.025,
				0.025, 0.25);
		rain = randRain.a(rain, x, z, rx, rx, 0.05, 0.05, 1.0 / 3.0);
		c = randRand.a(c, x, z, rx, rx, 0.25, 0.25, 10.0 / 17.0);
		int i = 0;

		for (int j = 0; j < rx; ++j) {
			for (int k = 0; k < rz; ++k) {
				BiomeBase userDefined = BiomePlugin.getBiomeBaseForLocation(
						world.getWorld().getName(), (x + j), (z + k));
				if (userDefined != null) {
					buffer[i++] = userDefined;
					continue;
				}

				double jitter = c[i] * 1.1 + 0.5;
				double min = 0.01;
				double max = 1.0 - min;
				double finalTemperature = (temperature[i] * 0.15 + 0.7) * max
						+ jitter * min;

				min = 0.002;
				max = 1.0 - min;
				double finalRain = (rain[i] * 0.15 + 0.5) * max + jitter * min;

				finalTemperature = 1.0 - (1.0 - finalTemperature)
						* (1.0 - finalTemperature);
				if (finalTemperature < 0.0) {
					finalTemperature = 0.0;
				}

				if (finalRain < 0.0) {
					finalRain = 0.0;
				}

				if (finalTemperature > 1.0) {
					finalTemperature = 1.0;
				}

				if (finalRain > 1.0) {
					finalRain = 1.0;
				}

				temperature[i] = finalTemperature;
				rain[i] = finalRain;
				buffer[i++] = BiomeBase.a(finalTemperature, finalRain);
			}
		}
		return buffer;
	}

	@Override
	public BiomeBase a(ChunkCoordIntPair coords) {
		return getBiome(coords.x << 4, coords.z << 4);
	}

	/**
	 * This seems to be used to get snowfall percentages
	 */
	@Override
	public double[] a(double[] buffer, int x, int z, int rx, int rz) {
		if (buffer == null || buffer.length < rx * rz) {
			buffer = new double[rx * rz];
		}

		buffer = randTemperature.a(buffer, x, z, rx, rz, 0.025, 0.025, 0.25);
		c = randRand.a(c, x, z, rx, rz, 0.25D, 0.25D, 10.0 / 17.0);
		int i = 0;

		for (int j = 0; j < rx; ++j) {
			for (int k = 0; k < rz; ++k) {
				double jitter = c[i] * 1.1 + 0.5;
				double min = 0.01;
				double max = 1.0 - min;
				double finalSnow = (buffer[i] * 0.15 + 0.7) * max + jitter
						* min;

				finalSnow = 1.0 - (1.0 - finalSnow) * (1.0 - finalSnow);
				if (finalSnow < 0.0) {
					finalSnow = 0.0;
				}

				if (finalSnow > 1.0) {
					finalSnow = 1.0;
				}

				buffer[i] = finalSnow;
				++i;
			}
		}

		return buffer;
	}

	@Override
	public BiomeBase getBiome(int x, int z) {
		return getBiomeData(x, z, 1, 1)[0];
	}

	@Override
	public BiomeBase[] getBiomeData(int x, int z, int rx, int rz) {
		d = a(d, x, z, rx, rz);
		return d;
	}

}
