package thatguy.mod.miningspeed2.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.EnumFacing;
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
import thatguy.mod.miningspeed2.CustomPlayerController;

import static thatguy.mod.miningspeed2.MiningSpeed.customPlayerController;
import static thatguy.mod.miningspeed2.MiningSpeed.minecraft;

@Mixin({Minecraft.class})
public class ClientMixins
{
    //@Inject(method = {"sendClickBlockToController"}, at={@At("invoke")}, cancellable = true)
    @Redirect(method = "processKeyBinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;sendClickBlockToController(Z)V"))
    public void sendClickBlockToController(Minecraft minecraft, boolean leftClick)
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

    @Redirect(method = "clickMouse", at=@At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;clickBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)V"))
    public void clickBlock(PlayerControllerMP controller, BlockPos location, EnumFacing facing)
    {
        customPlayerController.clickBlock(location, facing);
    }
}
