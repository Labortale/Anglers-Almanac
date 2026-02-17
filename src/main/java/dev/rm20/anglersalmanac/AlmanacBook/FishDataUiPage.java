package dev.rm20.anglersalmanac.AlmanacBook;

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
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.MinigameManager.SkillCheck.DialEventData;
import dev.rm20.anglersalmanac.utils.FishLootManager;
import dev.rm20.anglersalmanac.utils.StampUtil;
import dev.rm20.anglersalmanac.utils.TextUtils;

import javax.annotation.Nonnull;

import static dev.rm20.anglersalmanac.AlmanacBook.BookPageManager.OpenPage;

public class FishDataUiPage extends InteractiveCustomUIPage<StatUiPage.AlmanacGuiData> {
    private String PlayerUUID;
    private String PlayerName;
    private AlmanacDatabase.PlayerStatsData Stats;
    private FishLootManager FishDataLeft;
    private FishLootManager FishDataRight;
    private int Page;
    public FishDataUiPage(PlayerRef playerRef, String playerUUID, String playerName, AlmanacDatabase.PlayerStatsData stats,FishLootManager fishDataLeft,FishLootManager fishDataRight, int page) {
        super(playerRef, CustomPageLifetime.CanDismiss, StatUiPage.AlmanacGuiData.CODEC);
        PlayerUUID = playerUUID;
        PlayerName = playerName;
        Stats = stats;
        this.FishDataLeft = fishDataLeft;
        FishDataRight = fishDataRight;
        Page = page;
    }

    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder,
                      @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Almanac/Fish/AlmanacFishZone.ui");
        AnglersAlmanac.getInstance().getLogger().atInfo().log(FishDataLeft.getName());
        AnglersAlmanac.getInstance().getLogger().atInfo().log(FishDataRight.getName());

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

        // Left page
        if (FishDataLeft != null) {
            //check if fish has be found by player
//            if(AnglersAlmanac.getInstance().database.hasPlayerCaught(PlayerUUID, FishDataLeft.getId()))
//            {
//                int fishCount = Stats.getFishCount(FishDataLeft.getId());
//                String cleanName = FishDataLeft.getItemID()
//                        .replace("Fish_", "")
//                        .replace("_Item", "");
//
//                String Image = "UI/Custom/Almanac/Fish/Assets/" + cleanName + ".png";
//                String RarityFile = StampUtil.getStamp(cleanName,FishDataLeft.getRarity());
//                if(RarityFile != null)
//                {
//                    String RarityPath = "UI/Custom/Almanac/Fish/Stamps/"+FishDataLeft.getRarity()+"/"+RarityFile+".png";
//                    uiCommandBuilder.set("#StampImage.AssetPath", RarityPath);
//
//                }
//                uiCommandBuilder.set("#FishImage.AssetPath", Image);
//                uiCommandBuilder.set("#Header.TextSpans", Message.raw(FishDataLeft.getName()));
//                uiCommandBuilder.set("#CountNumber.TextSpans", Message.raw(String.valueOf(fishCount)));
//                uiCommandBuilder.set("#Family.TextSpans", Message.raw(TextUtils.formatDisplayName(FishDataLeft.getFamilyId())));
//                uiCommandBuilder.set("#Description.TextSpans", Message.raw(FishDataLeft.getDescription()));
//            }
//            else
//            {
//                uiCommandBuilder.set("#Header.TextSpans", Message.raw(TextUtils.seededScrambleText(FishDataLeft.getName())));
//                uiCommandBuilder.set("#CountNumber.TextSpans", Message.raw("Not found"));
//                uiCommandBuilder.set("#Family.TextSpans", Message.raw(TextUtils.seededScrambleText(FishDataLeft.getFamilyId())));
//                uiCommandBuilder.set("#Description.TextSpans", Message.raw(TextUtils.seededScrambleText(FishDataLeft.getDescription())));
//            }
        }

        // Right page
        if(FishDataRight !=null)
        {
            if(AnglersAlmanac.getInstance().database.hasPlayerCaught(PlayerUUID, FishDataRight.getId()))
            {
                int fishCount = Stats.getFishCount(FishDataRight.getId());
                String cleanName = FishDataRight.getItemID()
                        .replace("Fish_", "")
                        .replace("_Item", "");

                String Image = "UI/Custom/Almanac/Fish/Assets/" + cleanName + ".png";
                String RarityFile = StampUtil.getStamp(cleanName,FishDataRight.getRarity());
                if(RarityFile != null)
                {
                    String RarityPath = "UI/Custom/Almanac/Fish/Stamps/"+FishDataRight.getRarity()+"/"+RarityFile+".png";
                    uiCommandBuilder.set("#StampImage2.AssetPath", RarityPath);

                }
                uiCommandBuilder.set("#FishImage2.AssetPath", Image);
                uiCommandBuilder.set("#Header2.TextSpans", Message.raw(FishDataRight.getName()));
                uiCommandBuilder.set("#CountNumber2.TextSpans", Message.raw(String.valueOf(fishCount)));
                uiCommandBuilder.set("#Family2.TextSpans", Message.raw(TextUtils.formatDisplayName(FishDataRight.getFamilyId())));
                uiCommandBuilder.set("#Description2.TextSpans", Message.raw(FishDataRight.getDescription()));
            }
            else
            {
                uiCommandBuilder.set("#Header2.TextSpans", Message.raw(TextUtils.seededScrambleText(FishDataRight.getName())));
                uiCommandBuilder.set("#CountNumber2.TextSpans", Message.raw("Not Found"));
                uiCommandBuilder.set("#Family2.TextSpans", Message.raw(TextUtils.seededScrambleText(FishDataRight.getFamilyId())));
                uiCommandBuilder.set("#Description2.TextSpans", Message.raw(TextUtils.seededScrambleText(FishDataRight.getDescription())));
            }
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull StatUiPage.AlmanacGuiData data) {
        super.handleDataEvent(ref, store, data);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || data.getButton() == null) return;
        if(data.getButton().equals("PrevPage"))
        {
            OpenPage(player,(Page-1), PlayerUUID,PlayerName);
        }
        if (data.getButton().equals("NextPage")) {
            int newPage = BookPageManager.getNextPage(Page);
            if(newPage == Page)
            {
                return;
            }
            OpenPage(player,newPage, PlayerUUID,PlayerName);
        }
    }

}
