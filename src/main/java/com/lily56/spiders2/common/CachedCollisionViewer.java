package com.lily56.spiders2.common;

import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
//import net.minecraft.block.BlockEntityProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.CollisionView;
import net.minecraft.world.BlockView;
import net.minecraft.world.border.WorldBorder;

import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

public class CachedCollisionViewer implements CollisionView {
	private final CollisionView collisionReader;
	private final BlockView[] blockReaderCache;
	private final int minChunkX, minChunkZ, width;

	public CachedCollisionViewer(CollisionView collisionViewer, Box box) {
		this.collisionReader = collisionViewer;

		this.minChunkX = ((MathHelper.floor(box.minX - 1.0E-7D) - 1) >> 4);
		int maxChunkX = ((MathHelper.floor(box.maxX + 1.0E-7D) + 1) >> 4);
		this.minChunkZ = ((MathHelper.floor(box.minZ - 1.0E-7D) - 1) >> 4);
		int maxChunkZ = ((MathHelper.floor(box.maxZ + 1.0E-7D) + 1) >> 4);

		this.width = maxChunkX - this.minChunkX + 1;
		int depth = maxChunkZ - this.minChunkZ + 1;

		BlockView[] blockReaderCache = new BlockView[width * depth];

		for(int cx = minChunkX; cx <= maxChunkX; cx++) {
			for(int cz = minChunkZ; cz <= maxChunkZ; cz++) {
				blockReaderCache[(cx - minChunkX) + (cz - minChunkZ) * width] = collisionViewer.getChunkAsView(cx, cz);
			}
		}

		this.blockReaderCache = blockReaderCache;
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		return this.collisionReader.getBlockEntity(pos);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return this.collisionReader.getBlockState(pos);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return this.collisionReader.getFluidState(pos);
	}

	@Override
	public WorldBorder getWorldBorder() {
		return this.collisionReader.getWorldBorder();
	}

	@Override
	public Stream<VoxelShape> getEntityCollisions(Entity entity, Box box, Predicate<Entity> predicate) {
		return this.collisionReader.getEntityCollisions(entity, box, predicate);
	}

	public BlockView getChunkAsView(int chunkX, int chunkZ) {
		return this.blockReaderCache[(chunkX - minChunkX) + (chunkZ - minChunkZ) * width];
	}

	/**
	 * Returns the difference in the {@linkplain #getBottomY() minimum} and
	 * {@linkplain #getTopY() maximum} height.
	 *
	 * <p>This is the number of blocks that can be modified in any vertical column
	 * within the view, or the vertical size, in blocks, of the view.
	 *
	 * @return the difference in the minimum and maximum height
	 * @see #getBottomY()
	 * @see #getTopY()
	 */
	@Override
	public int getHeight() {
		return this.getHeight();
	}

	/**
	 * Returns the bottom Y level, or height, inclusive, of this view.
	 *
	 * @see #getTopY()
	 * @see #getHeight()
	 */
	@Override
	public int getBottomY() {
		return this.getBottomY();
	}
}
