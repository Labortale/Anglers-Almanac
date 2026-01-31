package dev.rm20.anglersalmanac.Database;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.MinigameManager.SkillCheck.DialEventData;

import javax.annotation.Nonnull;


public class StatUiPage extends InteractiveCustomUIPage<DialEventData> {

    public StatUiPage(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, DialEventData.CODEC);
    }
    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Almanac/AlmanacStats.ui");
    }
}
