package com.lily56.spiders2.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.util.math.Vec3d;
import com.lily56.spiders2.common.entity.mob.ILivingEntityDataManagerHook;
import com.lily56.spiders2.common.entity.mob.ILivingEntityJumpHook;
import com.lily56.spiders2.common.entity.mob.ILivingEntityLookAtHook;
import com.lily56.spiders2.common.entity.mob.ILivingEntityTravelHook;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements ILivingEntityLookAtHook, ILivingEntityDataManagerHook, ILivingEntityTravelHook, ILivingEntityJumpHook {
	@ModifyVariable(method = "lookAt(Lnet/minecraft/command/argument/EntityAnchorArgumentType;Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"), ordinal = 0)
	private Vec3d onLookAtModify(Vec3d vec, EntityAnchorArgumentType anchor, Vec3d vec2) {
		return this.onLookAt(anchor, vec);
	}

	@Override
	public Vec3d onLookAt(EntityAnchorArgumentType anchor, Vec3d vec) {
		return vec;
	}

	@Inject(method = "notifyDataManagerChange(Lnet/minecraft/entity/data/TrackedData;)V", at = @At("HEAD"))
	private void onNotifyDataManagerChange(TrackedData<?> key, CallbackInfo ci) {
		this.onNotifyDataManagerChange(key);
	}

	@Override
	public void onNotifyDataManagerChange(TrackedData<?> key) { }

	@Inject(method = "travel(Lnet/minecraft/util/math/vector/Vec3d;)V", at = @At("HEAD"), cancellable = true)
	private void onTravelPre(Vec3d relative, CallbackInfo ci) {
		if(this.onTravel(relative, true)) {
			ci.cancel();
		}
	}

	@Inject(method = "travel(Lnet/minecraft/util/math/vector/Vec3d;)V", at = @At("RETURN"))
	private void onTravelPost(Vec3d relative, CallbackInfo ci) {
		this.onTravel(relative, false);
	}

	@Override
	public boolean onTravel(Vec3d relative, boolean pre) {
		return false;
	}

	@Inject(method = "jump()V", at = @At("HEAD"), cancellable = true)
	private void onJump(CallbackInfo ci) {
		if(this.onJump()) {
			ci.cancel();
		}
	}

	@Override
	public boolean onJump() {
		return false;
	}
}
