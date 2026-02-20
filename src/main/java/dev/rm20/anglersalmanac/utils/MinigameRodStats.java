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
import dev.rm20.anglersalmanac.MinigameManager.RodStats;
import dev.rm20.anglersalmanac.metadata.FishingRodData;

//TODO: Move to models
//@HytaleAsset(
//        path = "AnglersAlmanacRod"
//)

public class MinigameRodStats implements JsonAssetWithMap<String, DefaultAssetMap<String, MinigameRodStats>> {

    public String id;
    public AssetExtraInfo.Data data;
    public RodStats stats;

    public static final String KEY = "AA_MinigameRodStats";

    public static final AssetBuilderCodec<String, MinigameRodStats> CODEC = AssetBuilderCodec.builder(
            MinigameRodStats.class,
            MinigameRodStats::new,
            Codec.STRING,
            (t, id) -> t.id = id,
            t -> t.id,
            (t, data) -> t.data = data,
            t -> t.data
    )
        .append(RodStats.KEYED_CODEC, (s, v) -> s.stats = v, (g) -> g.stats).add()
    .build();

    public static final KeyedCodec<MinigameRodStats> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);

    private static AssetStore<String, MinigameRodStats, DefaultAssetMap<String, MinigameRodStats>> ASSET_STORE;

    public static AssetStore<String, MinigameRodStats, DefaultAssetMap<String, MinigameRodStats>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(MinigameRodStats.class);
        }
        return ASSET_STORE;
    }

    @Override
    public String getId() {
        return id;
    }

    public static RodStats getRodStatsFromRodId(String rodId){
        RodStats stats;
        stats = getAssetStore().getAssetMap().getAsset(rodId).stats;
        assert stats != null;
        return stats;
    }
}
