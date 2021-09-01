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

public class CachedCollisionReader implements CollisionView {
	private final CollisionView collisionReader;
	private final BlockView[] blockReaderCache;
	private final int minChunkX, minChunkZ, width;

	public CachedCollisionReader(CollisionView collisionReader, Box aabb) {
		this.collisionReader = collisionReader;

		this.minChunkX = ((MathHelper.floor(aabb.minX - 1.0E-7D) - 1) >> 4);
		int maxChunkX = ((MathHelper.floor(aabb.maxX + 1.0E-7D) + 1) >> 4);
		this.minChunkZ = ((MathHelper.floor(aabb.minZ - 1.0E-7D) - 1) >> 4);
		int maxChunkZ = ((MathHelper.floor(aabb.maxZ + 1.0E-7D) + 1) >> 4);

		this.width = maxChunkX - this.minChunkX + 1;
		int depth = maxChunkZ - this.minChunkZ + 1;

		BlockView[] blockReaderCache = new BlockView[width * depth];

		for(int cx = minChunkX; cx <= maxChunkX; cx++) {
			for(int cz = minChunkZ; cz <= maxChunkZ; cz++) {
				blockReaderCache[(cx - minChunkX) + (cz - minChunkZ) * width] = collisionReader.getChunkAsView(cx, cz);
			}
		}

		this.blockReaderCache = blockReaderCache;
	}

	@Override
	public BlockEntity getTileEntity(BlockPos pos) {
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
	public Stream<VoxelShape> getEntityCollisions(Entity entity, Box aabb, Predicate<Entity> predicate) {
		return this.collisionReader.getEntityCollisions(entity, aabb, predicate);
	}

	@Override
	public BlockView getBlockReader(int chunkX, int chunkZ) {
		return this.blockReaderCache[(chunkX - minChunkX) + (chunkZ - minChunkZ) * width];
	}
}
