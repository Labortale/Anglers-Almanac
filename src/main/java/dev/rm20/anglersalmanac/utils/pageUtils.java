package dev.rm20.anglersalmanac.utils;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import dev.rm20.anglersalmanac.AlmanacBook.AlmanacDatabase;
import dev.rm20.anglersalmanac.AnglersAlmanac;

import java.util.Objects;

public class pageUtils {
    public static void FillPage(UICommandBuilder ui, String slotPath, FishLootManager data, String PlayerUUID, AlmanacDatabase.PlayerStatsData Stats) {
        slotPath = slotPath+"[0] ";
        boolean caught = AnglersAlmanac.getInstance().database.hasPlayerCaught(PlayerUUID, data.getId());
        if (caught) {
            int fishCount = Stats.getFishCount(data.getId());
            String cleanName = data.getItemID().replace("Fish_", "").replace("_Item", "");

            // Image
            String imagePath = (data.getBookInfo() != null && data.getBookInfo().image_file != null)
                    ? data.getBookInfo().image_file
                    : "UI/Custom/Almanac/Fish/FishAssets/" + cleanName + ".png";

            // Habitat
            String habitat = (data.getBookInfo() != null) ? Objects.requireNonNullElse(data.getBookInfo().habitat_info, "") : "";
            if(habitat.isEmpty())
            {
                ui.set(slotPath + "#HabitatSection" + ".Visible", false);

            }
            ui.set(slotPath + "#HabitatList"  + ".Text", habitat);

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
            ui.set(slotPath + "#Header" + ".TextSpans", Message.raw(TextUtils.seededScrambleText(data.getName())));
            ui.set(slotPath + "#CountNumber" + ".TextSpans", Message.raw("Not found"));
            ui.set(slotPath + "#Family"  + ".TextSpans", Message.raw(TextUtils.seededScrambleText(data.getFamilyId())));
            ui.set(slotPath + "#Description"  + ".TextSpans", Message.raw(TextUtils.seededScrambleText(data.getDescription())));
            ui.set(slotPath + "#FishImage"  + ".AssetPath", "UI/Custom/Almanac/Fish/Assets/unknown_silhouette.png");
            ui.set(slotPath + "#StampImage"  + ".Visible", "false");
        }
    }
}
