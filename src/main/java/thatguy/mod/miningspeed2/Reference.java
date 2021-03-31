package thatguy.mod.miningspeed2;

import net.minecraft.client.settings.KeyBinding;

public class Reference
{
    public static final String MOD_ID = "miningspeed2";
    public static final String NAME = "Precision Mining";
    public static final String VERSION = "1.3";

    public final static String MINING_CONTROL_ENABLED_TAG = "mining_control_enabled";

    public static KeyBinding toggleSpeedControlKey = new KeyBinding("Toggle Mining Speed Control", 43 /*backslash*/, "Mining Control");
}
