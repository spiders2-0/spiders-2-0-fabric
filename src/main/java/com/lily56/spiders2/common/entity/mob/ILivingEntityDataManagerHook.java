package com.lily56.spiders2.common.entity.mob;

import net.minecraft.entity.data.TrackedData;

public interface ILivingEntityDataManagerHook {
	public void onNotifyDataManagerChange(TrackedData<?> key);
}
