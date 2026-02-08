package dev.rm20.anglersalmanac.AlmanacBook;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.MinigameManager.SkillCheck.DialEventData;
import dev.rm20.anglersalmanac.utils.FishLootManager;

import javax.annotation.Nonnull;

public class FishDataUiPage extends InteractiveCustomUIPage<DialEventData> {
    private String PlayerUUID;
    private String PlayerName;
    private AlmanacDatabase.PlayerStatsData Stats;
    private FishLootManager FishDataLeft;
    private FishLootManager FishDataRight;
    public FishDataUiPage(PlayerRef playerRef, String playerUUID, String playerName, AlmanacDatabase.PlayerStatsData stats,FishLootManager fishDataLeft,FishLootManager fishDataRight) {
        super(playerRef, CustomPageLifetime.CanDismiss, DialEventData.CODEC);
        PlayerUUID = playerUUID;
        PlayerName = playerName;
        Stats = stats;
        this.FishDataLeft = fishDataLeft;
        FishDataRight = fishDataRight;
    }

    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder,
                      @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Almanac/Fish/AlmanacFish.ui");

    }
}
