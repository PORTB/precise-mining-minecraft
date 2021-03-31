package thatguy.mod.miningspeed2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

@Mod(modid = Reference.MOD_ID, name = Reference.NAME, version = Reference.VERSION)
public class MiningSpeed
{
    public final static Minecraft minecraft = Minecraft.getMinecraft();
    public final static CustomPlayerController customPlayerController = new CustomPlayerController();

    public static Logger logger;
    public static boolean hasBrokenBlock = false;

    public static final SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);
    static
    {
        network.registerMessage(PacketModeToggle.Handler.class, PacketModeToggle.class, 0, Side.SERVER);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);

        logger = event.getModLog();

    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        ClientRegistry.registerKeyBinding(Reference.toggleSpeedControlKey);
    }

    @Mod.EventHandler
    public void construct(FMLServerAboutToStartEvent event)
    {
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void clientTickEvent(TickEvent.ClientTickEvent event)
    {
        resetHasBrokenBlockIfMouseNotPressed();
        handleModeToggleKey();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onShowItemTooltip(ItemTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();
        NBTTagCompound tag = stack.getTagCompound();

        if(isItemMiningTool(stack))
        {
            boolean isEnabled = tag != null && tag.getBoolean(Reference.MINING_CONTROL_ENABLED_TAG);

            TextComponentString enabled = new TextComponentString("Enabled");
            TextComponentString disabled = new TextComponentString("Disabled");

            enabled.getStyle().setColor(TextFormatting.GREEN);
            disabled.getStyle().setColor(TextFormatting.RED);

            event.getToolTip().add(new TextComponentString("Mining control is ").appendSibling(isEnabled ? enabled : disabled).getFormattedText());
        }
    }

    private void resetHasBrokenBlockIfMouseNotPressed()
    {
        if(!minecraft.gameSettings.keyBindAttack.isKeyDown())
        {
            MiningSpeed.hasBrokenBlock = false;
        }
    }

    private void handleModeToggleKey()
    {
        if(Reference.toggleSpeedControlKey.isPressed())
        {
            ItemStack heldItem = minecraft.player.getHeldItemMainhand();

            if(heldItem != ItemStack.EMPTY)
                if(isItemMiningTool(heldItem))
                    network.sendToServer(new PacketModeToggle());
        }
    }

    static public boolean isItemMiningTool(ItemStack stack)
    {
        if(stack.getItem() instanceof ItemShears)
            return true;

        return stack.getItem() instanceof ItemTool;
    }
}
