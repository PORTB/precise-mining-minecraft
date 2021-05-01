package thatguy.mod.miningspeed2.proxy;

import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import thatguy.mod.miningspeed2.Reference;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class CommonProxy
{
    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {}

}
