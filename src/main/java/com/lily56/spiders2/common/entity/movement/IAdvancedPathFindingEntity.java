package com.lily56.spiders2.common.entity.movement;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;

public interface IAdvancedPathFindingEntity {
	/**
	 * The side on which the entity is currently walking
	 * @return
	 */
	public default Direction getGroundSide() {
		return Direction.DOWN;
	}
	
	/**
	 * Called when the mob tries to move along the path but is obstructed
	 */
	public void onPathingObstructed(Direction facing);

	/**
	 * Returns how many ticks the mob can be stuck before the path is considered to be obstructed
	 * @return
	 */
	public default int getMaxStuckCheckTicks() {
		return 40;
	}

	/**
	 * Returns the pathing malus for building a bridge
	 * @param entity
	 * @param pos
	 * @param fallPathPoint
	 * @return
	 */
	public default float getBridgePathingMalus(MobEntity entity, BlockPos pos, @Nullable PathNode fallPathPoint) {
		return -1.0f;
	}

	/**
	 * Returns the pathing malus for the given {@link PathNodeType} and block position.
	 * Nodes with negative values are avoided at all cost. Nodes with value 0.0 have the highest priority, i.e.
	 * are preferred over all other nodes. Nodes with a positive value incur an additional travel cost of the same magnitude
	 * and the higher their value the less they are preferred. Note that the additional travel cost increases the path's "length" (i.e. cost)
	 * and thus decreases the actual maximum path length in blocks.
	 * @param cache
	 * @param nodeType
	 * @param pos
	 * @param direction
	 * @param sides
	 * @return
	 */
	public default float getPathingMalus(BlockView cache, MobEntity entity, PathNodeType nodeType, BlockPos pos, Vec3i direction, Predicate<Direction> sides) {
		return entity.getPathfindingPenalty(nodeType);
	}

	/**
	 * Called after the path finder has finished finding a path.
	 * Can e.g. be used to clear caches.
	 */
	public default void pathFinderCleanup() {

	}
}
