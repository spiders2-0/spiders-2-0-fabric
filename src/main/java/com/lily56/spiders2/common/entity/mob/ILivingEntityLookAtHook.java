package com.lily56.spiders2.common.entity.mob;

import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.util.math.Vec3d;

public interface ILivingEntityLookAtHook {
	public Vec3d onLookAt(EntityAnchorArgumentType anchor, Vec3d vec);
}
