package ru.intezium.ender_dragon_egg_respawn.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = EnderDragonFight.class, priority = Integer.MIN_VALUE)
public abstract class EnderDragonFightMixin {
	@Shadow
	private final ServerWorld world;
	@Shadow
	private BlockPos exitPortalLocation;
	@Shadow
	private boolean previouslyKilled;

	protected EnderDragonFightMixin(ServerWorld world) {
		this.world = world;
	}

	@Inject(method = "dragonKilled(Lnet/minecraft/entity/boss/dragon/EnderDragonEntity;)V", at = @At("HEAD"))
	private void dragonKilled(EnderDragonEntity dragon, CallbackInfo info) {
		if (this.previouslyKilled) {
			BlockPos eggSpawnPos = searchEggSpawnPos(getPosAbovePortal(this.exitPortalLocation));
			this.world.setBlockState(eggSpawnPos, Blocks.DRAGON_EGG.getDefaultState());
		}
	}

	private BlockPos getPosAbovePortal(BlockPos portalPos) {
		BlockPos abovePortalPos = new BlockPos(portalPos);

		while (this.world.getBlockState(abovePortalPos).isOf(Blocks.BEDROCK)) {
			abovePortalPos = abovePortalPos.up();
		}
		return abovePortalPos;
	}

	private BlockPos searchEggSpawnPos(BlockPos abovePortalPos) {
		BlockPos eggSpawnPos = new BlockPos(abovePortalPos);
		BlockState blockState;

		while (!isUpperWorldLimit(eggSpawnPos)) {
			blockState = getBlockState(eggSpawnPos);

			if (blockState.isAir())
				return eggSpawnPos;
			else if (!blockState.isOf(Blocks.DRAGON_EGG))
				return searchEggSpawnPosExtended(abovePortalPos);

			eggSpawnPos = eggSpawnPos.up();
		}
		return searchEggSpawnPosExtended(abovePortalPos);
	}

	private BlockPos searchEggSpawnPosExtended(BlockPos abovePortalPos) {
		List<BlockPos> blockPosList = new ArrayList<>();

		int centerX = abovePortalPos.getX();
		int centerZ = abovePortalPos.getZ();
		int minSearchRadius = 4;
		int maxSearchRadius = 12;
		int searchHeight = 3;
		int x, z, delta, centerY;

		for (int radius = minSearchRadius; radius <= maxSearchRadius; radius++) {
			for (int height = 0; height < searchHeight; height++) {
				x = 0;
				z = radius;
				delta = 3 - (2 * radius);
				centerY = abovePortalPos.getY() + height;

				while (x <= z) {
					blockPosList.add(new BlockPos(centerX + x, centerY, centerZ + z));
					blockPosList.add(new BlockPos(centerX + x, centerY, centerZ - z));
					blockPosList.add(new BlockPos(centerX - x, centerY, centerZ + z));
					blockPosList.add(new BlockPos(centerX - x, centerY, centerZ - z));
					blockPosList.add(new BlockPos(centerX + z, centerY, centerZ + x));
					blockPosList.add(new BlockPos(centerX + z, centerY, centerZ - x));
					blockPosList.add(new BlockPos(centerX - z, centerY, centerZ + x));
					blockPosList.add(new BlockPos(centerX - z, centerY, centerZ - x));

					BlockPos airBlockPos = searchLowerAirBlockPos(blockPosList);

					if (airBlockPos != null)
						return airBlockPos;
					blockPosList.clear();

					if (delta < 0)
						delta += 4 * x + 6;
					else
						delta += 4 * (x - z--) + 10;
					x++;
				}
			}
		}
		return abovePortalPos;
	}

	private BlockPos searchLowerAirBlockPos(List<BlockPos> blockPosList) {
		for (BlockPos blockPos : blockPosList)
			if (getBlockState(blockPos).isAir()) {
				BlockPos lowerAirBlockPos = getLowerAirBlockPos(blockPos);
				BlockPos bottomBlockPos = lowerAirBlockPos.down();
				BlockState bottomBlockState = getBlockState(bottomBlockPos);

				if (bottomBlockState.isSolidBlock(this.world, bottomBlockPos) && !bottomBlockState.isOf(Blocks.BEDROCK))
					return lowerAirBlockPos;
			}
		return null;
	}

	private BlockPos getLowerAirBlockPos(BlockPos originBlockPos) {
		if(!getBlockState(originBlockPos.down()).isAir())
			return originBlockPos;

		BlockPos bottomBlockPos = new BlockPos(originBlockPos);

		while (bottomBlockPos.getY() > this.world.getBottomY() && getBlockState(bottomBlockPos.down()).isAir()) {
			bottomBlockPos = bottomBlockPos.down();
		}
		return bottomBlockPos;
	}

	private boolean isUpperWorldLimit(BlockPos blockPos) {
		return blockPos.getY() >= this.world.getHeight();
	}

	private BlockState getBlockState(BlockPos blockPos) {
		return this.world.getBlockState(blockPos);
	}
}