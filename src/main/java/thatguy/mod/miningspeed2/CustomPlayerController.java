package thatguy.mod.miningspeed2;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import static thatguy.mod.miningspeed2.MiningSpeed.minecraft;

public class CustomPlayerController
{
    private final static PlayerControllerMP playerController = minecraft.playerController;

    public boolean hasBrokenBlock = false;

    public boolean onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing)
    {
        final PlayerControllerMP playerController = minecraft.playerController;

        playerController.syncCurrentPlayItem();

        if(hasBrokenBlock)
            return false;

        if (playerController.blockHitDelay > 0)
        {
            --playerController.blockHitDelay;
            return true;
        }
        else if (playerController.currentGameType.isCreative() && playerController.mc.world.getWorldBorder().contains(posBlock))
        {
            playerController.blockHitDelay = 5;
            playerController.mc.getTutorial().onHitBlock(playerController.mc.world, posBlock, playerController.mc.world.getBlockState(posBlock), 1.0F);
            playerController.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, posBlock, directionFacing));
            PlayerControllerMP.clickBlockCreative(playerController.mc, playerController, posBlock, directionFacing);
            return true;
        }
        else if (playerController.isHittingPosition(posBlock))
        {
            IBlockState iblockstate = playerController.mc.world.getBlockState(posBlock);
            Block block = iblockstate.getBlock();

            if (iblockstate.getMaterial() == Material.AIR)
            {
                playerController.isHittingBlock = false;
                return false;
            }
            else
            {
                playerController.curBlockDamageMP += iblockstate.getPlayerRelativeBlockHardness(playerController.mc.player, playerController.mc.player.world, posBlock);

                if (playerController.stepSoundTickCounter % 4.0F == 0.0F)
                {
                    SoundType soundtype = block.getSoundType(iblockstate, minecraft.world, posBlock, minecraft.player);
                    playerController.mc.getSoundHandler().playSound(new PositionedSoundRecord(soundtype.getHitSound(), SoundCategory.NEUTRAL, (soundtype.getVolume() + 1.0F) / 8.0F, soundtype.getPitch() * 0.5F, posBlock));
                }

                ++playerController.stepSoundTickCounter;
                playerController.mc.getTutorial().onHitBlock(playerController.mc.world, posBlock, iblockstate, MathHelper.clamp(playerController.curBlockDamageMP, 0.0F, 1.0F));

                if (playerController.curBlockDamageMP >= 1.0F)
                {
                    hasBrokenBlock = true;
                    playerController.isHittingBlock = false;
                    playerController.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, posBlock, directionFacing));
                    playerController.onPlayerDestroyBlock(posBlock);
                    playerController.curBlockDamageMP = 0.0F;
                    playerController.stepSoundTickCounter = 0.0F;
                    playerController.blockHitDelay = 5;
                }

                playerController.mc.world.sendBlockBreakProgress(playerController.mc.player.getEntityId(), playerController.currentBlock, (int)(playerController.curBlockDamageMP * 10.0F) - 1);
                return true;
            }
        }
        else
        {
            return playerController.clickBlock(posBlock, directionFacing);
        }
    }
}
