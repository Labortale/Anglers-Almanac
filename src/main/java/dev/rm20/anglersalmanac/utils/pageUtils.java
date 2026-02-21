package dev.rm20.anglersalmanac.utils;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import dev.rm20.anglersalmanac.AlmanacBook.AlmanacDatabase;
import dev.rm20.anglersalmanac.AlmanacBook.BookPageManager;
import dev.rm20.anglersalmanac.AlmanacBook.Pages.StatUiPage;
import dev.rm20.anglersalmanac.AnglersAlmanac;

import java.util.Objects;

public class pageUtils {
    public static void FillPage(UICommandBuilder ui, String slotPath, FishLootManager data, String PlayerUUID, AlmanacDatabase.PlayerStatsData Stats) {
        slotPath = slotPath+"[0] ";
        boolean caught = AnglersAlmanac.getInstance().database.hasPlayerCaught(PlayerUUID, data.getId());
        String cleanName = data.getItemID().replace("Fish_", "").replace("_Item", "");

        // Image

        if (caught) {
            int fishCount = Stats.getFishCount(data.getId());
            String imagePath = (data.getBookInfo() != null && data.getBookInfo().image_file != null)
                    ? data.getBookInfo().image_file
                    : "UI/Custom/Almanac/Fish/FishAssets/" + cleanName + ".png";

            // Habitat
            String habitat = (data.getBookInfo() != null) ? Objects.requireNonNullElse(data.getBookInfo().habitat_info, "") : "";
            if(habitat.isEmpty())
            {
                ui.set(slotPath + "#HabitatSection" + ".Visible", false);

            }
            else
            {

                String formattedHabitat = habitat.replace("|", "\n");
                ui.set(slotPath + "#HabitatList"  + ".Text", formattedHabitat);
            }

            // Stamp
            String rarityStamp = StampUtil.getStamp(cleanName, data.getRarity());
            if (rarityStamp != null) {
                ui.set(slotPath + "#StampImage" + ".AssetPath", "UI/Custom/Almanac/Fish/Stamps/" + data.getRarity() + "/" + rarityStamp + ".png");
            }

            // Text
            ui.set(slotPath + "#FishImage" + ".AssetPath", imagePath);
            ui.set(slotPath + "#Header" + ".TextSpans", Message.raw(data.getName()));
            ui.set(slotPath + "#CountNumber" + ".TextSpans", Message.raw(String.valueOf(fishCount)));
            ui.set(slotPath + "#Family" + ".TextSpans", Message.raw(TextUtils.formatDisplayName(data.getFamilyId())));
            ui.set(slotPath + "#Description" + ".TextSpans", Message.raw(data.getDescription()));

        } else {
            //Not caught
            String imagePath = (data.getBookInfo() != null && data.getBookInfo().image_file != null)
                    ? data.getBookInfo().image_file
                    : "UI/Custom/Almanac/Fish/FishAssets/" + cleanName + "_Missing.png";
            ui.set(slotPath + "#Header" + ".TextSpans", Message.raw(TextUtils.seededScrambleText(data.getName())));
            ui.set(slotPath + "#CountNumber" + ".TextSpans", Message.raw("Not found"));
            ui.set(slotPath + "#Family"  + ".TextSpans", Message.raw(TextUtils.seededScrambleText(data.getFamilyId())));
            ui.set(slotPath + "#Description"  + ".TextSpans", Message.raw(TextUtils.seededScrambleText(data.getDescription())));
            ui.set(slotPath + "#FishImage"  + ".AssetPath", imagePath);
            ui.set(slotPath + "#StampImage"  + ".Visible", false);
            ui.set(slotPath + "#HabitatSection" + ".Visible", false);
        }
    }


    public static void addDynamicNav(UICommandBuilder uiCommandBuilder, UIEventBuilder uiEventBuilder, int Page) {
        uiCommandBuilder.append("#BookNav", "Almanac/Utils/Nav.ui");
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#BookNav #NextPageButton",
                EventData.of(AlmanacGuiData.KEY_BUTTON, "NextPage"),
                false
        );

        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#BookNav #PrevPageButton",
                EventData.of(AlmanacGuiData.KEY_BUTTON, "PrevPage"),
                false
        );

        if (Page <= 0) {
            uiCommandBuilder.set("#BookNav #PrevPageButton.Visible", false);
            uiCommandBuilder.set("#BookNav #PrevPageButton.Disabled", true);
        }

        if(Page == BookPageManager.getNextPage(Page))
        {
            uiCommandBuilder.set("#BookNav #NextPageButton.Visible", false);
            uiCommandBuilder.set("#BookNav #NextPageButton.Disabled", true);
        }

    }


    public static class AlmanacGuiData {
        public static final String KEY_BUTTON = "Button";

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
}
