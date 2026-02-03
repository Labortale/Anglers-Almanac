package dev.rm20.anglersalmanac.AlmanacBook;

import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateTranslations;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

public class AlmanacBook {
    public static void registerAlmanacBase() {
        Item baseItem = Item.getAssetMap().getAsset("Almanac_Book");
        registerItemOnServer("fishing:angler_almanac", baseItem);
    }


    public static void updateAlmanacText(PlayerRef playerRef, String playerUuid, String playerName) {
        Map<String, String> translations = new HashMap<>();
        translations.put("almanac.book.name", playerName + "'s Angler's Almanac");
        translations.put("almanac.book.description", "Bound to: " + playerUuid);

        UpdateTranslations packet = new UpdateTranslations();
        packet.type = UpdateType.AddOrUpdate;
        packet.translations = translations;
        playerRef.getPacketHandler().writeNoCache(packet);
    }

    private static void registerItemOnServer(String customId, Item baseItem) {
        try {
            AssetMap<String, Item> assetMap = Item.getAssetMap();
            Field mapField = assetMap.getClass().getDeclaredField("assetMap");
            Field lockField = assetMap.getClass().getDeclaredField("assetMapLock");

            mapField.setAccessible(true);
            lockField.setAccessible(true);
            Map<String, Item> internalMap = (Map<String, Item>) mapField.get(assetMap);
            StampedLock lock = (StampedLock) lockField.get(assetMap);
            if (internalMap.containsKey(customId)) return;

            // Thread-safe injection
            long stamp = lock.writeLock();
            try {
                internalMap.put(customId, baseItem);
            } finally {
                lock.unlockWrite(stamp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
