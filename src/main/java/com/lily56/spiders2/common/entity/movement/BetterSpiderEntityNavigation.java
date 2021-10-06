package com.lily56.spiders2.common.entity.movement;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.lily56.spiders2.common.entity.mob.IClimberEntity;

public class BetterSpiderEntityNavigation<T extends MobEntity & IClimberEntity> extends AdvancedClimberPathNavigator<T> {
	private boolean useVanillaBehaviour;
	private BlockPos targetPosition;

	public BetterSpiderEntityNavigation(T entity, World worldIn, boolean useVanillaBehaviour) {
		super(entity, worldIn, false, true, true);
		this.useVanillaBehaviour = useVanillaBehaviour;
	}

	@Override
	public Path getPathToPos(BlockPos pos, int pos2) {
		this.targetPosition = pos;
		return super.getPathToPos(pos, pos2);
	}

	@Override
	public Path getPathToEntity(Entity entityIn, int path2) {
		this.targetPosition = entityIn.getBlockPos();
		return super.getPathToEntity(entityIn, path2);
	}

	@Override
	public boolean tryMoveToEntityLiving(Entity entityIn, double speed) {
		Path path = this.getPathToEntity(entityIn, 0);
		if(path != null) {
			return this.startMovingAlong(path, speed);
		} else {
			this.targetPosition = entityIn.getBlockPos();
			this.speed = speed;
			return true;
		}
	}

	@Override
	public void tick() {
		if(!this.isIdle()) {
			super.tick();
		} else {
			if(this.targetPosition != null && this.useVanillaBehaviour) {
				// FORGE: Fix MC-94054
				if(!this.targetPosition.isWithinDistance(this.entity.getPos(), Math.max((double) this.entity.getWidth(), 1.0D)) && (!(this.entity.getY() > (double) this.targetPosition.getY()) || !(new BlockPos((double) this.targetPosition.getX(), this.entity.getY(), (double) this.targetPosition.getZ())).isWithinDistance(this.entity.getPos(), Math.max((double) this.entity.getWidth(), 1.0D)))) {
					this.entity.getMoveControl().moveTo((double) this.targetPosition.getX(), (double) this.targetPosition.getY(), (double) this.targetPosition.getZ(), this.speed);
				} else {
					this.targetPosition = null;
				}
			}

		}
	}
}
