package com.lily56.spiders2.common.entity.movement;

import net.minecraft.entity.ai.pathing.PathNodeType;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.ai.control.JumpControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.math.Vec3d;
import com.lily56.spiders2.common.entity.mob.IClimberEntity;
import com.lily56.spiders2.common.entity.mob.Orientation;
import net.minecraft.util.math.Box;

public class ClimberMoveController<T extends MobEntity & IClimberEntity> extends MoveControl {
	protected final IClimberEntity climber;

	@Nullable
	protected BlockPos block;

	@Nullable
	protected Direction side;

	public ClimberMoveController(T entity) {
		super(entity);
		this.climber = entity;
	}

	@Override
	public void moveTo(double x, double y, double z, double speed) {
		this.moveTo(x, y, z, null, null, speed);
	}

	public void moveTo(double x, double y, double z, BlockPos block, Direction side, double speed) {
		super.moveTo(x, y, z, speed);
		this.block = block;
		this.side = side;
	}

	@Override
	public void tick() {
		double speed = this.climber.getMovementSpeed() * this.speed;

		if(this.state == MoveControl.State.STRAFE) {
			this.state = MoveControl.State.WAIT;

			float forward = this.forwardMovement;
			float strafe = this.sidewaysMovement;
			float moveSpeed = MathHelper.sqrt(forward * forward + strafe * strafe);
			if(moveSpeed < 1.0F) {
				moveSpeed = 1.0F;
			}

			moveSpeed = (float)speed / moveSpeed;
			forward = forward * moveSpeed;
			strafe = strafe * moveSpeed;

			Orientation orientation = this.climber.getOrientation();

			Vec3d forwardVector = orientation.getGlobal(this.entity.getYaw(), 0);
			Vec3d strafeVector = orientation.getGlobal(this.entity.getYaw() - 90.0f, 0);

			if(!this.isWalkableAtOffset(forwardVector.x * forward + strafeVector.x * strafe, forwardVector.y * forward + strafeVector.y * strafe, forwardVector.z * forward + strafeVector.z * strafe)) {
				this.forwardMovement = 1.0F;
				this.sidewaysMovement = 0.0F;
			}

			this.entity.setMovementSpeed((float) speed);
			this.entity.setForwardSpeed(this.forwardMovement);
			this.entity.setSidewaysSpeed(this.sidewaysMovement);
		} else if(this.state == MoveControl.State.MOVE_TO) {
			this.state = MoveControl.State.WAIT;

			double dx = this.targetX - this.entity.getX();
			double dy = this.targetY - this.entity.getY();
			double dz = this.targetZ - this.entity.getZ();

			if(this.side != null && this.block != null) {
				VoxelShape shape = this.entity.world.getBlockState(this.block).getCollisionShape(this.entity.world, this.block);

				Box aabb = this.entity.getBoundingBox();

				double ox = 0;
				double oy = 0;
				double oz = 0;

				//Use offset towards pathing side if mob is above that pathing side
				switch(this.side) {
				case DOWN:
					if(aabb.minY >= this.block.getY() + shape.getMax(Direction.Axis.Y) - 0.01D) {
						ox -= 0.1D;
					}
					break;
				case UP:
					if(aabb.maxY <= this.block.getY() + shape.getMin(Direction.Axis.Y) + 0.01D) {
						oy += 0.1D;
					}
					break;
				case WEST:
					if(aabb.minX >= this.block.getX() + shape.getMax(Direction.Axis.X) - 0.01D) {
						ox -= 0.1D;
					}
					break;
				case EAST:
					if(aabb.maxX <= this.block.getX() + shape.getMin(Direction.Axis.X) + 0.01D) {
						ox += 0.1D;
					}
					break;
				case NORTH:
					if(aabb.minZ >= this.block.getZ() + shape.getMax(Direction.Axis.Z) - 0.01D) {
						oz -= 0.1D;
					}
					break;
				case SOUTH:
					if(aabb.maxZ <= this.block.getZ() + shape.getMin(Direction.Axis.Z) + 0.01D) {
						oz += 0.1D;
					}
					break;
				}

				Box blockAabb = new Box(this.block.offset(this.side.getOpposite()));

				//If mob is on the pathing side block then only apply the offsets if the block is above the according side of the voxel shape
				if(aabb.intersects(blockAabb)) {
					Direction.Axis offsetAxis = this.side.getAxis();
					double offset;

					switch(offsetAxis) {
					default:
					case X:
						offset = this.side.getOffsetX() * 0.5f;
						break;
					case Y:
						offset = this.side.getOffsetY() * 0.5f;
						break;
					case Z:
						offset = this.side.getOffsetZ() * 0.5f;
						break;
					}

					double allowedOffset = shape.calculateMaxDistance(offsetAxis, aabb.offset(-this.block.getX(), -this.block.getY(), -this.block.getZ()), offset);

					switch(this.side) {
					case DOWN:
						if(aabb.minY + allowedOffset < this.block.getY() + shape.getMax(Direction.Axis.Y) - 0.01D) {
							oy = 0;
						}
						break;
					case UP:
						if(aabb.maxY + allowedOffset > this.block.getY() + shape.getMin(Direction.Axis.Y) + 0.01D) {
							oy = 0;
						}
						break;
					case WEST:
						if(aabb.minX + allowedOffset < this.block.getX() + shape.getMax(Direction.Axis.X) - 0.01D) {
							ox = 0;
						}
						break;
					case EAST:
						if(aabb.maxX + allowedOffset > this.block.getX() + shape.getMin(Direction.Axis.X) + 0.01D) {
							ox = 0;
						}
						break;
					case NORTH:
						if(aabb.minZ + allowedOffset < this.block.getZ() + shape.getMax(Direction.Axis.Z) - 0.01D) {
							oz = 0;
						}
						break;
					case SOUTH:
						if(aabb.maxZ + allowedOffset > this.block.getZ() + shape.getMin(Direction.Axis.Z) + 0.01D) {
							oz = 0;
						}
						break;
					}
				}

				dx += ox;
				dy += oy;
				dz += oz;
			}

			Direction mainOffsetDir = Direction.getFacing(dx, dy, dz);

			float reach;
			switch(mainOffsetDir) {
			case DOWN:
				reach = 0;
				break;
			case UP:
				reach = this.entity.getHeight();
				break;
			default:
				reach = this.entity.getWidth() * 0.5f;
				break;
			}

			double verticalOffset = Math.abs(mainOffsetDir.getOffsetX() * dx) + Math.abs(mainOffsetDir.getOffsetY() * dy) + Math.abs(mainOffsetDir.getOffsetZ() * dz);

			Direction groundDir = this.climber.getGroundDirection().getLeft();

			Vec3d jumpDir = null;

			if(this.side != null && verticalOffset > reach - 0.05f && groundDir != this.side && groundDir.getAxis() != this.side.getAxis()) {
				double hdx = (1 - Math.abs(mainOffsetDir.getOffsetX())) * dx;
				double hdy = (1 - Math.abs(mainOffsetDir.getOffsetY())) * dy;
				double hdz = (1 - Math.abs(mainOffsetDir.getOffsetZ())) * dz;

				double hdsq = hdx * hdx + hdy * hdy + hdz * hdz;
				if(hdsq < 0.707f) {
					dx -= this.side.getOffsetX() * 0.2f;
					dy -= this.side.getOffsetY() * 0.2f;
					dz -= this.side.getOffsetZ() * 0.2f;

					if(hdsq < 0.1f) {
						jumpDir = new Vec3d(mainOffsetDir.getOffsetX(), mainOffsetDir.getOffsetY(), mainOffsetDir.getOffsetZ());
					}
				}
			}

			Orientation orientation = this.climber.getOrientation();

			Vec3d up = orientation.getGlobal(this.entity.getYaw(), -90);

			Vec3d offset = new Vec3d(dx, dy, dz);

			Vec3d targetDir = offset.subtract(up.multiply(offset.dotProduct(up)));
			double targetDist = targetDir.length();
			targetDir = targetDir.normalize();

			if(targetDist < 0.0001D) {
				this.entity.setForwardSpeed(0);
			} else {
				float rx = (float) orientation.localZ.dotProduct(targetDir);
				float ry = (float) orientation.localX.dotProduct(targetDir);
				this.entity.setYaw(this.wrapDegrees(this.entity.getYaw(), 270.0f - (float) Math.toDegrees(MathHelper.atan2(rx, ry)), 90.0f));

				if(jumpDir == null && this.side != null && targetDist < 0.1D && groundDir == this.side.getOpposite()) {
					jumpDir = new Vec3d(this.side.getOffsetX(), this.side.getOffsetY(), this.side.getOffsetZ());
				}
				
				if(jumpDir == null && this.side != null && Math.abs(this.climber.getGroundDirection().getRight().y) > 0.5f && (!this.climber.canAttachToSide(this.side) || !this.climber.canAttachToSide(Direction.getFacing(dx, dy, dz))) && this.targetY > this.entity.getY() + 0.1f && verticalOffset > this.entity.stepHeight) {
					jumpDir = new Vec3d(0, 1, 0);
				}

				if(jumpDir != null) {
					this.entity.setMovementSpeed((float) speed * 0.5f);

					JumpControl jumpController = this.entity.getJumpControl();

					if(jumpController instanceof ClimberJumpController) {
						((ClimberJumpController) jumpController).setJumping(jumpDir);
					}
				} else {
					this.entity.setMovementSpeed((float) speed);
				}
			}
		} else if(this.state == MoveControl.State.JUMPING) {
			this.entity.setMovementSpeed((float) speed);

			if(this.entity.isOnGround()) {
				this.state = MoveControl.State.WAIT;
			}
		} else {
			this.entity.setForwardSpeed(0);
		}
	}

	private boolean isWalkableAtOffset(double x, double y, double z) {
		EntityNavigation navigator = this.entity.getNavigation();

		if(navigator != null) {
			PathNodeMaker processor = navigator.getNodeMaker();

			if(processor != null && processor.getDefaultNodeType(this.entity.world, MathHelper.floor(this.entity.getX() + x), MathHelper.floor(this.entity.getY() + this.entity.getHeight() * 0.5f + y), MathHelper.floor(this.entity.getZ() + z)) != PathNodeType.WALKABLE) {
				return false;
			}
		}

		return true;
	}
}