package com.lily56.spiders2.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.world.World;
import com.lily56.spiders2.common.entity.mob.IMobEntityLivingTickHook;
import com.lily56.spiders2.common.entity.mob.IMobEntityNavigatorHook;
import com.lily56.spiders2.common.entity.mob.IMobEntityRegisterGoalsHook;
import com.lily56.spiders2.common.entity.mob.IMobEntityTickHook;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin implements IMobEntityLivingTickHook, IMobEntityTickHook, IMobEntityRegisterGoalsHook, IMobEntityNavigatorHook {
	@Inject(method = "tickMovement()V", at = @At("HEAD"))
	private void onLivingTick(CallbackInfo ci) {
		this.onLivingTick();
	}

	@Override
	public void onLivingTick() { }

	@Inject(method = "tick()V", at = @At("RETURN"))
	private void onTick(CallbackInfo ci) {
		this.onTick();
	}

	@Override
	public void onTick() { }

	@Shadow(prefix = "shadow$")
	private void shadow$initGoals() { }

	@Redirect(method = "<init>*", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/mob/MobEntity;initGoals()V"
			))
	private void onRegisterGoals(MobEntity _this) {
		this.shadow$initGoals();

		if(_this == (Object) this) {
			this.onRegisterGoals();
		}
	}

	@Override
	public void onRegisterGoals() { }

	@Inject(method = "createNavigation(Lnet/minecraft/world/World;)Lnet/minecraft/entity/ai/pathing/EntityNavigation;", at = @At("HEAD"), cancellable = true)
	private void onCreateNavigator(World world, CallbackInfoReturnable<EntityNavigation> ci) {
		EntityNavigation navigator = this.onCreateNavigator(world);
		if(navigator != null) {
			ci.setReturnValue(navigator);
		}
	}

	@Override
	public EntityNavigation onCreateNavigator(World world) {
		return null;
	}
}
