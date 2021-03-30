package thatguy.mod.miningspeed2;

import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MiningSpeed.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerSide
{
    public static void init()
    {
        Networking.register();
    }
}
