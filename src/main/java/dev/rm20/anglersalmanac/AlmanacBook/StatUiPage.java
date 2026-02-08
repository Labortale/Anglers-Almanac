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
import dev.rm20.anglersalmanac.MinigameManager.Minigame;
import dev.rm20.anglersalmanac.MinigameManager.SkillCheck.DialEventData;
import dev.rm20.anglersalmanac.utils.FishLootManager;

import javax.annotation.Nonnull;


public class StatUiPage extends InteractiveCustomUIPage<DialEventData> {

    private String PlayerUUID;
    private String PlayerName;
    private AlmanacDatabase.PlayerStatsData stats;
    public StatUiPage(PlayerRef playerRef, String playerUUID, String playerName,AlmanacDatabase.PlayerStatsData stats) {
        super(playerRef, CustomPageLifetime.CanDismiss, DialEventData.CODEC);
        PlayerUUID = playerUUID;
        PlayerName = playerName;
        this.stats = stats;
    }
    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder,
                      @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {


        uiCommandBuilder.append("Almanac/AlmanacStats.ui");

        //AlmanacDatabase db = AnglersAlmanac.getInstance().database;
        //AlmanacDatabase.PlayerStatsData stats = db.getPlayerStats(this.PlayerUUID);

        uiCommandBuilder.set("#TotalFish.TextSpans", Message.raw("Total fish: " + stats.totalCatches));
        uiCommandBuilder.set("#LegendaryCount.TextSpans", Message.raw("Legendary fish: " + stats.legendaryCount));
        int perfects = stats.getRatingCount(Minigame.PerformanceRating.PERFECT);
        uiCommandBuilder.set("#PerfectCount.TextSpans", Message.raw("Perfects: " + perfects));
        int great = stats.getRatingCount(Minigame.PerformanceRating.GREAT);
        uiCommandBuilder.set("#GreatCount.TextSpans", Message.raw("Great: " + great));
        uiCommandBuilder.set("#OwnerInfo.TextSpans", Message.raw("Property of: " + (this.PlayerName != null ? this.PlayerName : "Unknown")));

        String topFish = (stats.topFish != null && !stats.topFish.isEmpty()) ?FishLootManager.getFishData(stats.topFish.get(0).name()).getName() : "None yet!";
        uiCommandBuilder.set("#MostFound.TextSpans", Message.raw("Most found: " + topFish));


        for (int i = 0; i < 10; i++) {
            String labelId = "#Fish" + (i + 1);

            if (stats.topFish != null && i < stats.topFish.size()) {
                AlmanacDatabase.FishEntry entry = stats.topFish.get(i);
                FishLootManager loot = FishLootManager.getFishData(entry.name());
                uiCommandBuilder.set(labelId + ".TextSpans", Message.raw("- " + loot.getName() + " : " + entry.count()));
            } else {
                uiCommandBuilder.set(labelId + ".TextSpans", Message.raw("- ??? : 0"));
            }
        }

    }
}
