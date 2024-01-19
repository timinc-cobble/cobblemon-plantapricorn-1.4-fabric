package us.timinc.mc.cobblemon.plantapricorns

import com.cobblemon.mod.common.api.tags.CobblemonBlockTags
import com.cobblemon.mod.common.api.tags.CobblemonItemTags
import com.cobblemon.mod.common.item.ApricornItem
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.tag.BlockTags
import net.minecraft.resource.LifecycledResourceManager
import net.minecraft.server.MinecraftServer
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.world.World
import us.timinc.mc.cobblemon.plantapricorns.config.Config

object PlantApricorns : ModInitializer {
    const val MOD_ID = "cobblemon-plantapricorns"
    var config: Config = Config.Builder.load()

    override fun onInitialize() {
        UseBlockCallback.EVENT.register { pE, w, h, bhr -> handleBlockClick(pE, w, h, bhr) }
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register { s: MinecraftServer, lrm: LifecycledResourceManager, successful: Boolean ->
            handleReload(
                s,
                lrm,
                successful
            )
        }
    }

    fun handleReload(s: MinecraftServer, lrm: LifecycledResourceManager, successful: Boolean) {
        config = Config.Builder.load()
        println("Reloaded!")
    }

    fun handleBlockClick(playerEntity: PlayerEntity, world: World, hand: Hand, bhr: BlockHitResult): ActionResult {
        if (world.isClient || hand == Hand.OFF_HAND) {
            return ActionResult.PASS
        }
        val stack = playerEntity.getStackInHand(hand)
        if (stack.isIn(CobblemonItemTags.APRICORNS)
            && world.getBlockState(bhr.blockPos)
                .isIn(if (config.allLeaves) BlockTags.LEAVES else CobblemonBlockTags.APRICORN_LEAVES)
        ) {
            if (stack.item !is ApricornItem) {
                throw Exception("You've tried to plant an Apricorn that is not an Apricorn.")
            }
            val pos = bhr.blockPos.add(bhr.side.vector)
            if (!world.getBlockState(pos).isAir) {
                return ActionResult.PASS
            }
            val bs =
                (stack.item as ApricornItem).block.defaultState.with(HorizontalFacingBlock.FACING, bhr.side.opposite)
            world.setBlockState(pos, bs)
            stack.count--
            return ActionResult.SUCCESS
        }
        return ActionResult.PASS
    }
}