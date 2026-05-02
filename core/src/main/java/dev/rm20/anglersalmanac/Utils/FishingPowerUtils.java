package dev.rm20.anglersalmanac.Utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;

public class FishingPowerUtils {


    public static float getTotalFishingPower(Store<EntityStore> store, Ref<EntityStore> ref)
    {
        int FishingLevel = AnglersAlmanac.getInstance().skillTree.getFishingLevel(store,ref);
        float maxLevel = 100f;
        float maxPowerAtCap = 15.0f;
        float basePower = 1.0f;
        float fishingPower = basePower + (maxPowerAtCap - basePower) * (float) Math.pow(FishingLevel / maxLevel, 3);
        return fishingPower;
    }
}
