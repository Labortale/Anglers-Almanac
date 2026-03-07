package dev.rm20.anglersalmanac.Config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class AnglersAlmanacConfig {
    public static final String KEY = "MinigameConfig";

    public static final BuilderCodec<AnglersAlmanacConfig> CODEC = BuilderCodec.builder(AnglersAlmanacConfig.class, AnglersAlmanacConfig::new)
            .append(new KeyedCodec<>("MinigameToUse", Codec.STRING),
                    (config, value) -> config.minigameToUse = value,
                    (config) -> config.minigameToUse)
            .documentation("The name of the minigame logic to use for fishing.")
            .add()
            .build();

    public static final KeyedCodec<AnglersAlmanacConfig> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);

    private String minigameToUse = "TensionBar";

    public AnglersAlmanacConfig() {
    }

    // Getter
    public String getMinigameToUse() {
        return minigameToUse;
    }

    // Setter
    public void setMinigameToUse(String minigameToUse) {
        this.minigameToUse = minigameToUse;
    }
}