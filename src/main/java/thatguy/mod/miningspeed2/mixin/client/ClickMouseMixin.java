package thatguy.mod.miningspeed2.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thatguy.mod.miningspeed2.proxy.ClientProxy;

@Mixin(Minecraft.class)
public class ClickMouseMixin
{
    private static final Logger LOGGER = LogManager.getLogger();

    @Redirect(method = "processKeyBinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;clickMouse()V"))
    public void clickMouse(Minecraft minecraft)
    {
        if (minecraft.leftClickCounter <= 0)
        {
            if (minecraft.objectMouseOver == null)
            {
                LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");

                if (minecraft.playerController.isNotCreative())
                {
                    minecraft.leftClickCounter = 10;
                }
            }
            else if (!minecraft.player.isRowingBoat())
            {
                switch (minecraft.objectMouseOver.typeOfHit)
                {
                    case ENTITY:
                        minecraft.playerController.attackEntity(minecraft.player, minecraft.objectMouseOver.entityHit);
                        break;
                    case BLOCK:
                        BlockPos blockpos = minecraft.objectMouseOver.getBlockPos();

                        if (!minecraft.world.isAirBlock(blockpos))
                        {
                            ClientProxy.CUSTOM_PLAYER_CONTROLLER.clickBlock(blockpos, minecraft.objectMouseOver.sideHit);
                            break;
                        }

                    case MISS:

                        if (minecraft.playerController.isNotCreative())
                        {
                            minecraft.leftClickCounter = 10;
                        }

                        minecraft.player.resetCooldown();
                        net.minecraftforge.common.ForgeHooks.onEmptyLeftClick(minecraft.player);
                }

                minecraft.player.swingArm(EnumHand.MAIN_HAND);
            }
        }
    }


}
