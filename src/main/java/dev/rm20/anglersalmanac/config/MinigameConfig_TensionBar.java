package dev.rm20.anglersalmanac.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;

import java.util.HashMap;

public class MinigameConfig_TensionBar {
    public static final String KEY = "MinigameConfig_TensionBar";
    public static final BuilderCodec<MinigameConfig_TensionBar> CODEC;
    // Config:
    public float maxHookTime = 6f; // The longest that it can take to hook a fish in seconds.
    public float fishEscapeRate = 0.3333f; // The progress lost per-second that the fish is not in the catch bar.
    public float fishReelRate = 0.2f; // The progress gain per second when the fish is inside catch bar.
    public float barRadius = 0.1f; // The size of half the bar, used to check if bar is over the fish.
    public float fishMaxVeocity = 0.8f; // The maximum speed of the fish.
    public double minigameModelVerticalOffset = 1f; // The height above bobber to display the minigame elements.
    public float minigameScaleMin = 1.5f; // Minigame display minimum size.
    public float minigameScaleMax = 14f; // Minigame display maximum size.
    public float minigameScaleMultiplier = 1f; // minigameScale = distance from player * minigame scale multiplier.
    public float castCooldown = 0.5f; // Seconds before rod can be cast or reeled.
    public float barGravity = 0.7f; // How fast the bar falls when not being risen. Should be close to fish max velocity.
    public float barSpeed = 1.0f; // How fast the bar rises when right click is held. Should be faster than fish max velocity.
    public float barAcceleration = 0.4f; // The rate at which the bar accelerates multiplied by the speed of its direction.
    public double waterFriction = 0.85; // The amount by which the bobber model is slowed down while in water.

    // Builds the codec for plugin configuration.
    static {
        var codecBuilder = BuilderCodec.builder(MinigameConfig_TensionBar.class, MinigameConfig_TensionBar::new);

        // Add a new key and value to the config.
        codecBuilder.append(new KeyedCodec("MaxHookTime", new MapCodec(Codec.FLOAT, HashMap::new)),
                        (config, map) -> config.maxHookTime = map,
                        (config) -> config.maxHookTime).documentation("The longest that it can take to hook a fish in seconds.")
                .add();

        codecBuilder.append(new KeyedCodec("FishEscapeRate", Codec.FLOAT),
                        (config, value) -> config.fishEscapeRate = value,
                        (config) -> config.fishEscapeRate).documentation("The progress lost per-second that the fish is not in the catch bar.")
                .add();

        codecBuilder.append(new KeyedCodec("FishReelRate", Codec.FLOAT),
                        (config, value) -> config.fishReelRate = value,
                        (config) -> config.fishReelRate).documentation("The progress gain per second when the fish is inside catch bar.")
                .add();
        codecBuilder.append(new KeyedCodec("BarRadius", Codec.FLOAT),
                        (config, value) -> config.barRadius = value,
                        (config) -> config.barRadius).documentation("The size of half the bar, used to check if bar is over the fish.")
                .add();
        codecBuilder.append(new KeyedCodec("BishMaxVeocity", Codec.FLOAT),
                        (config, value) -> config.fishMaxVeocity = value,
                        (config) -> config.fishMaxVeocity).documentation("The maximum speed of the fish.")
                .add();
        codecBuilder.append(new KeyedCodec("MinigameModelVerticalOffset", Codec.DOUBLE),
                        (config, value) -> config.minigameModelVerticalOffset = value,
                        (config) -> config.minigameModelVerticalOffset).documentation("The height above bobber to display the minigame elements.")
                .add();
        codecBuilder.append(new KeyedCodec("MinigameScaleMin", Codec.FLOAT),
                        (config, value) -> config.minigameScaleMin = value,
                        (config) -> config.minigameScaleMin).documentation("Minigame display minimum size.")
                .add();
        codecBuilder.append(new KeyedCodec("MinigameScaleMax", Codec.FLOAT),
                        (config, value) -> config.minigameScaleMax = value,
                        (config) -> config.minigameScaleMax).documentation("Minigame display maximum size.")
                .add();
        codecBuilder.append(new KeyedCodec("MinigameScaleMultiplier", Codec.FLOAT),
                        (config, value) -> config.minigameScaleMultiplier = value,
                        (config) -> config.minigameScaleMultiplier).documentation("minigameScale = distance from player * minigame scale multiplier.")
                .add();
        codecBuilder.append(new KeyedCodec("CastCooldown", Codec.FLOAT),
                        (config, value) -> config.castCooldown = value,
                        (config) -> config.castCooldown).documentation("Seconds before rod can be cast or reeled.")
                .add();
        codecBuilder.append(new KeyedCodec("BarGravity", Codec.FLOAT),
                        (config, value) -> config.barGravity = value,
                        (config) -> config.barGravity).documentation("How fast the bar falls when not being risen. Should be close to fish max velocity.")
                .add();
        codecBuilder.append(new KeyedCodec("BarSpeed", Codec.FLOAT),
                        (config, value) -> config.barSpeed = value,
                        (config) -> config.barSpeed).documentation("How fast the bar rises when right click is held. Should be faster than fish max velocity.")
                .add();
        codecBuilder.append(new KeyedCodec("BarAcceleration", Codec.FLOAT),
                        (config, value) -> config.barAcceleration = value,
                        (config) -> config.barAcceleration).documentation("The rate at which the bar accelerates multiplied by the speed of its direction.")
                .add();
        codecBuilder.append(new KeyedCodec("WaterFriction", Codec.DOUBLE),
                        (config, value) -> config.waterFriction = value,
                        (config) -> config.waterFriction).documentation("The amount by which the bobber model is slowed down while in water.")
                .add();


        // Build and set the codec.
        CODEC = codecBuilder.build();
    }
    public static final KeyedCodec<MinigameConfig_TensionBar> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);

    public static MinigameConfig_TensionBar clone(){

    }
}
