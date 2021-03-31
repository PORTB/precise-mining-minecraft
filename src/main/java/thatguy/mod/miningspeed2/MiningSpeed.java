package thatguy.mod.miningspeed2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;

@Mod(modid = Reference.MOD_ID, name = Reference.NAME, version = Reference.VERSION)
public class MiningSpeed
{
    public final static Minecraft minecraft = Minecraft.getMinecraft();
    public final static PlayerControllerMP playerController = minecraft.playerController;
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

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void clientTickEvent(TickEvent.ClientTickEvent event)
    {
        resetHasBrokenBlockIfMouseNotPressed();
        handleModeToggleKey();
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
