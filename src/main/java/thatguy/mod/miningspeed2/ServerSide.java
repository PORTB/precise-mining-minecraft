package thatguy.mod.miningspeed2;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;

@Mod.EventBusSubscriber(modid = MiningSpeed.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerSide
{
    @SubscribeEvent
    public static void init(FMLDedicatedServerSetupEvent event)
    {
        Networking.register();
    }
}
