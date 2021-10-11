package com.lily56.spiders2.common;

import org.jetbrains.annotations.Nullable;
//import net.minecraftforge.common.ForgeConfigSpec;
//TODO: Replace with ClothConfig
public class Config {
	//public static final ForgeConfigSpec COMMON;

	//public static final ForgeConfigSpec.BooleanValue PREVENT_CLIMBING_IN_RAIN;

	//public static final ForgeConfigSpec.BooleanValue PATH_FINDER_DEBUG_PREVIEW;

	public static final String COMMON = "COMMON";

	public static final boolean PREVENT_CLIMBING_IN_RAIN = true;

	public static final boolean PATH_FINDER_DEBUG_PREVIEW = false;

	/*
	static {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

		PREVENT_CLIMBING_IN_RAIN = builder.comment("Whether spiders should be unable to climb when exposed to rain")
				.define("prevent_climbing_in_rain", true);

		PATH_FINDER_DEBUG_PREVIEW = builder
				.worldRestart()
				.comment("Whether the path finder debug preview should be enabled.")
				.define("path_finder_debug_preview", false);

		COMMON = builder.build();
	}
	*/
}
