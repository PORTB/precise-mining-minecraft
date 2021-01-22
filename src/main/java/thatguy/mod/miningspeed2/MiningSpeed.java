package thatguy.mod.miningspeed2;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MiningSpeed.MODID)
public class MiningSpeed
{
    public final static String MINING_SPEED_CONTROL_ENABLED_TAG = "mining_speed_enabled";
    public final static String MODID = "miningspeed2";


    //@OnlyIn(Dist.CLIENT)
    public MiningSpeed()
    {
        // Register ourselves for server and other game events we are interested in
        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible

        //ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, C);

        //MinecraftForge.EVENT_BUS.register(ClientSide::);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ServerSide::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSide::init);
    }

    //Function used to purposley lag the server for testing with bad tps.
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



}
