package com.lily56.spiders2.common.entity.movement;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.ai.control.JumpControl;
import net.minecraft.util.math.Vec3d;
import com.lily56.spiders2.common.entity.mob.IClimberEntity;

public class ClimberJumpController<T extends MobEntity & IClimberEntity> extends JumpControl {
	protected final T climber;

	@Nullable
	protected Vec3d dir;

	public ClimberJumpController(T mob) {
		super(mob);
		this.climber = mob;
	}

	@Override
	public void setJumping() {
		this.setJumping(null);
	}

	public void setJumping(Vec3d dir) {
		super.setActive();
		this.dir = dir;
	}

	@Override
	public void tick() {
		this.climber.setJumping(this.active);
		if(this.active) {
			this.climber.setJumpDirection(this.dir);
		} else if(this.dir == null) {
			this.climber.setJumpDirection(null);
		}
		this.active = false;
	}
}
