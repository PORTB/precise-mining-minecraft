package thatguy.mod.miningspeed2.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static thatguy.mod.miningspeed2.MiningSpeed.customPlayerController;
import static thatguy.mod.miningspeed2.MiningSpeed.minecraft;

@Mixin(Minecraft.class)
public class Cock
{
    @Redirect(method = "processKeyBinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;sendClickBlockToController(Z)V"))
    private void sendClickBlockToController(Minecraft minecraft, boolean leftClick)
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
            } else
            {
                minecraft.playerController.resetBlockRemoving();
            }
        }
    }
}
