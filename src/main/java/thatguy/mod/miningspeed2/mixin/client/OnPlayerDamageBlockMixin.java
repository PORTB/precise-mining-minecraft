package thatguy.mod.miningspeed2.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thatguy.mod.miningspeed2.proxy.ClientProxy;

@Mixin(Minecraft.class)
public class OnPlayerDamageBlockMixin
{
    @Redirect(method = "sendClickBlockToController", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;onPlayerDamageBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z"))
    private boolean onPlayerDamageBlock(PlayerControllerMP playerController, BlockPos blockPos, EnumFacing sideHit)
    {
        return ClientProxy.CUSTOM_PLAYER_CONTROLLER.onPlayerDamageBlock(blockPos, sideHit);
    }
}
