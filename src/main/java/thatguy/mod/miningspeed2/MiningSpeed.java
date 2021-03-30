package thatguy.mod.miningspeed2;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import scala.collection.parallel.ParIterableLike;

@Mod(modid = MiningSpeed.MODID, name = MiningSpeed.NAME, version = MiningSpeed.VERSION)
public class MiningSpeed
{
    public static final String MODID = "miningspeed2";
    public static final String NAME = "Precision Mining";
    public static final String VERSION = "1.0";

    public static Minecraft minecraft = Minecraft.getMinecraft();

    private static Logger logger;

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
