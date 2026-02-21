package dev.rm20.anglersalmanac.events;

import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.Universe;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.models.BookAssetData;
import dev.rm20.anglersalmanac.registration.EventInfo;

@EventInfo(PlayerDisconnectEvent.class)
public class OnPlayerDisconnectEvent {
    public static void handle(PlayerDisconnectEvent event) {
        if(Universe.get().getPlayerCount()<=1)
        {
            //AnglersAlmanac.getInstance().getLogger().atInfo().log("Clearing book Cache");
            BookAssetData.invalidateCache();
        }

    }
}
