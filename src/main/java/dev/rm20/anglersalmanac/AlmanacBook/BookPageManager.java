package dev.rm20.anglersalmanac.AlmanacBook;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.models.BookAssetData;
import dev.rm20.anglersalmanac.utils.FishLootManager;

import java.util.List;
import java.util.Objects;

public class BookPageManager {

    public static void OpenPage(Player player, int page, String playerUUID, String playerName)
    {
        AlmanacDatabase db = AnglersAlmanac.getInstance().database;
        AlmanacDatabase.PlayerStatsData stats = db.getPlayerStats(playerUUID);
        Ref<EntityStore> playerRef = player.getReference();
        PlayerRef playerRef1 = playerRef.getStore().getComponent(playerRef, PlayerRef.getComponentType());
        BookAssetData bookAsset = BookAssetData.getAssetStore().getAssetMap().getAssetMap().values().stream().toList().getFirst();

        if (bookAsset == null) return;
        PageManager pageManager = player.getPageManager();
        List<BookAssetData.SpreadTemplate> pages = bookAsset.getFlattenedPages();

        String UiFile = pages.get(page).uiFile;
        AnglersAlmanac.getInstance().getLogger().atInfo().log(pages.get(page).uiFile);
        if(UiFile.equalsIgnoreCase("Almanac/AlmanacStats.ui"))
        {
            StatUiPage statUiPage = new StatUiPage(playerRef1, playerUUID, playerName, stats);
            pageManager.openCustomPage(playerRef, playerRef.getStore(), statUiPage);
        }
        else if(pages.get(page).isDoublePage)
        {
            //TODO: 1 context 2 pages
            return;
        }
        else{
            FishLootManager FishDataLeft = FishLootManager.getFishData(pages.get(page).pages[0].contextData);
            FishLootManager FishDataRight =FishLootManager.getFishData(pages.get(page).pages[1].contextData);
            FishDataUiPage fishDataUiPage = new FishDataUiPage(playerRef1, playerUUID, playerName, stats,FishDataLeft ,FishDataRight);
            pageManager.openCustomPage(playerRef, playerRef.getStore(), fishDataUiPage);
        }

    }

}
