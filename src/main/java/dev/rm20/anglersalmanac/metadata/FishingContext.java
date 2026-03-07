package dev.rm20.anglersalmanac.metadata;

import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.utils.Validator.TimePeriod;

public record FishingContext(
        TimePeriod time,
        int moonPhase,
        String zone,
        int tier,
        String region,
        String biome,
        double yPos,
        String weather,
        int waterDepth
) {

    //help with debug
    public void logContext() {
        AnglersAlmanac.getInstance().getLogger().atInfo().log(String.format(
                "--- Fishing Context Trace ---\n" +
                        "Location: [Zone: %s, Tier: %d, Region: %s, Biome: %s]\n" +
                        "Environment: [Time: %s, Weather: %s, Moon: %d]\n" +
                        "Position: [Y: %.2f, Water Depth: %d]\n" +
                        "-----------------------------",
                zone, tier, region, biome, time, weather, moonPhase, yPos, waterDepth
        ));
    }
}