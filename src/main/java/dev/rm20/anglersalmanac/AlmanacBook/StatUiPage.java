package dev.rm20.anglersalmanac.AlmanacBook;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.MinigameManager.SkillCheck.DialEventData;

import javax.annotation.Nonnull;


public class StatUiPage extends InteractiveCustomUIPage<DialEventData> {

    private String PlayerUUID;
    private String PlayerName;
    public StatUiPage(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, DialEventData.CODEC);
        PlayerUUID = null;
    }
    public StatUiPage(PlayerRef playerRef, String playerUUID, String playerName) {
        super(playerRef, CustomPageLifetime.CanDismiss, DialEventData.CODEC);
        PlayerUUID = playerUUID;
        PlayerName = playerName;
    }
    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder,
                      @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {


        uiCommandBuilder.append("Almanac/AlmanacStats.ui");

        AlmanacDatabase db = AnglersAlmanac.getInstance().database;
        AlmanacDatabase.PlayerStatsData stats = db.getPlayerStats(this.PlayerUUID);

        uiCommandBuilder.set("#TotalFish.TextSpans", Message.raw("Total fish: " + stats.totalCatches));
        uiCommandBuilder.set("#LegendaryCount.TextSpans", Message.raw("Legendary fish: " + stats.legendaryCount));
        uiCommandBuilder.set("#OwnerInfo.TextSpans", Message.raw("Property of: " + (this.PlayerName != null ? this.PlayerName : "Unknown")));

        String topFish = (stats.topFish != null && !stats.topFish.isEmpty()) ? stats.topFish.get(0).name() : "None yet!";
        uiCommandBuilder.set("#MostFound.TextSpans", Message.raw("Most found: " + topFish));


        for (int i = 0; i < 10; i++) {
            String labelId = "#Fish" + (i + 1);

            if (stats.topFish != null && i < stats.topFish.size()) {
                AlmanacDatabase.FishEntry entry = stats.topFish.get(i);
                uiCommandBuilder.set(labelId + ".TextSpans", Message.raw("- " + entry.name() + " : " + entry.count()));
            } else {
                uiCommandBuilder.set(labelId + ".TextSpans", Message.raw("- ??? : 0"));
            }
        }

    }
}
