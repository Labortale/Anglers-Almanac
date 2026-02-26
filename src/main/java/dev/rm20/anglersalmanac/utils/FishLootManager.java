package dev.rm20.anglersalmanac.utils;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
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
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.metadata.FishingContext;
import dev.rm20.anglersalmanac.registration.HytaleAsset;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

//TODO: Move to models
//@HytaleAsset(
//        path = "AnglersAlmanac"
//)

public class FishLootManager implements JsonAssetWithMap<String, DefaultAssetMap<String, FishLootManager>> {

    // Codecs

    public static final BuilderCodec<BookInfo> Book_CODEC = BuilderCodec.builder(BookInfo.class, BookInfo::new)
            .append(new KeyedCodec<>("Image_Path", Codec.STRING), (h, v) -> h.image_file = v, h -> h.image_file).add()
            .append(new KeyedCodec<>("Habitat_Info", Codec.STRING), (h, v) -> h.habitat_info = v, h -> h.habitat_info).add()
            .append(new KeyedCodec<>("Page_File_Ui", Codec.STRING), (h, v) -> h.PageFileUI = v, h -> h.PageFileUI).add()
            .build();

    public static final BuilderCodec<Quantity> Quantity_CODEC = BuilderCodec.builder(Quantity.class, Quantity::new)
            .append(new KeyedCodec<>("Min", Codec.INTEGER), (h, v) -> h.min_amount = v, h -> h.min_amount).add()
            .append(new KeyedCodec<>("Max", Codec.INTEGER), (h, v) -> h.max_amount = v, h -> h.max_amount).add()
            .build();

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
            .appendInherited(new KeyedCodec<>("Quantity", Quantity_CODEC), (t, v) -> t.quantity = v, t -> t.quantity, (t, p) -> t.quantity = p.quantity).add()
            .appendInherited(new KeyedCodec<>("IsGlobal", Codec.BOOLEAN), (t, v) -> t.isGlobal = v, t -> t.isGlobal, (t, p) -> t.isGlobal = p.isGlobal).add()
            .appendInherited(new KeyedCodec<>("Habitats", HABITATS_CODEC), (t, v) -> t.habitats = v, t -> t.habitats, (t, p) -> t.habitats = p.habitats).add()
            .appendInherited(new KeyedCodec<>("Minigame_stats", STATS_CODEC), (t, v) -> t.minigameStats = v, t -> t.minigameStats, (t, p) -> t.minigameStats = p.minigameStats).add()
            .appendInherited(new KeyedCodec<>("Book_info", Book_CODEC), (t, v) -> t.bookInfo = v, t -> t.bookInfo, (t, p) -> t.bookInfo = p.bookInfo).add()
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
    private Quantity quantity;
    private Habitats habitats;
    private MinigameStats minigameStats;
    private BookInfo bookInfo;

    public FishLootManager() {
    }

    @Override
    public String getId() {
        return id;
    }

    public String getItemID() {
        return itemID;
    }

    public Habitats getHabitats() {
        return habitats;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getFamilyId() {
        return familyId;
    }

    public String getRarity() {
        return rarity;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public BookInfo getBookInfo() {
        return bookInfo;
    }
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

        public Height(int min, int max) {
            this.min_y = min;
            this.max_y = max;
        }
    }

    public static class MinigameStats {
        public int difficulty;
        public String behavior = "normal";
        public int stamina;
    }

    public static class Quantity {
        public int min_amount;
        public int max_amount;
    }

    public static class BookInfo {
        public String image_file;
        public String habitat_info;
        public String PageFileUI;
    }

    // Reward logic
    public static Collection<FishLootManager> getAllLoot() {
        return getAssetStore().getAssetMap().getAssetMap().values();
    }


    //Cache system
    public record GeoKey(String biome, String region, String zone, int tier) {
    }

    private static final LoadingCache<GeoKey, List<FishLootManager>> geoLootCache = Caffeine.newBuilder().expireAfterAccess(15, TimeUnit.MINUTES).softValues().build(key -> {
        return getAllLoot().stream().filter(loot -> isEligible(loot, key)).toList();
    });

    public static void invalidateCache() {
        geoLootCache.invalidateAll();
    }

    public static FishLootManager getRandomWeightedLoot(FishingContext ctx) {
        GeoKey key = new GeoKey(ctx.biome(), ctx.region(), ctx.zone(), ctx.tier());
        List<FishLootManager> geoPossible = geoLootCache.get(key);

        List<FishLootManager> possibleLoot = new ArrayList<>();
        int totalWeight = 0;
        for (FishLootManager loot : geoPossible) {
            if (checkEnvironment(loot, ctx)) {
                int w = loot.getExclusionWeight(loot, ctx);
                if (w > 0) {
                    possibleLoot.add(loot);
                    totalWeight += w;
                }
            }
        }

        if (possibleLoot.isEmpty()) {
            AnglersAlmanac.getInstance().getLogger().atInfo().log("No eligible fish found for this context!");
            return null;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(totalWeight);
        int currentSum = 0;

        for (FishLootManager loot : possibleLoot) {
            currentSum += loot.getExclusionWeight(loot, ctx);
            if (randomIndex < currentSum) return loot;
        }
        return possibleLoot.get(0);
    }

    public static List<FishLootManager> getFishInArea(FishingContext ctx) {
        GeoKey key = new GeoKey(ctx.biome(), ctx.region(), ctx.zone(), ctx.tier());
        return geoLootCache.get(key);
    }

    public static FishLootManager getFishData(String id) {
        if (id == null) return null;

        return getAllLoot().stream().filter(loot -> loot.id.equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    private static boolean isEligible(FishLootManager loot, GeoKey key) {
        Habitats hab = loot.getHabitats();
        if (hab == null) {
            //AnglersAlmanac.getInstance().getLogger().atInfo().log(loot.getName() + " is has no habitat info");
            return true;
        }


        //Check exclude_biomes and checks if weight is 0 incase
        if (containsIgnoreCase(hab.exclude_biomes, key.biome()) && hab.weight_multiplier == 0) {
            return false;
        }

        if (containsIgnoreCase(hab.exclude_regions, key.region()) && hab.weight_multiplier == 0) {
            return false;
        }

        if (containsIgnoreCase(hab.exclude_zones, key.zone()) && hab.weight_multiplier == 0) {
            return false;
        }

        if (loot.isGlobal()) {
            //AnglersAlmanac.getInstance().getLogger().atInfo().log(loot.getName() + " is global fish");
            return true;
        }

        boolean hasRequirement = false;
        boolean matchedAny = false;
        if (hab.biomes != null && hab.biomes.length > 0) {
            hasRequirement = true;
            for (String b : hab.biomes) {
                if (b.equalsIgnoreCase(key.biome())) {
                    //AnglersAlmanac.getInstance().getLogger().atInfo().log(loot.getName() + "found at biome: "+ key.biome());
                    matchedAny = true;
                    break;
                }
            }
        }

        if (!matchedAny && hab.regions != null && hab.regions.length > 0) {
            hasRequirement = true;
            for (String r : hab.regions) {
                if (r.equalsIgnoreCase(key.region())) {
                    //AnglersAlmanac.getInstance().getLogger().atInfo().log(loot.getName() + "found at region: "+ key.region());
                    matchedAny = true;
                    break;
                }
            }
        }

        if (!matchedAny && hab.zones != null && hab.zones.length > 0) {
            hasRequirement = true;
            for (String z : hab.zones) {
                if (z.equalsIgnoreCase(key.zone())) {
                    // If zone matches, still respect the tier requirement if it exists
                    if (hab.tier == null || hab.tier.length == 0 || Arrays.stream(hab.tier).anyMatch(t -> t == key.tier())) {
                        //AnglersAlmanac.getInstance().getLogger().atInfo().log(loot.getName() + "found at zone and tier: "+ key.zone() + " : "+ key.tier());
                        matchedAny = true;
                    }
                    break;
                }
            }
        }
        return !hasRequirement || matchedAny;
    }

    private static boolean checkEnvironment(FishLootManager loot, FishingContext ctx) {
        Habitats hab = loot.getHabitats();
        if (hab == null) return true;
        // Time of day
        if (hab.time_of_day != null && hab.time_of_day.length > 0) {
            boolean match = Arrays.stream(hab.time_of_day).anyMatch(t -> t != null && (t.equalsIgnoreCase("any") || t.equalsIgnoreCase(ctx.time())));
            if (!match) return false;
        }
        // Weather
        if (hab.required_weather != null && hab.required_weather.length > 0) {
            boolean match = Arrays.stream(hab.required_weather).anyMatch(w -> w != null && (w.equalsIgnoreCase("any") || w.equalsIgnoreCase(ctx.weather())));
            if (!match) return false;
        }

        // Moon phase
        if (hab.moon_phase != -1 && hab.moon_phase != ctx.moonPhase()) return false;
        // Water
        if (ctx.waterDepth() < hab.min_depth) return false;
        // Y level
        if (hab.height != null) {
            if (ctx.yPos() < hab.height.min_y) return false;
            return hab.height.max_y == -1 || !(ctx.yPos() > hab.height.max_y);
        }

        return true;
    }

    public int getExclusionWeight(FishLootManager loot, FishingContext ctx) {
        if (loot.habitats == null) return this.weight;

        boolean isExcluded = containsIgnoreCase(loot.habitats.exclude_biomes, ctx.biome()) || containsIgnoreCase(loot.habitats.exclude_regions, ctx.region()) || containsIgnoreCase(loot.habitats.exclude_zones, ctx.zone()) || Arrays.asList(loot.habitats.exclude_tiers).contains(ctx.tier());

        if (isExcluded) {
            return Math.round(this.weight * loot.habitats.weight_multiplier);
        }

        return this.weight;
    }

    public MinigameStats getMinigameStats() {
        return minigameStats;
    }


    private static boolean containsIgnoreCase(String[] array, String value) {
        if (array == null || value == null) return false;
        for (String s : array) {
            if (value.equalsIgnoreCase(s)) return true;
        }
        return false;
    }

    public static int getRarityWeight(String fishId) {
        var data = FishLootManager.getFishData(fishId);
        if (data == null) return 99;

        return switch (data.getRarity().toLowerCase()) {
            case "common" -> 0;
            case "uncommon" -> 1;
            case "rare" -> 2;
            case "epic" -> 3;
            case "legendary" -> 4;
            default -> 99; // Default weight for unknown rarities
        };
    }

    public static String getRarityColour(String Rarity) {
        if (Rarity == null) return "#5A5B5B";

        return switch (Rarity.toLowerCase()) {
            case "common" -> "#5A5B5B";
            case "uncommon" -> "#005205";
            case "rare" -> "#5481EB";
            case "epic" -> "#5C337B";
            case "legendary" -> "#FFDB4B";
            default -> "#5A5B5B"; // Default weight for unknown rarities
        };
    }
}