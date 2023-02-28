package intezium.ender_dragon_egg_respawn.mixin;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonFight.class)
public abstract class EnderDragonFightMixin {
	@Shadow
	private boolean previouslyKilled;

	@Inject(
			method = "dragonKilled(Lnet/minecraft/entity/boss/dragon/EnderDragonEntity;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/boss/dragon/EnderDragonFight;generateNewEndGateway()V"
			)
	)
	private void dragonKilled(EnderDragonEntity dragon, CallbackInfo info) {
		this.previouslyKilled = false;
	}
}