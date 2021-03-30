package thatguy.mod.miningspeed2.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static thatguy.mod.miningspeed2.MiningSpeed.customPlayerController;
import static thatguy.mod.miningspeed2.MiningSpeed.minecraft;

@Mixin({Minecraft.class})
public class ClientMixins
{
    @Inject(method = {"sendClickBlockToController"}, at={@At("head")}, cancellable = true)
    public void sendClickBlockToController(boolean leftClick, CallbackInfo callbackInfo)
    {
        if (!leftClick)
        {
            minecraft.leftClickCounter = 0;
        }

        if (minecraft.leftClickCounter <= 0 && !minecraft.player.isHandActive())
        {
            if (leftClick && minecraft.objectMouseOver != null && minecraft.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK)
            {
                BlockPos blockpos = minecraft.objectMouseOver.getBlockPos();

                //minecraft.player.sendChatMessage("hello");
                minecraft.player.sendMessage(new TextComponentString(Boolean.toString(customPlayerController.hasBrokenBlock)));

                if (!minecraft.world.isAirBlock(blockpos) && customPlayerController.onPlayerDamageBlock(blockpos, minecraft.objectMouseOver.sideHit))
                {
                    minecraft.effectRenderer.addBlockHitEffects(blockpos, minecraft.objectMouseOver);
                    minecraft.player.swingArm(EnumHand.MAIN_HAND);
                }
            }
            else
            {
                minecraft.playerController.resetBlockRemoving();
            }
        }

        callbackInfo.cancel();
    }

}
