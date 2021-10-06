package com.lily56.spiders2.common;

//import net.minecraftforge.common.MinecraftForge;

public class CommonSetup {
	public static void run() {
		MinecraftForge.EVENT_BUS.register(CommonEventHandlers.class);
	}
}
