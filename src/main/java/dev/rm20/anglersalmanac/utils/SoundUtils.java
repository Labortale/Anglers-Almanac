package dev.rm20.anglersalmanac.utils;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateSoundEvents;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.rm20.anglersalmanac.AnglersAlmanac;

import java.util.HashMap;
import java.util.Map;

public class SoundUtils {

    public static void changeSoundAssetVolume(PlayerRef playerRef, String soundId, float newVolume) {

        try {
            IndexedLookupTableAssetMap<String, SoundEvent> assetMap = SoundEvent.getAssetMap();
            SoundEvent originalSound = assetMap.getAsset(soundId);
            if (originalSound == null) {
                AnglersAlmanac.LOGGER.atWarning().log("SoundID not found: " + soundId);
                return;
            }

            com.hypixel.hytale.protocol.SoundEvent modifiedSound = originalSound.toPacket();
            modifiedSound.volume = newVolume;
            int soundIndex = assetMap.getIndex(soundId);
            Map<Integer, com.hypixel.hytale.protocol.SoundEvent> updates = new HashMap();
            updates.put(soundIndex, modifiedSound);
            UpdateSoundEvents packet = new UpdateSoundEvents(UpdateType.AddOrUpdate, assetMap.getNextIndex(), updates);
            playerRef.getPacketHandler().write(packet);
            AnglersAlmanac.LOGGER.atInfo().log("Changing sound asset volume to %s", newVolume);
        } catch (Exception e) {
            AnglersAlmanac.LOGGER.atWarning().log("Changing sound volume failed: " + e.getMessage());
        }
    }
}
