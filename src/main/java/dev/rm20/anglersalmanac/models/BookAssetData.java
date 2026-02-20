package dev.rm20.anglersalmanac.models;

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
import dev.rm20.anglersalmanac.registration.HytaleAsset;
import dev.rm20.anglersalmanac.utils.FishLootManager;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.rm20.anglersalmanac.utils.FishLootManager.getRarityWeight;


@HytaleAsset(
        path = "AnglersAlmanacBook"
)
public class BookAssetData implements JsonAssetWithMap<String, DefaultAssetMap<String, BookAssetData>> {

    public static final BuilderCodec<ZoneInfo> ZONE_INFO_CODEC = BuilderCodec.builder(ZoneInfo.class, ZoneInfo::new)
            .append(new KeyedCodec<>("ZoneDescription", Codec.STRING), (z, v) -> z.zoneDescription = v, z -> z.zoneDescription).add()
            .append(new KeyedCodec<>("ZoneImage", Codec.STRING), (z, v) -> z.ZoneImage = v, z -> z.ZoneImage).add()
            .append(new KeyedCodec<>("ProgressBarImage", Codec.STRING), (z, v) -> z.ProgressBarImage = v, z -> z.ProgressBarImage).add()
            .append(new KeyedCodec<>("ProgressBarColour", Codec.STRING), (z, v) -> z.ProgressBarColour = v, z -> z.ProgressBarColour).add()
            .build();

    public static final BuilderCodec<SpreadTemplate> SPREAD_CODEC = BuilderCodec.builder(SpreadTemplate.class, SpreadTemplate::new)
            .append(new KeyedCodec<>("UiFile", Codec.STRING), (s, v) -> s.uiFile = v, s -> s.uiFile).add()
            .append(new KeyedCodec<>("IsDoublePage", Codec.BOOLEAN), (s, v) -> s.isDoublePage = v, s -> s.isDoublePage).add()
            .append(new KeyedCodec<>("LeftPage", Codec.STRING), (s, v) -> s.LeftPage = v, s -> s.LeftPage).add()
            .append(new KeyedCodec<>("RightPage", Codec.STRING), (s, v) -> s.RightPage = v, s -> s.RightPage).add()
            .build();

    public static final BuilderCodec<habitatsInfo> HABITAT_INFO_CODEC = BuilderCodec.builder(habitatsInfo.class, habitatsInfo::new)
            .append(new KeyedCodec<>("ZoneName", Codec.STRING), (h, v) -> h.ZoneName = v, h -> h.ZoneName).add()
            .append(new KeyedCodec<>("ZoneInfo", ZONE_INFO_CODEC), (c, v) -> c.zoneInfo = v, c -> c.zoneInfo).add()
            .append(new KeyedCodec<>("Spread", new ArrayCodec<>(SPREAD_CODEC, SpreadTemplate[]::new)), (h, v) -> h.pages = v, h -> h.pages).add()
            .build();


    public static final AssetBuilderCodec<String, BookAssetData> CODEC = AssetBuilderCodec.builder(
                    BookAssetData.class,
                    BookAssetData::new,
                    Codec.STRING,
                    (t, id) -> t.id = id,
                    t -> t.id,
                    (t, data) -> t.data = data,
                    t -> t.data
            )
            .appendInherited(new KeyedCodec<>("Habitats", new ArrayCodec<>(HABITAT_INFO_CODEC, habitatsInfo[]::new)),
                    (t, v) -> t.habitats = v,
                    t -> t.habitats,
                    (t, p) -> t.habitats = p.habitats).add()
            .build();

    // Asset Store

    private static AssetStore<String, BookAssetData, DefaultAssetMap<String, BookAssetData>> ASSET_STORE;

    public static AssetStore<String, BookAssetData, DefaultAssetMap<String, BookAssetData>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(BookAssetData.class);
        }
        return ASSET_STORE;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Fields
    private String id;
    private AssetExtraInfo.Data data;
    private habitatsInfo[] habitats;

    public static class habitatsInfo {
        public String ZoneName;
        public ZoneInfo zoneInfo;
        public SpreadTemplate[] pages;
    }

    public static class SpreadTemplate {
        public String uiFile;
        public boolean isDoublePage;
        public String LeftPage;
        public String RightPage;
    }

    public static class ZoneInfo {
        public String zoneDescription;
        public String ZoneImage;
        public String ProgressBarImage;
        public String ProgressBarColour;
    }

    public BookAssetData() {
    }

    @Override
    public String getId() {
        return id;
    }

    public habitatsInfo[] getHabitats() {
        return habitats;
    }

    public List<SpreadTemplate> getFlattenedPages() {
        if (habitats == null) return List.of();

        return Arrays.stream(habitats)
                .filter(habitat -> habitat.pages != null)
                .flatMap(habitat -> Arrays.stream(habitat.pages))
                .toList();
    }


    public record FishEntry(String id, boolean isItem) {
    }

    private Map<String, List<FishEntry>> habitatCache;

    public List<FishEntry> getFishByHabitat(String habitatName) {
        //buildCache();
        if (habitatCache == null) {
            buildCache();
        }
        //AnglersAlmanac.getInstance().getLogger().atInfo().log(habitatCache.toString());
        return habitatCache.getOrDefault(habitatName.toLowerCase(), List.of());
    }

    private void buildCache() {
        if (habitats == null) return;
        habitatCache = new LinkedHashMap<>();

        for (habitatsInfo habitat : habitats) {
            if (habitat == null || habitat.ZoneName == null) continue;

            List<FishEntry> fishList = Arrays.stream(habitat.pages != null ? habitat.pages : new SpreadTemplate[0])
                    .flatMap(spread -> Stream.of(spread.LeftPage, spread.RightPage))
                    .filter(Objects::nonNull)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .map(id -> new FishEntry(id, FishLootManager.getFishData(id) != null))
                    .collect(Collectors.toList());

            habitatCache.put(habitat.ZoneName.toLowerCase(), fishList);
        }
    }


    public record HabitatProgress(int caught, int total) {
        public float getPercentage() {
            return total == 0 ? 0 : (float) caught / total;
        }
    }

    public Map<String, HabitatProgress> getAllHabitatProgress(String playerUUID) {
        if (habitatCache == null) buildCache();

        Map<String, HabitatProgress> globalProgress = new HashMap<>();

        habitatCache.forEach((zoneName, fishList) -> {
            List<String> validItemIds = fishList.stream()
                    .filter(FishEntry::isItem)
                    .map(FishEntry::id)
                    .toList();
            long caughtCount = validItemIds.stream()
                    .filter(id -> AnglersAlmanac.getInstance().database.hasPlayerCaught(playerUUID, id))
                    .count();
            globalProgress.put(zoneName, new HabitatProgress((int) caughtCount, validItemIds.size()));
        });

        return globalProgress;
    }

    public static BookAssetData getMasterMergedBook() {
        BookAssetData master = new BookAssetData();
        master.id = "master_almanac_merged";
        Map<String, habitatsInfo> mergedMap = new LinkedHashMap<>();

        getAssetStore().getAssetMap().getAssetMap().values().forEach(book -> {
            if (book.getHabitats() == null) return;
            for (habitatsInfo habitat : book.getHabitats()) {
                String key = habitat.ZoneName.toLowerCase();
                if (!mergedMap.containsKey(key)) {
                    habitat.pages = mergePages(new SpreadTemplate[0], habitat.pages);
                    mergedMap.put(key, habitat);
                } else {
                    habitatsInfo existing = mergedMap.get(key);
                    existing.pages = mergePages(existing.pages, habitat.pages);
                }
            }
        });

        master.habitats = mergedMap.values().stream()
                .sorted(Comparator.comparingInt(h -> getZoneRank(h.ZoneName)))
                .toArray(habitatsInfo[]::new);

        master.buildCache();
        return master;
    }

    private static SpreadTemplate[] mergePages(SpreadTemplate[] existing, SpreadTemplate[] incoming) {
        Set<String> uniqueFishIds = new LinkedHashSet<>();
        List<SpreadTemplate> specialSpreads = new ArrayList<>();
        String standardFishUi = "Almanac/Fish/AlmanacFish.ui";
        String statsUi = "Almanac/AlmanacStats.ui";

        Stream.concat(Arrays.stream(existing), Arrays.stream(incoming)).forEach(spread -> {
            if (!standardFishUi.equals(spread.uiFile)) {
                boolean alreadyExists = specialSpreads.stream().anyMatch(s -> s.uiFile.equals(spread.uiFile));
                if (!alreadyExists) {
                    if (statsUi.equals(spread.uiFile)) specialSpreads.add(0, spread);
                    else specialSpreads.add(spread);
                }
                return;
            }
            if (spread.LeftPage != null && !spread.LeftPage.isEmpty()) uniqueFishIds.add(spread.LeftPage);
            if (spread.RightPage != null && !spread.RightPage.isEmpty()) uniqueFishIds.add(spread.RightPage);
        });

        List<String> sortedFish = uniqueFishIds.stream()
                .sorted(Comparator.comparingInt(FishLootManager::getRarityWeight))
                .toList();

        List<SpreadTemplate> result = new ArrayList<>(specialSpreads);
        for (int i = 0; i < sortedFish.size(); i += 2) {
            SpreadTemplate s = new SpreadTemplate();
            s.uiFile = standardFishUi;
            s.isDoublePage = false;
            s.LeftPage = sortedFish.get(i);
            s.RightPage = (i + 1 < sortedFish.size()) ? sortedFish.get(i + 1) : "";
            result.add(s);
        }

        return result.toArray(new SpreadTemplate[0]);
    }

    private static int getZoneRank(String name) {
        return switch (name.toLowerCase()) {
            case "almanacstats" -> 0;
            case "global" -> 1;
            case "ocean" -> 2;
            default -> 99;
        };
    }


}
