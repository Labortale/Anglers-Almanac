package dev.rm20.anglersalmanac.events;

import com.hypixel.hytale.assetstore.event.AssetStoreMonitorEvent;
import dev.rm20.anglersalmanac.models.BookAssetData;
import dev.rm20.anglersalmanac.registration.EventInfo;
import dev.rm20.anglersalmanac.utils.FishLootManager;


@EventInfo(AssetStoreMonitorEvent.class)
public class OnAssetMonitorEvent {
    public static void handle(AssetStoreMonitorEvent event) {
        //AnglersAlmanac.getInstance().getLogger().atInfo().log("Clearing book Cache");
        BookAssetData.invalidateCache();
        FishLootManager.invalidateCache();

    }
}
