package dev.rm20.anglersalmanac.utils;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.models.FishingContext;

import java.util.*;

public class FishLootManager implements JsonAssetWithMap<String, DefaultAssetMap<String, FishLootManager>> {

    // Codecs

    public static final BuilderCodec<Height> HEIGHT_CODEC = BuilderCodec.builder(Height.class, () -> new Height(0, -1))
            .append(new KeyedCodec<>("Min_y", Codec.INTEGER), (h, v) -> h.min_y = v, h -> h.min_y).add()
            .append(new KeyedCodec<>("Max_y", Codec.INTEGER), (h, v) -> h.max_y = v, h -> h.max_y).add()
            .build();


    public static final BuilderCodec<Habitats> HABITATS_CODEC = BuilderCodec.builder(Habitats.class, Habitats::new)
            .append(new KeyedCodec<>("Zones", new ArrayCodec<>(Codec.STRING, String[]::new)), (h, v) -> h.zones = v, h -> h.zones).add()
            .append(new KeyedCodec<>("Tiers", new ArrayCodec<>(Codec.INTEGER, Integer[]::new)), (h, v) -> h.tier = v, h -> h.tier).add()
            .append(new KeyedCodec<>("Regions", new ArrayCodec<>(Codec.STRING, String[]::new)), (h, v) -> h.regions = v, h -> h.regions).add()
            .append(new KeyedCodec<>("Biomes", new ArrayCodec<>(Codec.STRING, String[]::new)), (h, v) -> h.biomes = v, h -> h.biomes).add()
            .append(new KeyedCodec<>("Time_of_day", new ArrayCodec<>(Codec.STRING, String[]::new)), (h, v) -> h.time_of_day = v, h -> h.time_of_day).add()
            .append(new KeyedCodec<>("Required_weather", new ArrayCodec<>(Codec.STRING, String[]::new)), (h, v) -> h.required_weather = v, h -> h.required_weather).add()
            .append(new KeyedCodec<>("Moon_phase", Codec.INTEGER), (h, v) -> h.moon_phase = v, h -> h.moon_phase).add()
            .append(new KeyedCodec<>("Min_depth", Codec.INTEGER), (h, v) -> h.min_depth = v, h -> h.min_depth).add()
            .append(new KeyedCodec<>("Height", HEIGHT_CODEC), (h, v) -> h.height = v, h -> h.height).add()
            .append(new KeyedCodec<>("Exclude_zones", new ArrayCodec<>(Codec.STRING, String[]::new)), (h, v) -> h.exclude_zones = v, h -> h.exclude_zones).add()
            .append(new KeyedCodec<>("Exclude_tiers", new ArrayCodec<>(Codec.INTEGER, Integer[]::new)), (h, v) -> h.exclude_tiers = v, h -> h.exclude_tiers).add()
            .append(new KeyedCodec<>("Exclude_biomes", new ArrayCodec<>(Codec.STRING, String[]::new)), (h, v) -> h.exclude_biomes = v, h -> h.exclude_biomes).add()
            .append(new KeyedCodec<>("Exclude_regions", new ArrayCodec<>(Codec.STRING, String[]::new)), (h, v) -> h.exclude_regions = v, h -> h.exclude_regions).add()
            .append(new KeyedCodec<>("Weight_multiplier", Codec.FLOAT), (h, v) -> h.weight_multiplier = v, h -> h.weight_multiplier).add()
            .build();


    public static final BuilderCodec<MinigameStats> STATS_CODEC = BuilderCodec.builder(MinigameStats.class, MinigameStats::new)
            .append(new KeyedCodec<>("Difficulty", Codec.INTEGER), (s, v) -> s.difficulty = v, s -> s.difficulty).add()
            .append(new KeyedCodec<>("Behavior", Codec.STRING), (s, v) -> s.behavior = v, s -> s.behavior).add()
            .append(new KeyedCodec<>("Stamina", Codec.INTEGER), (s, v) -> s.stamina = v, s -> s.stamina).add()
            .build();


    public static final AssetBuilderCodec<String, FishLootManager> CODEC = AssetBuilderCodec.builder(
                    FishLootManager.class,
                    FishLootManager::new,
                    Codec.STRING,
                    (t, id) -> t.id = id,
                    t -> t.id,
                    (t, data) -> t.data = data,
                    t -> t.data
            )
            .appendInherited(new KeyedCodec<>("ItemId", Codec.STRING), (t, v) -> t.itemID = v, t -> t.itemID, (t, p) -> t.itemID = p.itemID).add()
            .appendInherited(new KeyedCodec<>("Name", Codec.STRING), (t, v) -> t.name = v, t -> t.name, (t, p) -> t.name = p.name).add()
            .appendInherited(new KeyedCodec<>("Description", Codec.STRING), (t, v) -> t.description = v, t -> t.description, (t, p) -> t.description = p.description).add()
            .appendInherited(new KeyedCodec<>("FamilyId", Codec.STRING), (t, v) -> t.familyId = v, t -> t.familyId, (t, p) -> t.familyId = p.familyId).add()
            .appendInherited(new KeyedCodec<>("Rarity", Codec.STRING), (t, v) -> t.rarity = v, t -> t.rarity, (t, p) -> t.rarity = p.rarity).add()
            .appendInherited(new KeyedCodec<>("Weight", Codec.INTEGER), (t, v) -> t.weight = v, t -> t.weight, (t, p) -> t.weight = p.weight).add()
            .appendInherited(new KeyedCodec<>("Size", Codec.INTEGER), (t, v) -> t.size = v, t -> t.size, (t, p) -> t.size = p.size).add()
            .appendInherited(new KeyedCodec<>("IsGlobal", Codec.BOOLEAN), (t, v) -> t.isGlobal = v, t -> t.isGlobal, (t, p) -> t.isGlobal = p.isGlobal).add()
            .appendInherited(new KeyedCodec<>("Habitats", HABITATS_CODEC), (t, v) -> t.habitats = v, t -> t.habitats, (t, p) -> t.habitats = p.habitats).add()
            .appendInherited(new KeyedCodec<>("Minigame_stats", STATS_CODEC), (t, v) -> t.minigameStats = v, t -> t.minigameStats, (t, p) -> t.minigameStats = p.minigameStats).add()
            .build();

    private static AssetStore<String, FishLootManager, DefaultAssetMap<String, FishLootManager>> ASSET_STORE;

    public static AssetStore<String, FishLootManager, DefaultAssetMap<String, FishLootManager>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(FishLootManager.class);
        }
        return ASSET_STORE;
    }

    // Fields

    private String id;
    private AssetExtraInfo.Data data;

    private String itemID;
    private String name;
    private String description;
    private String familyId;
    private String rarity;
    private int weight;
    private int size;
    private boolean isGlobal;
    private Habitats habitats;
    private MinigameStats minigameStats;

    public FishLootManager() {}

    @Override
    public String getId() { return id; }
    public String getItemID() { return itemID; }
    public Habitats getHabitats() { return habitats; }
    public int getWeight() { return weight; }
    public boolean isGlobal() { return isGlobal; }
    public String getName() {return name;}
    // Classes used by BuilderCodec

    public static class Habitats {
        public String[] zones = new String[0];
        public Integer[] tier = new Integer[0];
        public String[] regions = new String[0];
        public String[] biomes = new String[0];
        public String[] time_of_day = new String[0];
        public String[] required_weather = new String[0];
        public int moon_phase = -1;
        public int min_depth = 0;
        public Height height = new Height(0, -1);
        public String[] exclude_zones = new String[0];
        public Integer[] exclude_tiers = new Integer[0];
        public String[] exclude_biomes = new String[0];
        public String[] exclude_regions = new String[0];
        public float weight_multiplier = 0.0f;
    }

    public static class Height {
        public int min_y;
        public int max_y;
        public Height(int min, int max) { this.min_y = min; this.max_y = max; }
    }

    public static class MinigameStats {
        public int difficulty;
        public String behavior = "normal";
        public int stamina;
    }

    // Reward logic
    public static Collection<FishLootManager> getAllLoot() {
        return getAssetStore().getAssetMap().getAssetMap().values();
    }

    public static FishLootManager getRandomWeightedLoot(FishingContext ctx) {
        List<FishLootManager> possibleLoot = getAllLoot().stream()
                .filter(loot -> isEligible(loot, ctx))
                .filter(loot -> loot.getExclusionWeight(loot,ctx) > 0)
                .toList();

        if (possibleLoot.isEmpty()) {
            AnglersAlmanac.getInstance().getLogger().atInfo().log("No eligible fish found for this context!");
            return null;
        }

//        int totalWeight = possibleLoot.stream().mapToInt(FishLootManager::getWeight).sum();
        int totalWeight = possibleLoot.stream().mapToInt(l -> l.getExclusionWeight(l,ctx)).sum();
        if (totalWeight <= 0) return possibleLoot.getFirst();

        int randomIndex = new Random().nextInt(totalWeight);
        int currentSum = 0;

        for (FishLootManager entry : possibleLoot) {
            currentSum += entry.getExclusionWeight(entry,ctx);
            if (randomIndex < currentSum) {
                return entry;
            }
        }
        return possibleLoot.get(0);
    }

    private static boolean isEligible(FishLootManager loot, FishingContext ctx) {
        Habitats hab = loot.getHabitats();
        if (hab == null) return true;

        if (!checkEnvironment(hab, ctx)) return false;
        if (loot.isGlobal()) return true;
        boolean hasRequirement = false;
        boolean matchedAny = false;

        if (hab.biomes != null && hab.biomes.length > 0) {
            hasRequirement = true;
            if (Arrays.asList(hab.biomes).contains(ctx.biome())) matchedAny = true;
        }

        if (!matchedAny && hab.regions != null && hab.regions.length > 0) {
            hasRequirement = true;
            if (Arrays.asList(hab.regions).contains(ctx.region())) matchedAny = true;
        }


        if (!matchedAny && hab.zones != null && hab.zones.length > 0) {
            hasRequirement = true;
            if (Arrays.asList(hab.zones).contains(ctx.zone())) {
                // If zone matches, still respect the tier requirement if it exists
                if (hab.tier == null || hab.tier.length == 0 || Arrays.stream(hab.tier).anyMatch(t -> t != null && t == ctx.tier())) {
                    matchedAny = true;
                }
            }
        }

        return !hasRequirement || matchedAny;
    }

    private static boolean checkEnvironment(Habitats hab, FishingContext ctx) {
        if (hab.time_of_day != null && hab.time_of_day.length > 0) {
            boolean match = Arrays.stream(hab.time_of_day)
                    .anyMatch(t -> t != null && (t.equalsIgnoreCase("any") || t.equalsIgnoreCase(ctx.time())));
            if (!match) return false;
        }

        if (hab.required_weather != null && hab.required_weather.length > 0) {
            boolean match = Arrays.stream(hab.required_weather)
                    .anyMatch(w -> w != null && (w.equalsIgnoreCase("any") || w.equalsIgnoreCase(ctx.weather())));
            if (!match) return false;
        }

        if (hab.moon_phase != -1 && hab.moon_phase != ctx.moonPhase()) return false;
        if (ctx.waterDepth() < hab.min_depth) return false;
        if (hab.height != null) {
            if (ctx.yPos() < hab.height.min_y) return false;
            if (hab.height.max_y != -1 && ctx.yPos() > hab.height.max_y) return false;
        }

        return true;
    }

    public int getExclusionWeight(FishLootManager loot,FishingContext ctx) {
        if (loot.habitats == null) return this.weight;

        boolean isExcluded = false;

        // Check Biome
        if (loot.habitats.exclude_biomes != null && Arrays.asList(loot.habitats.exclude_biomes).contains(ctx.biome())) {
            isExcluded = true;
        }
        // Check Region
        else if (loot.habitats.exclude_regions != null && Arrays.asList(loot.habitats.exclude_regions).contains(ctx.region())) {
            isExcluded = true;
        }
        // Check Zone
        else if (loot.habitats.exclude_zones != null && Arrays.asList(loot.habitats.exclude_zones).contains(ctx.zone())) {
            isExcluded = true;
        }
        // Check Tier
        else if (loot.habitats.exclude_tiers != null && Arrays.stream(loot.habitats.exclude_tiers).anyMatch(t -> t != null && t == ctx.tier())){
            isExcluded = true;
        }

        if (isExcluded) {
            return Math.round(this.weight * loot.habitats.weight_multiplier);
        }

        return this.weight;
    }
}