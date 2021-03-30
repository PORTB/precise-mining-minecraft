package thatguy.mod.miningspeed2.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Minecraft.class})
public class ClientMixins
{
    @Inject(method = {"sendClickBlockToController"}, at={@At("head")}, cancellable = true)
    public void sendClickBlockToController(boolean leftClick, CallbackInfo callbackInfo)
    {

    }

}
