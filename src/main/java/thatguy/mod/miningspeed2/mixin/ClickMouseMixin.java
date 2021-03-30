package thatguy.mod.miningspeed2.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static thatguy.mod.miningspeed2.MiningSpeed.customPlayerController;

//@Mixin({PlayerControllerMP.class})
public class ClickMouseMixin
{
    //@Redirect(method = "clickMouse", at=@At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;clickBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z"))
    //@Inject(method = {"clickBlock"}, at={@At("HEAD")}, cancellable = true)
    public boolean clickBlock(PlayerControllerMP playerControllerMP, BlockPos location, EnumFacing facing)
    {
        return false;
        //return customPlayerController.clickBlock(location, facing);
    }
}
