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
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static thatguy.mod.miningspeed2.MiningSpeed.customPlayerController;
import static thatguy.mod.miningspeed2.MiningSpeed.minecraft;

@Mixin({Minecraft.class})
public class ClientMixins
{
    //@Inject(method = {"sendClickBlockToController"}, at={@At("invoke")}, cancellable = true)
    @Redirect(method = {"sendClickBlockToController"}, at = @At("INVOKE"))
    public void sendClickBlockToController(boolean leftClick)
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

                //minecraft.player.sendMessage(new TextComponentString("hello" + System.nanoTime() % 10000));

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
    }
}
