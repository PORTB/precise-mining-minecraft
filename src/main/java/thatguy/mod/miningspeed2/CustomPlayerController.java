package thatguy.mod.miningspeed2;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;
import thatguy.mod.miningspeed2.proxy.ClientProxy;

public class CustomPlayerController
{
    private final static Minecraft minecraft = Minecraft.getMinecraft();
    public static boolean hasBrokenBlock = false;

    private boolean isMiningControlEnabled()
    {
        if (!ClientProxy.isItemMiningTool(minecraft.player.getHeldItemMainhand()))
            return false;

        ItemStack heldItem = minecraft.player.getHeldItemMainhand();

        if (heldItem.getTagCompound() != null)
        {
            NBTTagCompound tag = heldItem.getTagCompound();

            if (tag.hasKey(Reference.MINING_CONTROL_ENABLED_TAG))
            {
                return tag.getBoolean(Reference.MINING_CONTROL_ENABLED_TAG);
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    public boolean onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing)
    {
        final PlayerControllerMP playerController = minecraft.playerController;

        playerController.syncCurrentPlayItem();

        if (hasBrokenBlock && isMiningControlEnabled())
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
            IBlockState blockState = playerController.mc.world.getBlockState(posBlock);
            Block block = blockState.getBlock();

            if (blockState.getMaterial() == Material.AIR)
            {
                playerController.isHittingBlock = false;
                return false;
            }
            else
            {
                playerController.curBlockDamageMP += blockState.getPlayerRelativeBlockHardness(playerController.mc.player, playerController.mc.player.world, posBlock);

                if (playerController.stepSoundTickCounter % 4.0F == 0.0F)
                {
                    SoundType soundtype = block.getSoundType(blockState, minecraft.world, posBlock, minecraft.player);
                    playerController.mc.getSoundHandler().playSound(new PositionedSoundRecord(soundtype.getHitSound(), SoundCategory.NEUTRAL, (soundtype.getVolume() + 1.0F) / 8.0F, soundtype.getPitch() * 0.5F, posBlock));
                }

                ++playerController.stepSoundTickCounter;
                playerController.mc.getTutorial().onHitBlock(playerController.mc.world, posBlock, blockState, MathHelper.clamp(playerController.curBlockDamageMP, 0.0F, 1.0F));

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

                playerController.mc.world.sendBlockBreakProgress(playerController.mc.player.getEntityId(), playerController.currentBlock, (int) (playerController.curBlockDamageMP * 10.0F) - 1);
                return true;
            }
        }
        else
        {
            return clickBlock(posBlock, directionFacing);
        }
    }

    public boolean clickBlock(BlockPos loc, EnumFacing face)
    {
        final PlayerControllerMP playerController = minecraft.playerController;

        if (playerController.currentGameType.hasLimitedInteractions())
        {
            if (playerController.currentGameType == GameType.SPECTATOR)
            {
                return false;
            }

            if (!playerController.mc.player.isAllowEdit())
            {
                ItemStack itemstack = playerController.mc.player.getHeldItemMainhand();

                if (itemstack.isEmpty())
                {
                    return false;
                }

                if (!itemstack.canDestroy(playerController.mc.world.getBlockState(loc).getBlock()))
                {
                    return false;
                }
            }
        }

        if (!playerController.mc.world.getWorldBorder().contains(loc))
        {
            return false;
        }
        else
        {
            if (playerController.currentGameType.isCreative())
            {
                playerController.mc.getTutorial().onHitBlock(playerController.mc.world, loc, playerController.mc.world.getBlockState(loc), 1.0F);
                playerController.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
                if (!net.minecraftforge.common.ForgeHooks.onLeftClickBlock(playerController.mc.player, loc, face, net.minecraftforge.common.ForgeHooks.rayTraceEyeHitVec(playerController.mc.player, playerController.getBlockReachDistance() + 1)).isCanceled())
                    PlayerControllerMP.clickBlockCreative(playerController.mc, playerController, loc, face);
                playerController.blockHitDelay = 5;
            }
            else if (!playerController.isHittingBlock || !playerController.isHittingPosition(loc))
            {
                if (playerController.isHittingBlock)
                {
                    playerController.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, playerController.currentBlock, face));
                }

                if (hasBrokenBlock && isMiningControlEnabled())
                    return false;

                net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event = net.minecraftforge.common.ForgeHooks.onLeftClickBlock(playerController.mc.player, loc, face, net.minecraftforge.common.ForgeHooks.rayTraceEyeHitVec(playerController.mc.player, playerController.getBlockReachDistance() + 1));

                IBlockState iblockstate = playerController.mc.world.getBlockState(loc);
                playerController.mc.getTutorial().onHitBlock(playerController.mc.world, loc, iblockstate, 0.0F);
                playerController.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
                boolean flag = iblockstate.getMaterial() != Material.AIR;

                if (flag && playerController.curBlockDamageMP == 0.0F)
                {
                    if (event.getUseBlock() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY)
                        iblockstate.getBlock().onBlockClicked(playerController.mc.world, loc, playerController.mc.player);
                }

                if (event.getUseItem() == net.minecraftforge.fml.common.eventhandler.Event.Result.DENY) return true;
                if (flag && iblockstate.getPlayerRelativeBlockHardness(playerController.mc.player, playerController.mc.player.world, loc) >= 1.0F)
                {
                    hasBrokenBlock = true;
                    playerController.onPlayerDestroyBlock(loc);
                }
                else
                {
                    playerController.isHittingBlock = true;
                    playerController.currentBlock = loc;
                    playerController.currentItemHittingBlock = playerController.mc.player.getHeldItemMainhand();
                    playerController.curBlockDamageMP = 0.0F;
                    playerController.stepSoundTickCounter = 0.0F;
                    playerController.mc.world.sendBlockBreakProgress(playerController.mc.player.getEntityId(), playerController.currentBlock, (int) (playerController.curBlockDamageMP * 10.0F) - 1);
                }
            }

            return true;
        }
    }
}
