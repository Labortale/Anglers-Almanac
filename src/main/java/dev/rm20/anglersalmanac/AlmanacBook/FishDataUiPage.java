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
import dev.rm20.anglersalmanac.utils.FishLootManager;
import dev.rm20.anglersalmanac.utils.TextUtils;

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
        AnglersAlmanac.getInstance().getLogger().atInfo().log(FishDataLeft.getName());
        AnglersAlmanac.getInstance().getLogger().atInfo().log(FishDataRight.getName());

        // Left page
        if (FishDataLeft != null) {
            //check if fish has be found by player

            uiCommandBuilder.set("#Header.TextSpans", Message.raw(TextUtils.scrambleText(FishDataLeft.getName())));
            uiCommandBuilder.set("#CountNumber.TextSpans", Message.raw("0"));
            uiCommandBuilder.set("#Family.TextSpans", Message.raw(TextUtils.scrambleText(FishDataLeft.getFamilyId())));
            uiCommandBuilder.set("#Description.TextSpans", Message.raw(TextUtils.scrambleText(FishDataLeft.getDescription())));


        }

        // Right page
        if(FishDataRight !=null)
        {
            uiCommandBuilder.set("#Header2.TextSpans", Message.raw(FishDataRight.getName()));
            uiCommandBuilder.set("#CountNumber2.TextSpans", Message.raw("0"));
            uiCommandBuilder.set("#Family2.TextSpans", Message.raw(FishDataRight.getFamilyId()));
            uiCommandBuilder.set("#Description2.TextSpans", Message.raw(FishDataRight.getDescription()));

        }
    }
}
