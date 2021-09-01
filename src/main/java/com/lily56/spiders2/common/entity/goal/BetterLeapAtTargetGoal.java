package com.lily56.spiders2.common.entity.goal;

import java.util.EnumSet;

import net.minecraft.entity.Entity;
import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import com.lily56.spiders2.common.entity.mob.IClimberEntity;
import com.lily56.spiders2.common.entity.mob.Orientation;

public class BetterLeapAtTargetGoal<T extends MobEntity & IClimberEntity> extends Goal {
	private final T leaper;
	private final float leapMotionY;

	private LivingEntity leapTarget;
	private Vec3d forwardJumpDirection;
	private Vec3d upwardJumpDirection;

	public BetterLeapAtTargetGoal(T leapingEntity, float leapMotionYIn) {
		this.leaper = leapingEntity;
		this.leapMotionY = leapMotionYIn;
		this.setControls(EnumSet.of(Control.JUMP, Control.MOVE));
	}

	@Override
	public boolean shouldExecute() {
		if(this.leaper.hasPassengers()) {
			this.leapTarget = this.leaper.getTarget();

			if(this.leapTarget != null && this.leaper.isOnGround()) {
				Triple<Vec3d, Vec3d, Vec3d> projectedVector = this.getProjectedVector(this.leapTarget.getPos());

				double dstSq = projectedVector.getLeft().lengthSquared();
				double dstSqDot = projectedVector.getMiddle().lengthSquared();

				if(dstSq >= 4.0D && dstSq <= 16.0D && dstSqDot <= 1.2f && this.leaper.getRandom().nextInt(5) == 0) {
					this.forwardJumpDirection = projectedVector.getLeft().normalize();
					this.upwardJumpDirection = projectedVector.getRight().normalize();
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean shouldContinueExecuting() {
		return !this.leaper.isOnGround();
	}

	@Override
	public void startExecuting() {
		Vec3d motion = this.leaper.getVelocity();

		Vec3d jumpVector = this.forwardJumpDirection;

		if(jumpVector.lengthSquared() > 1.0E-7D) {
			jumpVector = jumpVector.normalize().multiply(0.4D).add(motion.multiply(0.2D));
		}

		jumpVector = jumpVector.add(this.upwardJumpDirection.multiply(this.leapMotionY));
		jumpVector = new Vec3d(jumpVector.x * (1 - Math.abs(this.upwardJumpDirection.x)), jumpVector.y, jumpVector.z * (1 - Math.abs(this.upwardJumpDirection.z)));

		this.leaper.setVelocity(jumpVector);

		Orientation orientation = this.leaper.getOrientation();

		float rx = (float) orientation.localZ.dotProduct(jumpVector);
		float ry = (float) orientation.localX.dotProduct(jumpVector);

		this.leaper.setYaw(270.0f - (float) Math.toDegrees(MathHelper.atan2(rx, ry)));
	}

	protected Triple<Vec3d, Vec3d, Vec3d> getProjectedVector(Vec3d target) {
		Orientation orientation = this.leaper.getOrientation();
		Vec3d up = orientation.getGlobal(this.leaper.getYaw(), -90.0f);
		Vec3d diff = target.subtract(this.leaper.getPos());
		Vec3d dot = up.multiply(up.dotProduct(diff));
		return Triple.of(diff.subtract(dot), dot, up);
	}

	@Override
	public boolean canStart() {
		return false;
	}
}
