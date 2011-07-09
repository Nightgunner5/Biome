package net.llamaslayers.minecraft.biome;

import java.io.Serializable;
import java.util.Arrays;

import org.bukkit.Location;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

// Values must be stored in order of z from smallest to largest, then x from
// smallest to largest, with no duplicates.
public class LocationSet implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final LocationSet EMPTY = new LocationSet(new int[0],
			new int[0]);
	public final int[] x;
	public final int[] z;
	public final int length;

	public LocationSet(int chunkX, int chunkZ) {
		x = new int[256];
		z = new int[256];
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				x[i + j * 16] = chunkX * 16 + i;
				z[i + j * 16] = chunkZ * 16 + j;
			}
		}
		length = 256;
	}

	private LocationSet(int[] _x, int[] _z) {
		int[] __x = new int[_x.length];
		int[] __z = new int[_z.length];
		int points = 0;
		blockloop: for (int i = 0; i < _x.length; i++) {
			if (_x[i] == Integer.MAX_VALUE || _z[i] == Integer.MAX_VALUE) {
				continue;
			}
			for (int j = 0; j < points; j++) {
				if (__x[j] == _x[i] && __z[j] == _z[i]) {
					continue blockloop;
				}
			}
			__x[points] = _x[i];
			__z[points] = _z[i];
			points++;
		}
		x = new int[points];
		z = new int[points];

		for (int i = 0; i < points; i++) {
			int best = 0;
			for (int j = 1; j < points; j++) {
				if (__z[best] > __z[j]) {
					best = j;
					continue;
				}
				if (__z[best] == __z[j] && __x[best] > __x[j]) {
					best = j;
				}
			}
			x[i] = __x[best];
			z[i] = __z[best];
			__x[best] = Integer.MAX_VALUE;
			__z[best] = Integer.MAX_VALUE;
		}
		length = points;
	}

	public LocationSet(Region region) {
		int[] _x = new int[region.getArea()];
		int[] _z = new int[region.getArea()];
		int points = 0;
		blockloop: for (BlockVector block : region) {
			for (int i = 0; i < points; i++) {
				if (_x[i] == block.getBlockX() && _z[i] == block.getBlockZ()) {
					continue blockloop;
				}
			}
			_x[points] = block.getBlockX();
			_z[points] = block.getBlockZ();
			points++;
		}
		x = new int[points];
		z = new int[points];

		for (int i = 0; i < points; i++) {
			int best = 0;
			for (int j = 1; j < points; j++) {
				if (_z[best] > _z[j]) {
					best = j;
					continue;
				}
				if (_z[best] == _z[j] && _x[best] > _x[j]) {
					best = j;
				}
			}
			x[i] = _x[best];
			z[i] = _z[best];
			_x[best] = Integer.MAX_VALUE;
			_z[best] = Integer.MAX_VALUE;
		}
		length = points;
	}

	public LocationSet(ProtectedRegion region) {
		int[] _x = new int[region.volume()];
		int[] _z = new int[region.volume()];
		int points = 0;
		for (int i = region.getMinimumPoint().getBlockX(); i <= region
				.getMaximumPoint().getBlockX(); i++) {
			for (int j = region.getMinimumPoint().getBlockZ(); j <= region
					.getMaximumPoint().getBlockZ(); j++) {
				for (int k = region.getMinimumPoint().getBlockY(); k <= region
						.getMaximumPoint().getBlockY(); k++) {
					if (region.contains(new BlockVector(i, j, k))) {
						_x[points] = i;
						_z[points] = i;
						points++;
						break;
					}
				}
			}
		}

		x = new int[points];
		z = new int[points];

		for (int i = 0; i < points; i++) {
			int best = 0;
			for (int j = 1; j < points; j++) {
				if (_z[best] > _z[j]) {
					best = j;
					continue;
				}
				if (_z[best] == _z[j] && _x[best] > _x[j]) {
					best = j;
				}
			}
			x[i] = _x[best];
			z[i] = _z[best];
			_x[best] = Integer.MAX_VALUE;
			_z[best] = Integer.MAX_VALUE;
		}
		length = points;
	}

	public boolean isEmpty() {
		return x.length == 0;
	}

	public boolean in(int blockX, int blockZ) {
		for (int i = 0; i < x.length; i++) {
			if (x[i] == blockX && z[i] == blockZ)
				return true;
		}
		return false;
	}

	public boolean in(Location location) {
		return in(location.getBlockX(), location.getBlockZ());
	}

	public LocationSet add(LocationSet other) {
		int[] _x = new int[x.length + other.x.length];
		int[] _z = new int[z.length + other.z.length];

		for (int i = 0; i < x.length; i++) {
			_x[i] = x[i];
			_z[i] = z[i];
		}

		int start = x.length;
		for (int i = 0; i < other.x.length; i++) {
			_x[i + start] = other.x[i];
			_z[i + start] = other.z[i];
		}

		return new LocationSet(_x, _z);
	}

	public LocationSet remove(LocationSet other) {
		int[] _x = new int[x.length];
		int[] _z = new int[z.length];

		for (int i = 0; i < x.length; i++) {
			if (other.in(x[i], z[i])) {
				_x[i] = Integer.MAX_VALUE;
				_z[i] = Integer.MAX_VALUE;
			} else {
				_x[i] = x[i];
				_z[i] = z[i];
			}
		}
		return new LocationSet(_x, _z);
	}

	public LocationSet union(LocationSet other) {
		int[] _x = new int[x.length];
		int[] _z = new int[z.length];

		for (int i = 0; i < x.length; i++) {
			if (other.in(x[i], z[i])) {
				_x[i] = x[i];
				_z[i] = z[i];
			} else {
				_x[i] = Integer.MAX_VALUE;
				_z[i] = Integer.MAX_VALUE;
			}
		}
		return new LocationSet(_x, _z);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(x);
		result = prime * result + Arrays.hashCode(z);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof LocationSet))
			return false;
		LocationSet other = (LocationSet) obj;
		if (!Arrays.equals(x, other.x))
			return false;
		if (!Arrays.equals(z, other.z))
			return false;
		return true;
	}
}
