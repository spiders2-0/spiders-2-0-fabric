package com.lily56.spiders2.common.entity.mob;

import net.minecraft.nbt.NbtCompound;

public interface IEntityReadWriteHook {
	public void onRead(NbtCompound nbt);
	
	public void onWrite(NbtCompound nbt);
}
