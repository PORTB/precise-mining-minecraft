package thatguy.mod.miningspeed2;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config
{
    public static ForgeConfigSpec CLIENT_CONFIG;
    public static ForgeConfigSpec.BooleanValue CLIENT_ONLY_MODE_ENABLED;

    static
    {
        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();

        clientBuilder.comment("Client-only mode\nAllows the mod to be used on servers which don't have the mod installed\nDisables per-item control of mining control mode");

        CLIENT_ONLY_MODE_ENABLED = clientBuilder.define("client_only_mode", false);

        CLIENT_CONFIG = clientBuilder.build();
    }
}
