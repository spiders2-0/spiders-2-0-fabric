package com.lily56.spiders2.common.entity.movement;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;

import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.ai.pathing.PathMinHeap;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.world.chunk.ChunkCache;
/*
import net.minecraft.pathfinding.FlaggedPathPoint;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathHeap;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.world.Region;
 */



public class CustomPathNodeNavigator extends PathNodeNavigator {
	private final PathMinHeap path = new PathMinHeap();
	private final PathNode[] pathOptions = new PathNode[32];
	private final PathNodeMaker PathNodeMaker;

	private int maxExpansions = 200;

	public static interface Heuristic {
		public float compute(PathNode start, PathNode end, boolean isTargetHeuristic);
	}

	public static final Heuristic DEFAULT_HEURISTIC = (start, end, isTargetHeuristic) -> start.getManhattanDistance(end); //distanceManhattan

	private Heuristic heuristic = DEFAULT_HEURISTIC;

	public CustomPathNodeNavigator(PathNodeMaker processor, int maxExpansions) {
		super(processor, maxExpansions);
		this.PathNodeMaker = processor;
		this.maxExpansions = maxExpansions;
	}

	public PathNodeMaker getPathNodeMaker() {
		return this.PathNodeMaker;
	}

	public CustomPathNodeNavigator setMaxExpansions(int expansions) {
		this.maxExpansions = expansions;
		return this;
	}

	public CustomPathNodeNavigator setHeuristic(Heuristic heuristic) {
		this.heuristic = heuristic;
		return this;
	}

	@Nullable
	public Path NewPath(ChunkCache region, MobEntity entity, Set<BlockPos> checkpoints, float maxDistance, int checkpointRange, float maxExpansionsMultiplier) {
		this.path.clearPath();

		this.PathNodeMaker.init(region, entity);

		PathNode pathnode = this.PathNodeMaker.getStart();

		//Create a checkpoint for each block pos in the checkpoints set
		Map<TargetPathNode, BlockPos> checkpointsMap = checkpoints.stream().collect(Collectors.toMap((pos) -> {
			return this.PathNodeMaker.func_224768_a(pos.getX(), pos.getY(), pos.getZ());
		}, Function.identity()));

		Path path = this.findPath(pathnode, checkpointsMap, maxDistance, checkpointRange, maxExpansionsMultiplier);
		this.PathNodeMaker.clear();

		return path;
	}

	//TODO Re-implement custom heuristics

	@Nullable
	private Path findPath(PathNode start, Map<TargetPathNode, BlockPos> checkpointsMap, float maxDistance, int checkpointRange, float maxExpansionsMultiplier) {
		Set<TargetPathNode> checkpoints = checkpointsMap.keySet();

		start.totalPathDistance = 0.0F;
		start.distanceToNext = this.computeHeuristic(start, checkpoints);
		start.distanceToTarget = start.distanceToNext;

		this.path.clearPath();
		this.path.addPoint(start);

		Set<TargetPathNode> reachedCheckpoints = Sets.newHashSetWithExpectedSize(checkpoints.size());

		int expansions = 0;
		int maxExpansions = (int) (this.maxExpansions * maxExpansionsMultiplier);

		while(!this.path.isPathEmpty() && ++expansions < maxExpansions) {
			PathNode openPathPoint = this.path.dequeue();
			openPathPoint.visited = true;

			for(TargetPathNode checkpoint : checkpoints) {
				if(openPathPoint.func_224757_c(checkpoint) <= checkpointRange) {
					checkpoint.func_224764_e();
					reachedCheckpoints.add(checkpoint);
				}
			}

			if(!reachedCheckpoints.isEmpty()) {
				break;
			}

			if(openPathPoint.distanceTo(start) < maxDistance) {
				int numOptions = this.PathNodeMaker.func_222859_a(this.pathOptions, openPathPoint);

				for(int i = 0; i < numOptions; ++i) {
					PathNode successorPathPoint = this.pathOptions[i];

					float costHeuristic = openPathPoint.distanceTo(successorPathPoint); //TODO Replace with cost heuristic

					//field_222861_j corresponds to the total path cost of the evaluation function
					successorPathPoint.field_222861_j = openPathPoint.field_222861_j + costHeuristic;

					float totalSuccessorPathCost = openPathPoint.totalPathDistance + costHeuristic + successorPathPoint.costMalus;

					if(successorPathPoint.field_222861_j < maxDistance && (!successorPathPoint.isAssigned() || totalSuccessorPathCost < successorPathPoint.totalPathDistance)) {
						successorPathPoint.previous = openPathPoint;
						successorPathPoint.totalPathDistance = totalSuccessorPathCost;

						//distanceToNext corresponds to the heuristic part of the evaluation function
						successorPathPoint.distanceToNext = this.computeHeuristic(successorPathPoint, checkpoints) * 1.0f; //TODO Vanilla's 1.5 multiplier is too greedy :( Move to custom heuristic stuff

						if(successorPathPoint.isAssigned()) {
							this.path.changeDistance(successorPathPoint, successorPathPoint.totalPathDistance + successorPathPoint.distanceToNext);
						} else {
							//distanceToTarget corresponds to the evaluation function, i.e. total path cost + heuristic
							successorPathPoint.distanceToTarget = successorPathPoint.totalPathDistance + successorPathPoint.distanceToNext;
							this.path.addPoint(successorPathPoint);
						}
					}
				}
			}
		}

		Optional<Path> path;

		if(!reachedCheckpoints.isEmpty()) {
			//Use shortest path towards next reached checkpoint
			path = reachedCheckpoints.stream().map((checkpoint) -> {
				return this.createPath(checkpoint.func_224763_d(), checkpointsMap.get(checkpoint), true);
			}).min(Comparator.comparingInt(Path::getCurrentPathLength));
		} else {
			//Use lowest cost path towards any checkpoint
			path = checkpoints.stream().map((checkpoint) -> {
				return this.createPath(checkpoint.func_224763_d(), checkpointsMap.get(checkpoint), false);
			}).min(Comparator.comparingDouble(Path::func_224769_l /*TODO Replace calculation with cost heuristic*/).thenComparingInt(Path::getCurrentPathLength));
		}

		return !path.isPresent() ? null : path.get();
	}

	private float computeHeuristic(PathNode pathPoint, Set<TargetPathNode> checkpoints) {
		float minDst = Float.MAX_VALUE;

		for(TargetPathNode checkpoint : checkpoints) {
			float dst = pathPoint.distanceTo(checkpoint); //TODO Replace with target heuristic
			checkpoint.func_224761_a(dst, pathPoint);
			minDst = Math.min(dst, minDst);
		}

		return minDst;
	}

	protected Path createPath(PathNode start, BlockPos target, boolean isTargetReached) {
		List<PathNode> points = Lists.newArrayList();

		PathNode currentPathPoint = start;
		points.add(0, start);

		while(currentPathPoint.previous != null) {
			currentPathPoint = currentPathPoint.previous;
			points.add(0, currentPathPoint);
		}

		return new Path(points, target, isTargetReached);
	}
}
