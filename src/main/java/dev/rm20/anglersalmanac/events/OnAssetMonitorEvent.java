package dev.rm20.anglersalmanac.events;

import com.hypixel.hytale.assetstore.event.AssetStoreMonitorEvent;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.models.BookAssetData;
import dev.rm20.anglersalmanac.registration.EventInfo;


@EventInfo(AssetStoreMonitorEvent.class)
public class OnAssetMonitorEvent {
    public static void handle(AssetStoreMonitorEvent event) {
        if(event.getAssetPack().equals("dev.rm20:AnglersAlmanac"))
        {
            //AnglersAlmanac.getInstance().getLogger().atInfo().log("Clearing book Cache");
            BookAssetData.invalidateCache();
        }
    }
}
