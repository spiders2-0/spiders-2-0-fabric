package com.lily56.spiders2.common;

import net.minecraft.entity.Entity;
//import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityDimensions;
//import net.minecraft.entity.monster.CaveSpiderEntity;
import net.minecraft.entity.mob.CaveSpiderEntity;
//import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.mob.SpiderEntity;
//import net.minecraftforge.event.entity.EntityEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommonEventHandlers {
	@SubscribeEvent
	public static void onEntitySize(EntityEvent.Size event) {
		Entity entity = event.getEntity();

		if(entity instanceof CaveSpiderEntity) {
			event.setNewSize(EntityDimensions.changing(0.7f, 0.5f));
		} else if(entity instanceof SpiderEntity) {
			event.setNewSize(EntityDimensions.changing(0.95f, 0.85f));
		}
	}
}
