package thatguy.mod.miningspeed2.mixin.client;

import jdk.nashorn.internal.codegen.CompilerConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import scala.collection.parallel.ParIterableLike;
import thatguy.mod.miningspeed2.CustomPlayerController;

import static thatguy.mod.miningspeed2.MiningSpeed.customPlayerController;
import static thatguy.mod.miningspeed2.MiningSpeed.minecraft;

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
            } else if (!minecraft.player.isRowingBoat())
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
                            customPlayerController.clickBlock(blockpos, minecraft.objectMouseOver.sideHit);
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
