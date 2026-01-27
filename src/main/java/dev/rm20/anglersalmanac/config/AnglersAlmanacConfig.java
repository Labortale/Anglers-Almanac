package dev.rm20.anglersalmanac.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import dev.rm20.anglersalmanac.AnglersAlmanac;

import java.util.HashMap;

public class AnglersAlmanacConfig {
    public static final String KEY = "MinigameConfig";
    public static final BuilderCodec<AnglersAlmanacConfig> CODEC;

    public String minigameToUse = "TensionBar";

    // Builds the codec for plugin configuration.
    static {
        var codecBuilder = BuilderCodec.builder(AnglersAlmanacConfig.class, AnglersAlmanacConfig::new);

        // Add a new key and value to the config.
        codecBuilder.append(new KeyedCodec("MinigameToUse", new MapCodec(Codec.STRING, HashMap::new)),
                        (config, map) -> config.minigameToUse = map,
                        (config) -> config.minigameToUse).documentation("The longest that it can take to hook a fish in seconds.")
                .add();


        // Build and set the codec.
        CODEC = codecBuilder.build();
    }
    public static final KeyedCodec<AnglersAlmanacConfig> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);
}
