package com.lily56.spiders2.common.entity.movement;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.util.math.Vec3d;
import com.lily56.spiders2.common.entity.mob.IClimberEntity;

import java.util.Optional;

public class ClimberLookController<T extends MobEntity & IClimberEntity> extends LookControl {
	protected final IClimberEntity climber;

	public ClimberLookController(T entity) {
		super(entity);
		this.climber = entity;
	}

	@Override
	protected Optional<Float> getTargetPitch() {
		Vec3d dir = new Vec3d(this.lookX - this.entity.getX(), this.lookY - this.entity.getEyeY(), this.lookZ - this.entity.getZ());
		return Optional.ofNullable(this.climber.getOrientation().getLocalRotation(dir).getRight());
	}

	@Override
	protected Optional<Float> getTargetYaw() {
		Vec3d dir = new Vec3d(this.lookX - this.entity.getX(), this.lookY - this.entity.getEyeY(), this.lookZ - this.entity.getZ());
		return Optional.ofNullable(this.climber.getOrientation().getLocalRotation(dir).getLeft());
	}
}
