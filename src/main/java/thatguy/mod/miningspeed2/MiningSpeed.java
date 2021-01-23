package thatguy.mod.miningspeed2;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MiningSpeed.MODID)
public class MiningSpeed
{
    public final static String MINING_SPEED_CONTROL_ENABLED_TAG = "mining_speed_enabled";
    public final static String MODID = "miningspeed2";

    public MiningSpeed()
    {
        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientSide::init);
        Networking.register();

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
