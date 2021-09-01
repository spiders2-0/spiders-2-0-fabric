package com.lily56.spiders2.common.entity.mob;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.MovementType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface IEntityMovementHook {
	public boolean onMove(MovementType type, Vec3d pos, boolean pre);

	@Nullable
	public BlockPos getAdjustedOnPosition(BlockPos onPosition);

	public boolean getAdjustedCanTriggerWalking(boolean canTriggerWalking);
}
