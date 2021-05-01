package thatguy.mod.miningspeed2.proxy;

import mekanism.common.item.ItemAtomicDisassembler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thatguy.mod.miningspeed2.*;

import java.sql.Ref;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Reference.MOD_ID)
public class ClientProxy extends CommonProxy
{
    public static final KeyBinding TOGGLE_SPEED_CONTROL_KEY = new KeyBinding("Toggle Mining Speed Control", 43 /*backslash*/, "Mining Control");
    public final static Minecraft MINECRAFT = Minecraft.getMinecraft();
    public final static CustomPlayerController CUSTOM_PLAYER_CONTROLLER = new CustomPlayerController();

    @Mod.EventHandler
    @Override
    public void init(FMLInitializationEvent event)
    {
        ClientRegistry.registerKeyBinding(TOGGLE_SPEED_CONTROL_KEY);
    }

    @SubscribeEvent
    static public void clientTickEvent(TickEvent.ClientTickEvent event)
    {
        resetHasBrokenBlockIfMouseNotPressed();
        handleModeToggleKey();
    }

    @SubscribeEvent
    static public void onShowItemTooltip(ItemTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();
        NBTTagCompound tag = stack.getTagCompound();

        if (isItemMiningTool(stack))
        {
            boolean isEnabled = tag != null && tag.getBoolean(Reference.MINING_CONTROL_ENABLED_TAG);

            TextComponentString enabled = new TextComponentString("Enabled");
            TextComponentString disabled = new TextComponentString("Disabled");

            enabled.getStyle().setColor(TextFormatting.GREEN);
            disabled.getStyle().setColor(TextFormatting.RED);

            event.getToolTip().add(new TextComponentString("Mining control is ").appendSibling(isEnabled ? enabled : disabled).getFormattedText());
        }
    }

    static private void resetHasBrokenBlockIfMouseNotPressed()
    {
        if (!MINECRAFT.gameSettings.keyBindAttack.isKeyDown())
        {
            CustomPlayerController.hasBrokenBlock = false;
        }
    }

    static private void handleModeToggleKey()
    {
        if (TOGGLE_SPEED_CONTROL_KEY.isPressed())
        {
            ItemStack heldItem = MINECRAFT.player.getHeldItemMainhand();

            if (heldItem != ItemStack.EMPTY)
                if (isItemMiningTool(heldItem))
                    MiningSpeed.network.sendToServer(new PacketModeToggle());
        }
    }

    static public boolean isItemMiningTool(ItemStack stack)
    {
        if (stack.getItem() instanceof ItemShears)
            return true;

        if (EnvironmentInfo.isMekanismInstalled())
        {
            if (stack.getItem() instanceof ItemAtomicDisassembler)
                return true;
        }

        return !stack.getItem().getToolClasses(stack).isEmpty();
    }
}
