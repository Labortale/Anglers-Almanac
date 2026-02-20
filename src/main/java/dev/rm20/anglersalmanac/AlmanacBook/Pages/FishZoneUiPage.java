package dev.rm20.anglersalmanac.AlmanacBook.Pages;

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
import dev.rm20.anglersalmanac.AlmanacBook.BookPageManager;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.models.BookAssetData;
import dev.rm20.anglersalmanac.utils.FishLootManager;
import dev.rm20.anglersalmanac.utils.StampUtil;
import dev.rm20.anglersalmanac.utils.TextUtils;
import dev.rm20.anglersalmanac.utils.pageUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static dev.rm20.anglersalmanac.AlmanacBook.BookPageManager.OpenPage;

public class FishZoneUiPage extends InteractiveCustomUIPage<StatUiPage.AlmanacGuiData> {
    private final String PlayerUUID;
    private final String PlayerName;
    private final AlmanacDatabase.PlayerStatsData Stats;
    private final String ZoneName;
    private final FishLootManager FishDataRight;
    private final int Page;
    private final BookAssetData.ZoneInfo zoneInfo;

    public FishZoneUiPage(PlayerRef playerRef, String playerUUID, String playerName, AlmanacDatabase.PlayerStatsData stats, String zoneName, FishLootManager fishDataRight, int page, BookAssetData.ZoneInfo ZoneInfo) {
        super(playerRef, CustomPageLifetime.CanDismiss, StatUiPage.AlmanacGuiData.CODEC);
        PlayerUUID = playerUUID;
        PlayerName = playerName;
        Stats = stats;
        ZoneName = zoneName;
        FishDataRight = fishDataRight;
        Page = page;
        zoneInfo = ZoneInfo;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Almanac/Fish/AlmanacFishZone.ui");
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#NextPageButton",
                EventData.of(StatUiPage.AlmanacGuiData.KEY_BUTTON, "NextPage"),
                false
        );
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#PrevPageButton",
                EventData.of(StatUiPage.AlmanacGuiData.KEY_BUTTON, "PrevPage"),
                false
        );
        BookAssetData bookAsset = BookAssetData.getMasterMergedBook();
        ;
        List<BookAssetData.FishEntry> Fish = bookAsset.getFishByHabitat(ZoneName);
        //AnglersAlmanac.getInstance().getLogger().atInfo().log(Fish.toString());
        uiCommandBuilder.set("#Header.Text", ZoneName + " Fish");
        uiCommandBuilder.set("#ZoneHeader.Text", zoneInfo.zoneDescription);
        uiCommandBuilder.set("#ZoneIconImage.AssetPath", zoneInfo.ZoneImage);
        if (zoneInfo.ProgressBarColour != null) {
            uiCommandBuilder.set("#FishProgress.Color", zoneInfo.ProgressBarColour);
        }
        List<BookAssetData.FishEntry> validFishItems = Fish.stream()
                .filter(BookAssetData.FishEntry::isItem)
                .toList();
        buildDynamicFishGrid(validFishItems, PlayerUUID, uiCommandBuilder);
        Map<String, BookAssetData.HabitatProgress> progressMap = bookAsset.getAllHabitatProgress(PlayerUUID);
        BookAssetData.HabitatProgress zoneProgress = progressMap.getOrDefault(ZoneName, new BookAssetData.HabitatProgress(0, 0));
        uiCommandBuilder.set("#FishProgress.Value", Math.min(1.0f, zoneProgress.getPercentage()));

        // Right page
        if (FishDataRight != null) {
            uiCommandBuilder.clear("#RightPageSlot");
            if (FishDataRight.getBookInfo() != null) {
                //Check if there is a custom page
                uiCommandBuilder.append("#RightPageSlot", Objects.requireNonNullElse(FishDataRight.getBookInfo().PageFileUI, "Almanac/Fish/Pages/FishPage0.ui"));
            } else {
                //Use default if no BookInfo
                uiCommandBuilder.append("#RightPageSlot", "Almanac/Fish/Pages/FishPage0.ui");
            }
            pageUtils.FillPage(uiCommandBuilder, "#RightPageSlot", FishDataRight, PlayerUUID, Stats);
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull StatUiPage.AlmanacGuiData data) {
        super.handleDataEvent(ref, store, data);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || data.getButton() == null) return;
        if (data.getButton().equals("PrevPage")) {
            OpenPage(player, (Page - 1), PlayerUUID, PlayerName);
        }
        if (data.getButton().equals("NextPage")) {
            int newPage = BookPageManager.getNextPage(Page);
            if (newPage == Page) {
                return;
            }
            OpenPage(player, newPage, PlayerUUID, PlayerName);
        }
    }


    private void buildDynamicFishGrid(List<BookAssetData.FishEntry> validFishItems, String playerUUID, @Nonnull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.clear("#FishGridContainer");

        int itemsPerRow = 5;
        int maxSlots = 25;
        int limit = Math.min(validFishItems.size(), maxSlots);
        for (int i = 0; i < limit; i++) {
            int rowIndex = i / itemsPerRow;
            int colIndex = i % itemsPerRow;
            String rowId = "#FishRow" + rowIndex;
            if (colIndex == 0) {
                uiCommandBuilder.appendInline("#FishGridContainer",
                        "Group " + rowId + " { LayoutMode: Left; Anchor: (Top: 10, Left: 10); }");
            }
            uiCommandBuilder.append(rowId, "Almanac/Utils/FishItemSlot.ui");
            AnglersAlmanac.LOGGER.atInfo().log(uiCommandBuilder.toString());
            String slotPath = rowId + "[" + colIndex + "]";

            BookAssetData.FishEntry currentFish = validFishItems.get(i);
            FishLootManager actualItem = FishLootManager.getFishData(currentFish.id());
            boolean hasCaught = AnglersAlmanac.getInstance().database.hasPlayerCaught(playerUUID, currentFish.id());

            if (hasCaught && actualItem != null) {
                uiCommandBuilder.set(slotPath + " #ItemIcon.ItemId", actualItem.getItemID());
                uiCommandBuilder.set(slotPath + " #ItemIcon.TooltipText", actualItem.getName());
                uiCommandBuilder.set(slotPath + " #ItemIconMissing.Visible", false);
            } else {
                uiCommandBuilder.set(slotPath + " #ItemIcon.ItemId", "");
                uiCommandBuilder.set(slotPath + " #ItemIconMissing.Visible", true);
                uiCommandBuilder.set(slotPath + " #ItemIconMissing.TooltipText", "Unknown Fish");
            }
        }
    }
}