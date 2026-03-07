package dev.rm20.anglersalmanac.registration;

import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.interactions.LaunchBobberInteraction;
import dev.rm20.anglersalmanac.interactions.MinigameInteraction;
import dev.rm20.anglersalmanac.interactions.OpenBookInteraction;

public class InteractionManager {
    public static void registerInteractions(AnglersAlmanac plugin) {
        plugin.getCodecRegistry(Interaction.CODEC).register("launch_bobber_interaction", LaunchBobberInteraction.class, LaunchBobberInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC).register("minigame_interaction", MinigameInteraction.class, MinigameInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC).register("open_almanac_interaction", OpenBookInteraction.class, OpenBookInteraction.CODEC);
    }
}
