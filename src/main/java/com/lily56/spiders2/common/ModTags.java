package com.lily56.spiders2.common;


import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class ModTags {
	public static final Tag<Block> NON_CLIMBABLE = TagRegistry.block(new Identifier("spiders2", "non_climbable"));
}
