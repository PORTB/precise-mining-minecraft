package thatguy.mod.miningspeed2;

import net.minecraft.launchwrapper.Launch;

import java.util.Map;

public class EnvironmentInfo
{
    public static final String MEKANISM_ID = "mekanism";
    private static Boolean _isMekanismInstalled = null;

    @SuppressWarnings("unchecked")
    public static boolean isMekanismInstalled()
    {
        if (_isMekanismInstalled == null)
        {
            Map<String, Map<String, String>> modList = (Map<String, Map<String, String>>) Launch.blackboard.get("modList");

            for (Map.Entry<String, Map<String, String>> mod : modList.entrySet())
            {
                if (mod.getValue().get("id").equals(MEKANISM_ID))
                {
                    _isMekanismInstalled = true;
                    return true;
                }
            }

            _isMekanismInstalled = false;
        }

        return _isMekanismInstalled;
    }
}
