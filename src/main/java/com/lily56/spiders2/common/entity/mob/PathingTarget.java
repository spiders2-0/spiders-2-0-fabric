package com.lily56.spiders2.common.entity.mob;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;

public class PathingTarget {
	public final BlockPos pos;
	public final Direction side;
	
	public PathingTarget(BlockPos pos, Direction side) {
		this.pos = pos;
		this.side = side;
	}
}