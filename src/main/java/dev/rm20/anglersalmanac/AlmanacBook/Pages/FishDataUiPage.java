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
import dev.rm20.anglersalmanac.utils.FishLootManager;
import dev.rm20.anglersalmanac.utils.StampUtil;
import dev.rm20.anglersalmanac.utils.TextUtils;
import dev.rm20.anglersalmanac.utils.pageUtils;

import javax.annotation.Nonnull;

import java.util.Objects;

import static dev.rm20.anglersalmanac.AlmanacBook.BookPageManager.OpenPage;

public class FishDataUiPage extends InteractiveCustomUIPage<StatUiPage.AlmanacGuiData> {
    private final String PlayerUUID;
    private final String PlayerName;
    private final AlmanacDatabase.PlayerStatsData Stats;
    private final FishLootManager FishDataLeft;
    private final FishLootManager FishDataRight;
    private final int Page;
    private final String UiFile;

    public FishDataUiPage(PlayerRef playerRef, String playerUUID, String playerName, AlmanacDatabase.PlayerStatsData stats, FishLootManager fishDataLeft, FishLootManager fishDataRight, int page, String uiFile) {
        super(playerRef, CustomPageLifetime.CanDismiss, StatUiPage.AlmanacGuiData.CODEC);
        PlayerUUID = playerUUID;
        PlayerName = playerName;
        Stats = stats;
        FishDataLeft = fishDataLeft;
        FishDataRight = fishDataRight;
        Page = page;
        UiFile = uiFile;
    }

    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder,
                      @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append(UiFile);

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

        //Left side
        if (FishDataLeft != null) {
            uiCommandBuilder.clear("#LeftPageSlot");
            if (FishDataLeft.getBookInfo() != null) {
                //Check if there is a custom page
                uiCommandBuilder.append("#LeftPageSlot", Objects.requireNonNullElse(FishDataLeft.getBookInfo().PageFileUI, "Almanac/Fish/Pages/FishPage0.ui"));
            } else {
                //Use default if no BookInfo
                uiCommandBuilder.append("#LeftPageSlot", "Almanac/Fish/Pages/FishPage0.ui");
            }
            pageUtils.FillPage(uiCommandBuilder, "#LeftPageSlot", FishDataLeft, PlayerUUID, Stats);
        }
        //Right side
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

}
