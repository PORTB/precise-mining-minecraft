package thatguy.mod.miningspeed2;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MiningSpeed.MODID)
public class MiningSpeed
{
    public final static String MINING_SPEED_CONTROL_ENABLED_TAG = "mining_speed_enabled";
    public final static String MODID = "miningspeed2";

    public MiningSpeed()
    {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientSide::init);
        Networking.register();
    }
}
