package thatguy.mod.miningspeed2;

import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.potion.Effect;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.*;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.network.FMLMCRegisterPacketHandler;
import net.minecraftforge.fml.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import javax.tools.Tool;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MiningSpeed.MODID)
public class MiningSpeed
{
    public final static String MODID = "miningspeed2";
    public final static String MINING_SPEED_CONTROL_ENABLED_TAG = "mining_speed_enabled";
    // Directly reference a log4j logger.
    private static final Logger log = LogManager.getLogger();
    private static final Minecraft minecraft = Minecraft.getInstance();
    private static final PlayerController controller = minecraft.playerController;

    public static KeyBinding toggleSpeedControl = new KeyBinding("Toggle Mining Speed Control", GLFW.GLFW_KEY_BACKSLASH, "Mining Control");
    private final Object blockBrokenLock = new Object();
    private Boolean hasPlayerBrokenABlock = false;

    public MiningSpeed()
    {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        ClientRegistry.registerKeyBinding(toggleSpeedControl);
        Networking.register();
    }

    //@SubscribeEvent
//    void lagServer(TickEvent.ServerTickEvent event)
//    {
//        String s = "d";
//        for (int i = 0; i < 4500; i++)
//        {
//            s += "swedwedewdew" + i + "nededed";
//        }
//        //System.out.println("EEEEEEEEEEEEEEE");
//    }

    static private boolean isItemMiningTool(ItemStack stack)
    {
        if (stack.getItem() instanceof ShearsItem)
        {
            return true;
        }

        ToolType[] toolTypes = new ToolType[]{ToolType.AXE, ToolType.HOE, ToolType.PICKAXE, ToolType.SHOVEL};
        boolean containsToolType = false;

        for (ToolType toolType : toolTypes)
        {
            if (stack.getToolTypes().contains(toolType))
            {
                containsToolType = true;
                break;
            }
        }

        return containsToolType;
    }

    @SubscribeEvent
    public void onRenderTooltip(ItemTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();
        CompoundNBT nbt = stack.getOrCreateTag();

        if (isItemMiningTool(stack))
        {
            boolean isEnabled = nbt.getBoolean(MINING_SPEED_CONTROL_ENABLED_TAG);
            TextComponent enabled = new StringTextComponent("Enabled");
            TextComponent disabled = new StringTextComponent("Disabled");

            enabled.mergeStyle(TextFormatting.GREEN);
            disabled.mergeStyle(TextFormatting.RED);

            List<ITextComponent> tooltip = event.getToolTip();
            tooltip.add(new StringTextComponent("Mining control is ").append(isEnabled ? enabled : disabled));
        }
    }

    @SubscribeEvent
    void clientTickEvent(TickEvent.ClientTickEvent event)
    {
        processMouseInput();
        handleKeybindings();
    }

    private void processMouseInput()
    {
        if (!minecraft.gameSettings.keyBindAttack.isKeyDown())
        {
            synchronized (blockBrokenLock)
            {
                hasPlayerBrokenABlock = false;
            }
        }
    }

    private void handleKeybindings()
    {
        if (toggleSpeedControl.isPressed())
        {
            ClientPlayerEntity player = minecraft.player;

            if (player != null)
            {
                if (!player.getHeldItemMainhand().isEmpty())
                {
                    ItemStack stack = player.getHeldItemMainhand();

                    //don't set nbt for non tool items
                    if (!isItemMiningTool(stack))
                        return;

                    Networking.sendToServer(new Networking.ToggleMiningControlMessage());
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void handleInputEvent(InputEvent.ClickInputEvent event)
    {
        //region null checks
        if (minecraft.player == null)
            return;

        if (minecraft.player.getHeldItemMainhand().isEmpty())
            return;

        if (!minecraft.player.getHeldItemMainhand().getOrCreateTag().getBoolean(MINING_SPEED_CONTROL_ENABLED_TAG))
            return;

        if (event.isCanceled())
            return;

        if (!event.isAttack())
            return;
        //endregion

        ClientWorld world = minecraft.world;
        RayTraceResult objectMouseOver = minecraft.objectMouseOver;

        if (world == null)
            return;

        if (minecraft.playerController.getCurrentGameType().isCreative())
            return;

        if (!minecraft.playerController.isHittingBlock)
        {
            if (objectMouseOver != null)
            {
                if (objectMouseOver.getType() == RayTraceResult.Type.BLOCK)
                {
                    BlockRayTraceResult result = (BlockRayTraceResult) objectMouseOver;
                    BlockPos blockPos = result.getPos();

                    if (!world.isAirBlock(blockPos))
                    {
                        if (!clickBlock(blockPos, result.getFace()))
                        {
                            event.setSwingHand(false);
                        }
                    }
                }
            }
        }
        else
        {
            handlePlayerBreakingBlock(event);
        }

        event.setCanceled(true);
    }

    /**
     * Replaces PlayerController.clickBlock
     *
     * @param blockPos
     * @param direction
     * @return Return value seems to have no effect. True if the action succeeded, false if it did not, e.g. due to restrictions.
     */
    public boolean clickBlock(BlockPos blockPos, Direction direction)
    {
        ClientPlayerEntity player = minecraft.player;
        ClientWorld world = minecraft.world;
        PlayerController controller = minecraft.playerController;
        GameType currentGameType = controller.getCurrentGameType();

        //region null checks
        if (player == null)
            return true;

        if (world == null)
            return true;
        //endregion

        if (player.blockActionRestricted(world, blockPos, minecraft.playerController.getCurrentGameType()))
        {
            return false;
        }
        else if (!world.getWorldBorder().contains(blockPos))
        {
            return false;
        }
        else
        {
            if (currentGameType.isCreative())
            {
                BlockState blockState = world.getBlockState(blockPos);
                minecraft.getTutorial().onHitBlock(world, blockPos, blockState, 1.0f);
                controller.sendDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, blockPos, direction);

                if (!ForgeHooks.onLeftClickBlock(player, blockPos, direction).isCanceled())
                {
                    controller.onPlayerDestroyBlock(blockPos);
                }
            }
            else if (!controller.isHittingBlock || !controller.isHittingPosition(blockPos))
            {
                if (controller.isHittingBlock)
                {
                    controller.sendDiggingPacket(CPlayerDiggingPacket.Action.ABORT_DESTROY_BLOCK, controller.currentBlock, direction);
                }

                if (hasPlayerBrokenABlock)
                    return false;

                PlayerInteractEvent.LeftClickBlock event = ForgeHooks.onLeftClickBlock(player, blockPos, direction);

                BlockState blockState = world.getBlockState(blockPos);
                minecraft.getTutorial().onHitBlock(world, blockPos, blockState, 0.0f);
                controller.sendDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, blockPos, direction);

                boolean isBlockAir = blockState.isAir(world, blockPos);

                if (!isBlockAir && controller.curBlockDamageMP == 0.0f)
                {
                    if (event.getUseBlock() != Event.Result.DENY)
                    {
                        blockState.onBlockClicked(world, blockPos, player);
                    }
                }

                if (event.getUseItem() == Event.Result.DENY)
                    return true;

                if (!isBlockAir && blockState.getPlayerRelativeBlockHardness(player, world, blockPos) >= 1.0f)
                {
                    hasPlayerBrokenABlock = true;
                    controller.onPlayerDestroyBlock(blockPos);
                }
                else
                {
                    controller.isHittingBlock = true;
                    controller.currentBlock = blockPos;
                    controller.currentItemHittingBlock = player.getHeldItemMainhand();
                    controller.curBlockDamageMP = 0.0f;
                    world.sendBlockBreakProgress(player.getEntityId(), controller.currentBlock, (int) (controller.curBlockDamageMP * 10.0f) - 1);
                }
            }

            return true;
        }
    }

    /**
     * Replaces part of sendBlockClickToController
     *
     * @param event
     */
    private void handlePlayerBreakingBlock(InputEvent.ClickInputEvent event)
    {
        BlockRayTraceResult objectMouseOver = (BlockRayTraceResult) minecraft.objectMouseOver;
        BlockPos blockPos = objectMouseOver.getPos();
        Direction direction = objectMouseOver.getFace();

        if (event.isCanceled())
        {
            if (event.shouldSwingHand())
            {
                minecraft.particles.addBlockHitEffects(blockPos, objectMouseOver);
                minecraft.player.swingArm(Hand.MAIN_HAND);
            }

            return;
        }

        if (onPlayerDamageBlock(blockPos, direction))
        {
            if (event.shouldSwingHand())
            {
                minecraft.particles.addBlockHitEffects(blockPos, objectMouseOver);
                minecraft.player.swingArm(Hand.MAIN_HAND);
            }
        }
    }

    /**
     * replaces PlayerController.onPlayerDamageBlock
     *
     * @param blockPos
     * @param direction
     * @return
     */
    public boolean onPlayerDamageBlock(BlockPos blockPos, Direction direction)
    {
        //For some reason the static field results to null here, this fixes it
        PlayerController controller = minecraft.playerController;

        if (controller == null)
            return true;

        controller.syncCurrentPlayItem();

        if (hasPlayerBrokenABlock)
        {
            return false;
        }

        if (controller.blockHitDelay > 0)
        {
            --controller.blockHitDelay;
            return true;
        }
        else if (controller.getCurrentGameType().isCreative() && minecraft.world.getWorldBorder().contains(blockPos))
        {
            controller.blockHitDelay = 5;
            BlockState blockState = minecraft.world.getBlockState(blockPos);

            minecraft.getTutorial().onHitBlock(minecraft.world, blockPos, blockState, 1.0f);
            controller.sendDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, blockPos, direction);

            if (!net.minecraftforge.common.ForgeHooks.onLeftClickBlock(minecraft.player, blockPos, direction).isCanceled())
            {
                controller.onPlayerDestroyBlock(blockPos);
            }

            return true;
        }
        else if (controller.isHittingPosition(blockPos))
        {
            BlockState blockState = minecraft.world.getBlockState(blockPos);

            if (blockState.isAir(minecraft.world, blockPos))
            {
                controller.isHittingBlock = false;
                return false;
            }
            else
            {
                controller.curBlockDamageMP += blockState.getPlayerRelativeBlockHardness(minecraft.player, minecraft.world, blockPos);

                if (controller.stepSoundTickCounter % 4.0f == 0.0f)
                {
                    SoundType soundType = blockState.getSoundType(minecraft.world, blockPos, minecraft.player);
                    minecraft.getSoundHandler().play(new SimpleSound(
                            soundType.getHitSound(),
                            SoundCategory.BLOCKS,
                            (soundType.getVolume() + 1.0F) / 8.0F,
                            soundType.getPitch() * 0.5F, blockPos));
                }

                ++controller.stepSoundTickCounter;
                minecraft.getTutorial().onHitBlock(minecraft.world, blockPos, blockState, 1.0f);

                if (ForgeHooks.onLeftClickBlock(minecraft.player, blockPos, direction).getUseItem() == Event.Result.DENY)
                {
                    return true;
                }

                if (controller.curBlockDamageMP >= 1.0f)
                {
                    hasPlayerBrokenABlock = true;
                    controller.isHittingBlock = false;
                    controller.sendDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction);
                    controller.onPlayerDestroyBlock(blockPos);
                    controller.curBlockDamageMP = 0.0f;
                    controller.stepSoundTickCounter = 0.0f;
                    controller.blockHitDelay = 5;
                }

                minecraft.world.sendBlockBreakProgress(minecraft.player.getEntityId(), controller.currentBlock, (int) (controller.curBlockDamageMP * 10.0F) - 1);
                return true;
            }
        }
        else
        {
            return clickBlock(blockPos, direction);
        }
    }

    //region unused
    // not going to lie, I looked at and rewrote the wrong function lol, this isn't used at all
    private boolean onPlayerDestroyBlockORIGINAL(BlockPos blockPos)
    {
        if (minecraft.player.getHeldItemMainhand().onBlockStartBreak(blockPos, minecraft.player))
        {
            return false;
        }

        if (minecraft.player.blockActionRestricted(minecraft.world, blockPos, controller.getCurrentGameType()))
        {
            return false;
        }
        else
        {
            World world = minecraft.world;
            BlockState blockState = world.getBlockState(blockPos);

            if (!minecraft.player.getHeldItemMainhand().getItem().canPlayerBreakBlockWhileHolding(blockState, world, blockPos, minecraft.player))
            {
                return false;
            }
            else
            {
                Block block = blockState.getBlock();

                if (block instanceof CommandBlockBlock || block instanceof StructureBlock || block instanceof JigsawBlock && !minecraft.player.canUseCommandBlock())
                {
                    return false;
                }
                else if (blockState.isAir(world, blockPos))
                {
                    return false;
                }
                else
                {
                    FluidState fluidState = world.getFluidState(blockPos);
                    boolean isBlockRemovedSuccessfully = blockState.removedByPlayer(world, blockPos, minecraft.player, false, fluidState);

                    if (isBlockRemovedSuccessfully)
                    {
                        block.onPlayerDestroy(world, blockPos, blockState);
                    }

                    return isBlockRemovedSuccessfully;
                }
            }
        }

    }

    public boolean clickBlockORIGINAL(BlockPos blockPos, Direction direction)
    {
        ClientPlayerEntity player = minecraft.player;
        ClientWorld world = minecraft.world;
        GameType currentGameType = controller.getCurrentGameType();

        if (player == null)
            return true;

        if (world == null)
            return true;

        if (player.blockActionRestricted(world, blockPos, minecraft.playerController.getCurrentGameType()))
        {
            return false;
        }
        else if (!world.getWorldBorder().contains(blockPos))
        {
            return false;
        }
        else
        {
            if (currentGameType.isCreative())
            {
                BlockState blockState = world.getBlockState(blockPos);
                minecraft.getTutorial().onHitBlock(world, blockPos, blockState, 1.0f);
                controller.sendDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, blockPos, direction);

                if (!ForgeHooks.onLeftClickBlock(player, blockPos, direction).isCanceled())
                {
                    controller.onPlayerDestroyBlock(blockPos);
                }
            }
            else if (!controller.isHittingBlock || !controller.isHittingPosition(blockPos))
            {
                if (controller.isHittingBlock)
                {
                    controller.sendDiggingPacket(CPlayerDiggingPacket.Action.ABORT_DESTROY_BLOCK, controller.currentBlock, direction);
                }

                PlayerInteractEvent.LeftClickBlock event = ForgeHooks.onLeftClickBlock(player, blockPos, direction);

                BlockState blockState = world.getBlockState(blockPos);
                minecraft.getTutorial().onHitBlock(world, blockPos, blockState, 0.0f);
                controller.sendDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, blockPos, direction);

                boolean isBlockAir = blockState.isAir(world, blockPos);

                if (!isBlockAir && controller.curBlockDamageMP == 0.0f)
                {
                    if (event.getUseBlock() != Event.Result.DENY)
                    {
                        blockState.onBlockClicked(world, blockPos, player);
                    }
                }

                if (event.getUseItem() == Event.Result.DENY)
                    return true;

                if (!isBlockAir && blockState.getPlayerRelativeBlockHardness(player, world, blockPos) >= 1.0f)
                {
                    controller.onPlayerDestroyBlock(blockPos);
                }
                else
                {
                    controller.isHittingBlock = true;
                    controller.currentBlock = blockPos;
                    controller.currentItemHittingBlock = player.getHeldItemMainhand();
                    controller.curBlockDamageMP = 0.0f;
                    world.sendBlockBreakProgress(player.getEntityId(), controller.currentBlock, (int) (controller.curBlockDamageMP * 10.0f) - 1);
                }
            }

            return true;
        }
    }
    //endregion
}
