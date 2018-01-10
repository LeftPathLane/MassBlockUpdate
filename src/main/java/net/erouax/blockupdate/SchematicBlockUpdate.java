package net.erouax.blockupdate;

import com.github.steveice10.opennbt.NBTIO;
import com.github.steveice10.opennbt.tag.builtin.ByteArrayTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ShortTag;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Example implementation of {@link DetailedMassBlockUpdate} for pasting schematics
 */
public class SchematicBlockUpdate extends DetailedMassBlockUpdate {


	private final boolean pasteAir;
	private final Location location;
	private final CompoundTag schematic;

	/**
	 * Constructor to create a new instance of {@link BasicMassBlockUpdate}.
	 * Uses {@link NBTIO} to load the read the .schematic
	 *
	 * @param plugin         - Plugin to run tasks on
	 * @param location       - Location to paste the schematic into
	 * @param rateLimit      - Maximum blocks changed per tick
	 * @param schematicFile  - File for the .schematic
	 * @param pasteAir       - Should we paste in the air blocks
	 */
	public SchematicBlockUpdate(JavaPlugin plugin, Location location, int rateLimit, File schematicFile, boolean pasteAir) throws IOException {
		super(location.getWorld(), plugin, rateLimit);
		schematic = NBTIO.readFile(schematicFile);
//		schematic = new SmallSchematic(schematicFile);
		this.pasteAir = pasteAir;
		this.location = location;
	}

	public SchematicBlockUpdate(JavaPlugin plugin, Location location, int rateLimit, File schamticFile) throws IOException {
		this(plugin, location, rateLimit, schamticFile, true);
	}

	/**
	 * Loop through the blocks in the schematic and add them to the {@link DetailedMassBlockUpdate}
	 */
	public void setupBlockUpdater() {
		int chunkx = 0;
		int chunkz = 0;
		byte[] blocks = ((ByteArrayTag) schematic.getValue().get("Blocks")).getValue();
		byte[] datas = ((ByteArrayTag) schematic.getValue().get("Data")).getValue();
		short width = ((ShortTag) schematic.getValue().get("Width")).getValue();
		short length = ((ShortTag) schematic.getValue().get("Length")).getValue();
		short height = ((ShortTag) schematic.getValue().get("Height")).getValue();
		boolean firstrun = true;
		for (int x = 0; x < width; x++) {
			for (int z = 0; z < length; z++) {
				int x0 = x + location.getBlockX();
				int z0 = z + location.getBlockZ();
				if (x0 >> 4 != chunkx || z0 >> 4 != chunkz || firstrun) {
					if (firstrun) firstrun = false;
					this.addChunk(this.location.getWorld().getChunkAt(x0 >> 4, z0 >> 4));
					chunkx = x0 >> 4;
					chunkz = z0 >> 4;
				}
				for (int y = 0; y < height; y++) {
					int y0 = y + location.getBlockY();
					if (y0 > 256) break;
					int index = (y * length + z) * width + x;
					byte id = blocks[index];
					byte data = datas[index];
					if (pasteAir || id != 0) {
						this.addBlock(new BasicBlock((short) id, (short) data, x0, y0, z0));
					}
				}
			}
		}
	}
}
