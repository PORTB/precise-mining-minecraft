package thatguy.mod.miningspeed2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.Logger;
import scala.collection.parallel.ParIterableLike;

@Mod(modid = MiningSpeed.MODID, name = MiningSpeed.NAME, version = MiningSpeed.VERSION)
public class MiningSpeed
{
    public static final String MODID = "miningspeed2";
    public static final String NAME = "Precision Mining";
    public static final String VERSION = "1.0";

    public final static Minecraft minecraft = Minecraft.getMinecraft();
    public final static PlayerControllerMP playerController = minecraft.playerController;
    public final static CustomPlayerController customPlayerController = new CustomPlayerController();

    public static Logger logger;
    public static boolean hasBrokenBlock = false;


    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);

        logger = event.getModLog();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
        logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    @SubscribeEvent
    public void clientTickEvent(TickEvent.ClientTickEvent event)
    {
        resetHasBrokenBlockIfMouseNotPressed();
    }

    private void resetHasBrokenBlockIfMouseNotPressed()
    {
        if(!minecraft.gameSettings.keyBindAttack.isKeyDown())
        {
            MiningSpeed.hasBrokenBlock = false;
        }
    }
}
