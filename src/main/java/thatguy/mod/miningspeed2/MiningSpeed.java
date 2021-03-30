package thatguy.mod.miningspeed2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.Logger;
import scala.collection.parallel.ParIterableLike;
import thatguy.mod.miningspeed2.proxy.CommonProxy;

@Mod(modid = MiningSpeed.MODID, name = MiningSpeed.NAME, version = MiningSpeed.VERSION)
public class MiningSpeed
{
    public static final String MODID = "miningspeed2";
    public static final String NAME = "Precision Mining";
    public static final String VERSION = "1.0";

    public final static Minecraft minecraft = Minecraft.getMinecraft();
    public final static PlayerControllerMP playerController = minecraft.playerController;
    public final static CustomPlayerController customPlayerController = new CustomPlayerController();

    private static Logger logger;

    @SidedProxy(clientSide = "thatguy.mod.miningspeed2.proxy.ClientProxy",
            serverSide = "thatguy.mod.miningspeed2.proxy.CommonProxy",
            modId = MODID
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
        logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }
}
