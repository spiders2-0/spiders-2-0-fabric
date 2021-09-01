package com.lily56.spiders2.common.entity.mob;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.world.World;


public interface IMobEntityNavigatorHook {
	@Nullable
	public EntityNavigation onCreateNavigator(World world);
}
