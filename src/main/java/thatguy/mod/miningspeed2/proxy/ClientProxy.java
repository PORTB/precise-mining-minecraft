package thatguy.mod.miningspeed2.proxy;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static thatguy.mod.miningspeed2.MiningSpeed.customPlayerController;
import static thatguy.mod.miningspeed2.MiningSpeed.minecraft;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    @SubscribeEvent
    public void clientTickEvent(TickEvent.ClientTickEvent event)
    {
        resetHasBrokenBlockIfMouseNotPressed();
    }

    private void resetHasBrokenBlockIfMouseNotPressed()
    {
        if(!minecraft.gameSettings.keyBindAttack.isKeyDown())
        {
            customPlayerController.hasBrokenBlock = false;
        }
    }
}
