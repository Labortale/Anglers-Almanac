package dev.rm20.anglersalmanac.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.metadata.FishingContext;
import dev.rm20.anglersalmanac.metadata.ZoneInfo;
import dev.rm20.anglersalmanac.registration.CommandInfo;
import dev.rm20.anglersalmanac.utils.EnvironmentParser;
import dev.rm20.anglersalmanac.utils.TimeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@CommandInfo(
        name = "zoneinfo",
        description = "Gets info about current area, to be used for fishing"
)
public class HabitatCommand extends AbstractPlayerCommand {

    public HabitatCommand(String name, String description) {
        super(name, description);
    }

    @Nullable
    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        if (!(context.sender() instanceof Player player)) {
            context.sendMessage(Message.raw("This command can only be run by a player!"));
            return;
        }

        if (ref.isValid()) {
            // Position Info
            var transform = store.getComponent(ref, TransformComponent.getComponentType());
            if (transform == null) return;
            double y = transform.getPosition().getY();

            // Time & Moon Info
            WorldTimeResource timeResource = store.getResource(WorldTimeResource.getResourceType());
            String gameTime = timeResource.getGameTime().toString();
            String timeKeyword = TimeUtils.getTimeKeyword(gameTime);
            int moonPhase = timeResource.getMoonPhase();

            // Zone/Biome/Region Info
            WorldMapTracker worldMapTracker = player.getWorldMapTracker();
            WorldMapTracker.ZoneDiscoveryInfo currentZone = worldMapTracker.getCurrentZone();

            String rawZone = currentZone.regionName();
            String region = currentZone.zoneName();
            String biome = worldMapTracker.getCurrentBiomeName();
            ZoneInfo info = EnvironmentParser.parse(rawZone);
            String zone = info.zone();
            int tier = info.tier();

            FishingContext locationInfo = new FishingContext(
                    timeKeyword,
                    moonPhase,
                    zone,
                    tier,
                    region,
                    biome,
                    y,
                    "clear", // Hardcoded for test
                    0
            );

            // 5. Output
            context.sendMessage(Message.raw("--- Habitat info ---"));
            context.sendMessage(Message.raw("Time: " + locationInfo.time()));
            context.sendMessage(Message.raw("Moon Phase: " + locationInfo.moonPhase()));
            context.sendMessage(Message.raw("Zone: " + locationInfo.zone() + " (Tier " + locationInfo.tier() + ")"));
            context.sendMessage(Message.raw("Region/Biome: " + locationInfo.region() + " / " + locationInfo.biome()));
            context.sendMessage(Message.raw("Y-Level: " + locationInfo.yPos()));
            //logToConsole(locationInfo);
        }
    }

    private void logToConsole(FishingContext ctx) {
        AnglersAlmanac.getInstance().getLogger().atInfo().log();
    }
}
