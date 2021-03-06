package com.lily56.spiders2.common.entity.movement;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import com.lily56.spiders2.common.entity.mob.IClimberEntity;
import com.lily56.spiders2.common.entity.mob.Orientation;

public class AdvancedClimberPathNavigator<T extends MobEntity & IClimberEntity> extends AdvancedGroundPathNavigator<T> {
	protected final IClimberEntity climber;

	protected Direction verticalFacing = Direction.DOWN;

	protected boolean findDirectPathNodes = false;

	public AdvancedClimberPathNavigator(T entity, World worldIn, boolean checkObstructions, boolean canPathWalls, boolean canPathCeiling) {
		super(entity, worldIn, checkObstructions);

		this.climber = entity;

		if(this.nodeMaker instanceof AdvancedLandPathNodeMaker) {
			AdvancedLandPathNodeMaker processor = (AdvancedLandPathNodeMaker) this.nodeMaker;
			processor.setStartPathOnGround(false);
			processor.setCanPathWalls(canPathWalls);
			processor.setCanPathCeiling(canPathCeiling);
		}
	}
//lots TODO in here..
	@Override
	protected Vec3d getPos() {
		return this.entity.getPos().add(0, this.entity.getHeight() / 2.0f, 0);
	}

	@Override
	@Nullable
	public Path findPathTo(BlockPos pos, int checkpointRange) {
		return this.findPathToAny(ImmutableSet.of(pos), 8, false, checkpointRange);
	}

	@Override
	@Nullable
	public Path findPathTo(Entity entityIn, int checkpointRange) {
		return this.findPathToAny(ImmutableSet.of(entityIn.getBlockPos()), 16, true, checkpointRange);
	}

	@Override
	public void tick() {
		++this.tickCount;

		if(this.shouldRecalculate) {
			this.recalculatePath();
		}

		if(!this.isIdle()) {
			if(this.isAtValidPosition()) {
				this.continueFollowingPath();
			} else if(this.currentPath != null && !this.currentPath.isFinished()) {
				Vec3d pos = this.getPos();
				Vec3d targetPos = this.currentPath.getNodePosition(this.entity);

				if(pos.y > targetPos.y && !this.entity.isOnGround() && MathHelper.floor(pos.x) == MathHelper.floor(targetPos.x) && MathHelper.floor(pos.z) == MathHelper.floor(targetPos.z)) {
					this.currentPath.next();
				}
			}

			DebugInfoSender.sendPathfindingData(this.world, this.entity, this.currentPath, this.nodeReachProximity);

			if(!this.isIdle()) {
				PathNode targetPoint = this.currentPath.getNode(this.currentPath.getCurrentNodeIndex());

				Direction dir = null;

				if(targetPoint instanceof DirectionalPathNode) {
					dir = ((DirectionalPathNode) targetPoint).getPathSide();
				}

				if(dir == null) {
					dir = Direction.DOWN;
				}

				Vec3d targetPos = this.getExactPathingTarget(this.world, targetPoint.getBlockPos(), dir);

				MoveControl moveController = this.entity.getMoveControl();

				if(moveController instanceof ClimberMoveController && targetPoint instanceof DirectionalPathNode && ((DirectionalPathNode) targetPoint).getPathSide() != null) {
					((ClimberMoveController) moveController).moveTo(targetPos.x, targetPos.y, targetPos.z, targetPoint.getBlockPos().offset(dir), ((DirectionalPathNode) targetPoint).getPathSide(), this.speed);
				} else {
					moveController.moveTo(targetPos.x, targetPos.y, targetPos.z, this.speed);
				}
			}
		}
	}

	public Vec3d getExactPathingTarget(BlockView blockaccess, BlockPos pos, Direction dir) {
		BlockPos offsetPos = pos.offset(dir);

		VoxelShape shape = blockaccess.getBlockState(offsetPos).getCollisionShape(blockaccess, offsetPos);

		Direction.Axis axis = dir.getAxis();

		int sign = dir.getOffsetX() + dir.getOffsetY() + dir.getOffsetZ();
		double offset = shape.isEmpty() ? sign /*undo offset if no collider*/ : (sign > 0 ? shape.getMin(axis) - 1 : shape.getMax(axis));

		double marginXZ = 1 - (this.entity.getWidth() % 1);
		double marginY = 1 - (this.entity.getHeight() % 1);

		double pathingOffsetXZ = (int)(this.entity.getWidth() + 1.0F) * 0.5D;
		double pathingOffsetY = (int)(this.entity.getHeight() + 1.0F) * 0.5D - this.entity.getHeight() * 0.5f;

		double x = offsetPos.getX() + pathingOffsetXZ + dir.getOffsetX() * marginXZ;
		double y = offsetPos.getY() + pathingOffsetY  + (dir == Direction.DOWN ? -pathingOffsetY : 0.0D) + (dir == Direction.UP ? -pathingOffsetY + marginY : 0.0D);
		double z = offsetPos.getZ() + pathingOffsetXZ + dir.getOffsetZ() * marginXZ;

		switch(axis) {
		default:
		case X:
			return new Vec3d(x + offset, y, z);
		case Y:
			return new Vec3d(x, y + offset, z);
		case Z:
			return new Vec3d(x, y, z + offset);
		}
	}

	@Override
	protected void continueFollowingPath() {
		Vec3d pos = this.getPos();

		this.nodeReachProximity = this.entity.getWidth() > 0.75F ? this.entity.getWidth() / 2.0F : 0.75F - this.entity.getWidth() / 2.0F;
		float maxDistanceToWaypointY = Math.max(1 /*required for e.g. slabs*/, this.entity.getHeight() > 0.75F ? this.entity.getHeight() / 2.0F : 0.75F - this.entity.getHeight() / 2.0F);

		int sizeX = MathHelper.ceil(this.entity.getWidth());
		int sizeY = MathHelper.ceil(this.entity.getHeight());
		int sizeZ = sizeX;

		Orientation orientation = this.climber.getOrientation();
		Vec3d upVector = orientation.getGlobal(this.entity.getYaw(), -90);

		this.verticalFacing = Direction.getFacing((float) upVector.x, (float) upVector.y, (float) upVector.z);

		//Look up to 4 nodes ahead, so it doesn't backtrack on positions with multiple path sides when changing/updating path
		for(int i = 4; i >= 0; i--) {
			if(this.currentPath.getCurrentNodeIndex() + i < this.currentPath.getLength()) {
				PathNode currentTarget = this.currentPath.getNode(this.currentPath.getCurrentNodeIndex() + i);

				double dx = Math.abs(currentTarget.x + (int) (this.entity.getWidth() + 1.0f) * 0.5f - this.entity.getX());
				double dy = Math.abs(currentTarget.y - this.entity.getY());
				double dz = Math.abs(currentTarget.z + (int) (this.entity.getWidth() + 1.0f) * 0.5f - this.entity.getZ());

				boolean isWaypointInReach = dx < this.nodeReachProximity && dy < maxDistanceToWaypointY && dz < this.nodeReachProximity;

				boolean isOnSameSideAsTarget = false;
				if(this.canSwim() && (currentTarget.type == PathNodeType.WATER || currentTarget.type == PathNodeType.WATER_BORDER || currentTarget.type == PathNodeType.LAVA)) {
					isOnSameSideAsTarget = true;
				} else if(currentTarget instanceof DirectionalPathNode) {
					Direction targetSide = ((DirectionalPathNode) currentTarget).getPathSide();
					isOnSameSideAsTarget = targetSide == null || this.climber.getGroundDirection().getLeft() == targetSide;
				} else {
					isOnSameSideAsTarget = true;
				}

				if(isOnSameSideAsTarget && (isWaypointInReach || (i == 0 && this.entity.canJumpToNextPathNode(this.currentPath.getCurrentNode().type) && this.isNextTargetInLine(pos, sizeX, sizeY, sizeZ, 1 + i)))) {
					this.currentPath.setCurrentNodeIndex(this.currentPath.getCurrentNodeIndex() + 1 + i);
					break;
				}
			}
		}

		if(this.findDirectPathNodes) {
			Direction.Axis verticalAxis = this.verticalFacing.getAxis();

			int firstDifferentHeightPoint = this.currentPath.getLength();

			switch(verticalAxis) {
			case X:
				for(int i = this.currentPath.getCurrentNodeIndex(); i < this.currentPath.getLength(); ++i) {
					if(this.currentPath.getNode(i).x != Math.floor(pos.x)) {
						firstDifferentHeightPoint = i;
						break;
					}
				}
				break;
			case Y:
				for(int i = this.currentPath.getCurrentNodeIndex(); i < this.currentPath.getLength(); ++i) {
					if(this.currentPath.getNode(i).y != Math.floor(pos.y)) {
						firstDifferentHeightPoint = i;
						break;
					}
				}
				break;
			case Z:
				for(int i = this.currentPath.getCurrentNodeIndex(); i < this.currentPath.getLength(); ++i) {
					if(this.currentPath.getNode(i).z != Math.floor(pos.z)) {
						firstDifferentHeightPoint = i;
						break;
					}
				}
				break;
			}

			for(int i = firstDifferentHeightPoint - 1; i >= this.currentPath.getCurrentNodeIndex(); --i) {
				if(this.canPathDirectlyThrough(pos, this.currentPath.getNodePosition(this.entity, i), sizeX, sizeY, sizeZ)) {
					this.currentPath.setCurrentNodeIndex(i);
					break;
				}
			}
		}

		this.checkTimeouts(pos);
	}
	private boolean isNextTargetInLine(Vec3d pos, int sizeX, int sizeY, int sizeZ, int offset) {
		if(this.currentPath.getCurrentNodeIndex() + offset >= this.currentPath.getLength()) {
			return false;
		} else {
			Vec3d currentTarget = Vec3d.ofBottomCenter(this.currentPath.getCurrentNodePos());

			if(!pos.isInRange(currentTarget, 2.0D)) {
				return false;
			} else {
				Vec3d nextTarget = Vec3d.ofBottomCenter(this.currentPath.getNodePos(this.currentPath.getCurrentNodeIndex() + offset));
				Vec3d targetDir = nextTarget.subtract(currentTarget);
				Vec3d currentDir = pos.subtract(currentTarget);

				if(targetDir.dotProduct(currentDir) > 0.0D) {
					Direction.Axis ax, ay, az;
					boolean invertY;

					switch(this.verticalFacing.getAxis()) {
					case X:
						ax = Direction.Axis.Z;
						ay = Direction.Axis.X;
						az = Direction.Axis.Y;
						invertY = this.verticalFacing.getOffsetX() < 0;
						break;
					default:
					case Y:
						ax = Direction.Axis.X;
						ay = Direction.Axis.Y;
						az = Direction.Axis.Z;
						invertY = this.verticalFacing.getOffsetY() < 0;
						break;
					case Z:
						ax = Direction.Axis.Y;
						ay = Direction.Axis.Z;
						az = Direction.Axis.X;
						invertY = this.verticalFacing.getOffsetZ() < 0;
						break;
					}

					//Make sure that the mob can stand at the next point in the same orientation it currently has
					return this.isSafeToStandAt(MathHelper.floor(nextTarget.x), MathHelper.floor(nextTarget.y), MathHelper.floor(nextTarget.z), sizeX, sizeY, sizeZ, currentTarget, 0, 0, -1, ax, ay, az, invertY);
				}

				return false;
			}
		}
	}

	@Override
	protected boolean canPathDirectlyThrough(Vec3d start, Vec3d end, int sizeX, int sizeY, int sizeZ) {
		switch(this.verticalFacing.getAxis()) {
		case X:
			return this.canPathDirectlyThrough(start, end, sizeX, sizeY, sizeZ, Direction.Axis.Z, Direction.Axis.X, Direction.Axis.Y, 0.0D, this.verticalFacing.getOffsetX() < 0);
		case Y:
			return this.canPathDirectlyThrough(start, end, sizeX, sizeY, sizeZ, Direction.Axis.X, Direction.Axis.Y, Direction.Axis.Z, 0.0D, this.verticalFacing.getOffsetY() < 0);
		case Z:
			return this.canPathDirectlyThrough(start, end, sizeX, sizeY, sizeZ, Direction.Axis.Y, Direction.Axis.Z, Direction.Axis.X, 0.0D, this.verticalFacing.getOffsetZ() < 0);
		}
		return false;
	}

	protected static double swizzle(Vec3d vec, Direction.Axis axis) {
		switch(axis) {
		case X:
			return vec.x;
		case Y:
			return vec.y;
		case Z:
			return vec.z;
		}
		return 0;
	}

	protected static int swizzle(int x, int y, int z, Direction.Axis axis) {
		switch(axis) {
		case X:
			return x;
		case Y:
			return y;
		case Z:
			return z;
		}
		return 0;
	}

	protected static int unswizzle(int x, int y, int z, Direction.Axis ax, Direction.Axis ay, Direction.Axis az, Direction.Axis axis) {
		Direction.Axis unswizzle;
		if(axis == ax) {
			unswizzle = Direction.Axis.X;
		} else if(axis == ay) {
			unswizzle = Direction.Axis.Y;
		} else {
			unswizzle = Direction.Axis.Z;
		}
		return swizzle(x, y, z, unswizzle);
	}

	protected boolean canPathDirectlyThrough(Vec3d start, Vec3d end, int sizeX, int sizeY, int sizeZ, Direction.Axis ax, Direction.Axis ay, Direction.Axis az, double minDotProduct, boolean invertY) {
		int bx = MathHelper.floor(swizzle(start, ax));
		int bz = MathHelper.floor(swizzle(start, az));
		double dx = swizzle(end, ax) - swizzle(start, ax);
		double dz = swizzle(end, az) - swizzle(start, az);
		double dSq = dx * dx + dz * dz;

		int by = (int) swizzle(start, ay);

		int sizeX2 = swizzle(sizeX, sizeY, sizeZ, ax);
		int sizeY2 = swizzle(sizeX, sizeY, sizeZ, ay);
		int sizeZ2 = swizzle(sizeX, sizeY, sizeZ, az);

		if(dSq < 1.0E-8D) {
			return false;
		} else {
			double d3 = 1.0D / Math.sqrt(dSq);
			dx = dx * d3;
			dz = dz * d3;
			sizeX2 = sizeX2 + 2;
			sizeZ2 = sizeZ2 + 2;

			if(!this.isSafeToStandAt(
					unswizzle(bx, by, bz, ax, ay, az, Direction.Axis.X), unswizzle(bx, by, bz, ax, ay, az, Direction.Axis.Y), unswizzle(bx, by, bz, ax, ay, az, Direction.Axis.Z),
					unswizzle(sizeX2, sizeY2, sizeZ2, ax, ay, az, Direction.Axis.X), unswizzle(sizeX2, sizeY2, sizeZ2, ax, ay, az, Direction.Axis.Y), unswizzle(sizeX2, sizeY2, sizeZ2, ax, ay, az, Direction.Axis.Z),
					start, dx, dz, minDotProduct, ax, ay, az, invertY)) {
				return false;
			} else {
				sizeX2 = sizeX2 - 2;
				sizeZ2 = sizeZ2 - 2;
				double stepX = 1.0D / Math.abs(dx);
				double stepZ = 1.0D / Math.abs(dz);
				double relX = (double)bx - swizzle(start, ax);
				double relZ = (double)bz - swizzle(start, az);

				if(dx >= 0.0D) {
					++relX;
				}

				if(dz >= 0.0D) {
					++relZ;
				}

				relX = relX / dx;
				relZ = relZ / dz;
				int dirX = dx < 0.0D ? -1 : 1;
				int dirZ = dz < 0.0D ? -1 : 1;
				int ex = MathHelper.floor(swizzle(end, ax));
				int ez = MathHelper.floor(swizzle(end, az));
				int offsetX = ex - bx;
				int offsetZ = ez - bz;

				while(offsetX * dirX > 0 || offsetZ * dirZ > 0) {
					if(relX < relZ) {
						relX += stepX;
						bx += dirX;
						offsetX = ex - bx;
					} else {
						relZ += stepZ;
						bz += dirZ;
						offsetZ = ez - bz;
					}

					if(!this.isSafeToStandAt(
							unswizzle(bx, by, bz, ax, ay, az, Direction.Axis.X), unswizzle(bx, by, bz, ax, ay, az, Direction.Axis.Y), unswizzle(bx, by, bz, ax, ay, az, Direction.Axis.Z),
							unswizzle(sizeX2, sizeY2, sizeZ2, ax, ay, az, Direction.Axis.X), unswizzle(sizeX2, sizeY2, sizeZ2, ax, ay, az, Direction.Axis.Y), unswizzle(sizeX2, sizeY2, sizeZ2, ax, ay, az, Direction.Axis.Z),
							start, dx, dz, minDotProduct, ax, ay, az, invertY)) {
						return false;
					}
				}

				return true;
			}
		}
	}

	protected boolean isSafeToStandAt(int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vec3d start, double dx, double dz, double minDotProduct, Direction.Axis ax, Direction.Axis ay, Direction.Axis az, boolean invertY) {
		int sizeX2 = swizzle(sizeX, sizeY, sizeZ, ax);
		int sizeZ2 = swizzle(sizeX, sizeY, sizeZ, az);

		int bx = swizzle(x, y, z, ax) - sizeX2 / 2;
		int bz = swizzle(x, y, z, az) - sizeZ2 / 2;

		int by = swizzle(x, y, z, ay);

		if(!this.isPositionClear(
				unswizzle(bx, y, bz, ax, ay, az, Direction.Axis.X), unswizzle(bx, y, bz, ax, ay, az, Direction.Axis.Y), unswizzle(bx, y, bz, ax, ay, az, Direction.Axis.Z),
				sizeX, sizeY, sizeZ, start, dx, dz, minDotProduct, ax, ay, az)) {
			return false;
		} else {
			for(int obx = bx; obx < bx + sizeX2; ++obx) {
				for(int obz = bz; obz < bz + sizeZ2; ++obz) {
					double offsetX = (double)obx + 0.5D - swizzle(start, ax);
					double offsetZ = (double)obz + 0.5D - swizzle(start, az);

					if(offsetX * dx + offsetZ * dz >= minDotProduct) {
						PathNodeType nodeTypeBelow = this.nodeMaker.getNodeType(
								this.world,
								unswizzle(obx, by + (invertY ? 1 : -1), obz, ax, ay, az, Direction.Axis.X), unswizzle(obx, by + (invertY ? 1 : -1), obz, ax, ay, az, Direction.Axis.Y), unswizzle(obx, by + (invertY ? 1 : -1), obz, ax, ay, az, Direction.Axis.Z),
								this.entity, sizeX, sizeY, sizeZ, true, true);

						if(nodeTypeBelow == PathNodeType.WATER) {
							return false;
						}

						if(nodeTypeBelow == PathNodeType.LAVA) {
							return false;
						}

						if(nodeTypeBelow == PathNodeType.OPEN) {
							return false;
						}

						PathNodeType type = this.nodeMaker.getNodeType(
								this.world,
								unswizzle(obx, by, obz, ax, ay, az, Direction.Axis.X), unswizzle(obx, by, obz, ax, ay, az, Direction.Axis.Y), unswizzle(obx, by, obz, ax, ay, az, Direction.Axis.Z),
								this.entity, sizeX, sizeY, sizeZ, true, true);
						float f = this.entity.getPathfindingPenalty(type);

						if(f < 0.0F || f >= 8.0F) {
							return false;
						}

						if(type == PathNodeType.DAMAGE_FIRE || type == PathNodeType.DANGER_FIRE || type == PathNodeType.DAMAGE_OTHER) {
							return false;
						}
					}
				}
			}

			return true;
		}
	}

	protected boolean isPositionClear(int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vec3d start, double dx, double dz, double minDotProduct, Direction.Axis ax, Direction.Axis ay, Direction.Axis az) {
		for(BlockPos pos : BlockPos.iterate(new BlockPos(x, y, z), new BlockPos(x + sizeX - 1, y + sizeY - 1, z + sizeZ - 1))) {
			double offsetX = swizzle(pos.getX(), pos.getY(), pos.getZ(), ax) + 0.5D - swizzle(start, ax);
			double pffsetZ = swizzle(pos.getX(), pos.getY(), pos.getZ(), az) + 0.5D - swizzle(start, az);

			if(offsetX * dx + pffsetZ * dz >= minDotProduct) {
				BlockState state = this.world.getBlockState(pos);

				if(!state.canPathfindThrough(this.world, pos, NavigationType.LAND)) {
					return false;
				}
			}
		}

		return true;
	}
}
