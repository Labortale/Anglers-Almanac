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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class BookAssetData implements JsonAssetWithMap<String, DefaultAssetMap<String, BookAssetData>> {

    public static final BuilderCodec<PageContext> CONTEXT_CODEC = BuilderCodec.builder(PageContext.class, PageContext::new)
            .append(new KeyedCodec<>("ContextData", Codec.STRING), (c, v) -> c.contextData = v, c -> c.contextData).add()
            .build();

    public static final BuilderCodec<SpreadTemplate> SPREAD_CODEC = BuilderCodec.builder(SpreadTemplate.class, SpreadTemplate::new)
            .append(new KeyedCodec<>("UiFile", Codec.STRING), (s, v) -> s.uiFile = v, s -> s.uiFile).add()
            .append(new KeyedCodec<>("IsDoublePage", Codec.BOOLEAN), (s, v) -> s.isDoublePage = v, s -> s.isDoublePage).add()
            .append(new KeyedCodec<>("Pages", new ArrayCodec<>(CONTEXT_CODEC, PageContext[]::new)), (s, v) -> s.pages = v, s -> s.pages).add()
            .build();

    public static final BuilderCodec<habitatsInfo> HABITAT_INFO_CODEC = BuilderCodec.builder(habitatsInfo.class, habitatsInfo::new)
            .append(new KeyedCodec<>("ZoneName", Codec.STRING), (h, v) -> h.ZoneName = v, h -> h.ZoneName).add()
            .append(new KeyedCodec<>("Pages", new ArrayCodec<>(SPREAD_CODEC, SpreadTemplate[]::new)), (h, v) -> h.pages = v, h -> h.pages).add()
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

    // Fields
    private String id;
    private AssetExtraInfo.Data data;
    private habitatsInfo[] habitats;
    public static class habitatsInfo {
        public String ZoneName;
        public SpreadTemplate[] pages;
    }
    public static class SpreadTemplate {
        public String uiFile;
        public boolean isDoublePage;
        public PageContext[] pages = new PageContext[0];
    }

    public static class PageContext {
        public String contextData;
    }
    public BookAssetData() {}

    @Override public String getId() { return id; }

    public List<SpreadTemplate> getFlattenedPages() {
        List<SpreadTemplate> flattened = new ArrayList<>();
        if (habitats == null) return flattened;

        for (habitatsInfo habitat : habitats) {
            if (habitat.pages != null) {
                flattened.addAll(Arrays.asList(habitat.pages));
            }
        }
        return flattened;
    }

}
