package thatguy.mod.miningspeed2;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MiningSpeed.MODID)
public class MiningSpeed
{
    public final static String MINING_SPEED_CONTROL_ENABLED_TAG = "mining_speed_enabled";
    public final static String MODID = "miningspeed2";

    private static boolean isClientSideOnlyModeEnabled = false;

    public MiningSpeed()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);

        isClientSideOnlyModeEnabled = Config.CLIENT_ONLY_MODE_ENABLED.get();

        if (isClientSideOnlyModeEnabled)
        {
            //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        }
        else
        {
            Networking.register();
        }

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> MiningSpeed::initClientSide);
    }

    private static void initClientSide()
    {
        ClientSide.init(isClientSideOnlyModeEnabled);
    }
}
