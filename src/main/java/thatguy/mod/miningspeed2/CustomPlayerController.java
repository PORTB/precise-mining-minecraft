package thatguy.mod.miningspeed2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;

public class CustomPlayerController
{
    private final static PlayerControllerMP playerController = Minecraft.getMinecraft().playerController;

    public boolean hasBrokenBlock = false;
}
