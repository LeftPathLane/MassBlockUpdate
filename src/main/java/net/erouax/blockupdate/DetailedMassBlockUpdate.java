package net.erouax.blockupdate;

import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_8_R3.BlockPosition;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@RequiredArgsConstructor
public class DetailedMassBlockUpdate {

	private final Deque<BasicBlock> blockQueue = new ArrayDeque<>();
	private final Set<Chunk> chunkQueue = new HashSet<>();
	protected final World world;
	private final Plugin plugin;
	private final int rateLimit;

	public void addBlock(BasicBlock block) {
		blockQueue.add(block);
	}

	public void addChunk(Chunk chunk) {
		chunkQueue.add(chunk);
	}

	public void runDebug() {
		new DetailedBlockUpdateTask(blockQueue, rateLimit, true).runTaskTimer(plugin, 0L, 1L);
	}

	public void run() {
		new DetailedBlockUpdateTask(blockQueue, rateLimit, false).runTaskTimer(plugin, 0L, 1L);
	}

	private class DetailedBlockUpdateTask extends BukkitRunnable {

		private final Deque<BasicBlock> blocks;
		private final net.minecraft.server.v1_8_R3.World nmsWorld;
		private final boolean debug;
		private final int rateLimit;

		private final List<Long> loadTimes;
		private int ticks;

		public DetailedBlockUpdateTask(Deque<BasicBlock> blocks, int rateLimit, boolean debug) {
			this.blocks = blocks;
			this.nmsWorld = ((CraftWorld) world).getHandle();
			this.debug = debug;
			this.rateLimit = rateLimit;

			loadTimes = new ArrayList<>();
		}

		private void updateBlock(BasicBlock block) {
			net.minecraft.server.v1_8_R3.Chunk chunk = this.nmsWorld.getChunkAt(block.getX() >> 4, block.getZ() >> 4);
			chunk.a(new BlockPosition(block.getX(), block.getY(), block.getZ()), net.minecraft.server.v1_8_R3.Block.getByCombinedId(block.getData() << 12 | block.getId()));
		}

		@Override
		public void run() {
			long start = System.currentTimeMillis();

			for (int i = 0; i < this.rateLimit; i++) {
				if (this.blocks.isEmpty()) {
					break;
				}
				this.updateBlock(blocks.remove());
			}

			long end = System.currentTimeMillis();
			if (this.debug) {
				this.loadTimes.add(end - start);
				this.ticks++;
			}

			if (this.blocks.isEmpty()) {
				this.onFinish();
				this.cancel();
			}
		}

		private void onFinish() {
			long start = System.currentTimeMillis();
			for (Chunk chunk : chunkQueue) {
				chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
			}
			long end = System.currentTimeMillis();

			if (!this.debug) {
				return;
			}

			long total = 0;
			long max = 0;

			for (Long l : this.loadTimes) {
				total += l;
				if (l > max) {
					max = l;
				}
			}

			double average = (double) total / (double) this.loadTimes.size();

			if (this.debug) {
				System.out.println(" ---------- SCHEMATIC PASTE ----------");
				System.out.println(" Added load per tick: " + average + " ms");
				System.out.println(" Highest load: " + max + " ms");
				System.out.println(" Total ticks run: " + this.ticks);
				System.out.println(" Refreshing Chunk took " + (end - start) + " ms");
				System.out.println(" -------------------------------------");
			}
		}
	}


}
