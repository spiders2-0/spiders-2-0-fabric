package com.lily56.spiders2.mixins;

import java.util.UUID;
import java.util.function.Predicate;

import net.minecraft.entity.mob.HostileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.lily56.spiders2.common.Config;
import com.lily56.spiders2.common.ModTags;
import com.lily56.spiders2.common.entity.goal.BetterLeapAtTargetGoal;
import com.lily56.spiders2.common.entity.mob.IClimberEntity;
import com.lily56.spiders2.common.entity.mob.IMobEntityNavigatorHook;
import com.lily56.spiders2.common.entity.mob.IMobEntityRegisterGoalsHook;
import com.lily56.spiders2.common.entity.movement.BetterSpiderEntityNavigation;

@Mixin(value = SpiderEntity.class, priority = 1001)
public abstract class BetterSpiderEntityMixin extends HostileEntity implements IClimberEntity, IMobEntityRegisterGoalsHook, IMobEntityNavigatorHook {

	private static final UUID FOLLOW_RANGE_INCREASE_ID = UUID.fromString("9e815957-3a8e-4b65-afbc-eba39d2a06b4");
	private static final EntityAttributeModifier FOLLOW_RANGE_INCREASE = new EntityAttributeModifier(FOLLOW_RANGE_INCREASE_ID, "Spiders 2.0 follow range increase", 8.0D, EntityAttributeModifier.Operation.ADDITION);

	private boolean pathFinderDebugPreview;

	private BetterSpiderEntityMixin(EntityType<? extends HostileEntity> type, World worldIn) {
		super(type, worldIn);
	}

	@Inject(method = "<init>*", at = @At("RETURN"))
	private void onConstructed(CallbackInfo ci) {
		this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE).addPersistentModifier(FOLLOW_RANGE_INCREASE);
	}

	@Override
	public EntityNavigation onCreateNavigator(World world) {
		BetterSpiderEntityNavigation<BetterSpiderEntityMixin> navigate = new BetterSpiderEntityNavigation<>(this, world, false);
		navigate.setCanSwim(true);
		return navigate;
	}

	@Inject(method = "registerData()V", at = @At("HEAD"))
	private void onRegisterData(CallbackInfo ci) {
		this.pathFinderDebugPreview = Config.PATH_FINDER_DEBUG_PREVIEW;
	}

	@Redirect(method = "registerGoals()V", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V"
			))
	private void onAddGoal(GoalSelector selector, int priority, Goal task) {
		if(task instanceof PounceAtTargetGoal) {
			selector.add(3, new BetterLeapAtTargetGoal<>(this, 0.4f));
		} else if(task instanceof TrackTargetGoal) {
			selector.add(2, ((TrackTargetGoal) task).setMaxTimeWithoutVisibility(200));
		} else {
			selector.add(priority, task);
		}
	}

	@Override
	public boolean shouldTrackPathingTargets() {
		return this.pathFinderDebugPreview;
	}	

	@Override	
	public boolean canClimbOnBlock(BlockState state, BlockPos pos) {
		return !state.getBlock().isIn(ModTags.NON_CLIMBABLE);
	}

	@Override
	public boolean canAttachToSide(Direction side) {
		if(!this.isJumping && Config.PREVENT_CLIMBING_IN_RAIN && side.getAxis() != Direction.Axis.Y && this.world.isRainingAt(new BlockPos(this.getPosX(), this.getPosY() + this.getHeight() * 0.5f,  this.getPosZ()))) {
			return false;
		}
		return true;
	}

	@Override
	public float getBlockSlipperiness(BlockPos pos) {
		BlockState offsetState = this.world.getBlockState(pos);

		float slipperiness = offsetState.getBlock().getSlipperiness(offsetState, this.world, pos, this) * 0.91f;

		if(offsetState.getBlock().isIn(ModTags.NON_CLIMBABLE)) {
			slipperiness = 1 - (1 - slipperiness) * 0.25f;
		}

		return slipperiness;
	}

	@Override
	public float getPathingMalus(BlockView cache, MobEntity entity, PathNodeType nodeType, BlockPos pos, Vector3i direction, Predicate<Direction> sides) {
		if(direction.getY() != 0) {
			if(Config.PREVENT_CLIMBING_IN_RAIN && !sides.test(Direction.UP) && !sides.test(Direction.DOWN) && this.world.isRainingAt(pos)) {
				return -1.0f;
			}

			boolean hasClimbableNeigbor = false;

			BlockPos.Mutable offsetPos = new BlockPos.Mutable();

			for(Direction offset : Direction.values()) {
				if(sides.test(offset)) {
					offsetPos.setPos(pos.getX() + offset.getXOffset(), pos.getY() + offset.getYOffset(), pos.getZ() + offset.getZOffset());

					BlockState state = cache.getBlockState(offsetPos);

					if(this.canClimbOnBlock(state, offsetPos)) {
						hasClimbableNeigbor = true;
					}
				}
			}

			if(!hasClimbableNeigbor) {
				return -1.0f;
			}
		}

		return entity.getPathPriority(nodeType);
	}
}
