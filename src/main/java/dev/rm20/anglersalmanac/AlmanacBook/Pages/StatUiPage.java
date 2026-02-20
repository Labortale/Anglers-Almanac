package dev.rm20.anglersalmanac.AlmanacBook.Pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AlmanacBook.AlmanacDatabase;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.MinigameManager.Minigame;
import dev.rm20.anglersalmanac.models.BookAssetData;
import dev.rm20.anglersalmanac.utils.FishLootManager;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.Map;

import static dev.rm20.anglersalmanac.AlmanacBook.BookPageManager.OpenPage;


public class StatUiPage extends InteractiveCustomUIPage<StatUiPage.AlmanacGuiData> {

    private final String PlayerUUID;
    private final String PlayerName;
    private final AlmanacDatabase.PlayerStatsData stats;
    public StatUiPage(PlayerRef playerRef, String playerUUID, String playerName,AlmanacDatabase.PlayerStatsData stats) {
        super(playerRef, CustomPageLifetime.CanDismiss, AlmanacGuiData.CODEC);
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

        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#NextPageButton",
                EventData.of(AlmanacGuiData.KEY_BUTTON, "NextPage"),
                false
        );

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


        //Right page
        BookAssetData bookAsset = BookAssetData.getMasterMergedBook();
        Map<String, BookAssetData.HabitatProgress> progress = bookAsset.getAllHabitatProgress(PlayerUUID);
        int globalCaught = progress.values().stream().mapToInt(p -> p.caught()).sum();
        int globalTotal = progress.values().stream().mapToInt(p -> p.total()).sum();
        float globalPercent = (globalTotal > 0) ? (float) globalCaught / globalTotal : 0;
        uiCommandBuilder.set("#FishProgress.Value",globalPercent );
        CreateList(uiCommandBuilder,uiEventBuilder,bookAsset);
    }
    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull AlmanacGuiData data) {
        super.handleDataEvent(ref, store, data);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || data.getButton() == null) return;
        AnglersAlmanac.LOGGER.atInfo().log(data.getButton());
        if (data.getButton().equals("NextPage")) {
            OpenPage(player, 1, PlayerUUID, PlayerName);
        }
        // Zone click
        else if (data.getButton().startsWith("OpenZone:")) {
            String zoneName = data.getButton().split(":")[1];
            AnglersAlmanac.LOGGER.atInfo().log(zoneName);
        }
    }
    public static class AlmanacGuiData {
        static final String KEY_BUTTON = "Button";

        public static final BuilderCodec<AlmanacGuiData> CODEC = BuilderCodec.builder(AlmanacGuiData.class, AlmanacGuiData::new)
                .append(new KeyedCodec<>(KEY_BUTTON, Codec.STRING),
                        (data, val) -> data.button = val,
                        data -> data.button)
                .add()
                .build();

        private String button;

        public String getButton() {
            return button;
        }
    }

    public Message createProgressBar(int caught, int total) {
        int barWidth = 15;
        int filledCount = (total > 0) ? (int) ((float) caught / total * barWidth) : 0;

        Message bar = Message.raw("[").color("#494950");
        Message filledPart = Message.raw("|".repeat(filledCount)).color("#3498DB");
        Message emptyPart = Message.raw("-".repeat(barWidth - filledCount)).color("#494950");
        Message closingBracket = Message.raw("]").color("#494950");

        return bar.insertAll(filledPart, emptyPart, closingBracket);
    }

    public void CreateList(UICommandBuilder uiCommandBuilder, UIEventBuilder uiEventBuilder, BookAssetData bookAsset)
    {
        uiCommandBuilder.clear("#ZonessInfo");
        // 1. Get progress for all habitats
        Map<String, BookAssetData.HabitatProgress> allProgress = bookAsset.getAllHabitatProgress(PlayerUUID);
        List<String> sortedZones = allProgress.keySet().stream()
                .sorted((a, b) -> {
                    if (a.equalsIgnoreCase("Ocean")) return -1; // Ocean always first
                    if (b.equalsIgnoreCase("Ocean")) return 1;
                    return a.compareToIgnoreCase(b); // Others alphabetical (Zone1, Zone2, etc.)
                })
                .toList();
        int index = 0;
        for (String zoneName : sortedZones) {
            BookAssetData.HabitatProgress habitat = allProgress.get(zoneName);

            // 2. Append the template to the scrolling group
            uiCommandBuilder.append("#ZonessInfo", "Almanac/Utils/HabitatEntry.ui");

            // 3. Target the specific element by its index in the container
            String basePath = "#ZonessInfo[" + index + "]";

            // Set the Name and the ASCII Progress Bar
            uiCommandBuilder.set(basePath + " #HabitatHeader.Text", zoneName + " Progress:");

            Message progressBar = createProgressBar(habitat.caught(), habitat.total());
            uiCommandBuilder.set(basePath + " #HabitatProgressText.TextSpans", progressBar);

            // Add Tooltip (e.g., "12/20 -> Click to view")
            uiCommandBuilder.set(basePath + " #HabitatButton.TooltipText",
                    habitat.caught() + "/" + habitat.total() + " -> Click to view");

            // 4. Add Event Binding to open that specific zone's page
            // We send the zone name as the event data
            uiEventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    basePath + " #HabitatButton",
                    EventData.of(AlmanacGuiData.KEY_BUTTON, "OpenZone:" + zoneName),
                    false
            );

            index++;
        }
    }
}
