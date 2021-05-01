package thatguy.mod.miningspeed2.proxy;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import thatguy.mod.miningspeed2.Reference;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class CommonProxy
{
    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
    }

}
