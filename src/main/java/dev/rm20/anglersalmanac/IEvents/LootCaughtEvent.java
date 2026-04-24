package dev.rm20.anglersalmanac.IEvents;

import com.hypixel.hytale.event.ICancellable;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import dev.rm20.anglersalmanac.Models.FishLootManager;

public class LootCaughtEvent implements IEvent<Void> {
    private final FishLootManager loot;
    private final Player player;
    private final boolean newDiscovery;

    public LootCaughtEvent(FishLootManager lootId, boolean newDiscovery, Player player) {
        this.loot = lootId;
        this.player = player;
        this.newDiscovery = newDiscovery;
    }

    public FishLootManager getLoot() { return loot; }
    public String getLootID() {return loot.getItemID();}
    public Player getPlayer() { return player; }
    public boolean isNewDiscovery() { return newDiscovery; }
}


