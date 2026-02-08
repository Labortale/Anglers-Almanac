package dev.rm20.anglersalmanac.models;


import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;

public class AnglerDataComponent implements Component<EntityStore> {
    // Map of FishID -> CatchDetails
    public Map<String, CatchDetails> catches = new HashMap<>();
    public int totalCatches = 0;
    public static final BuilderCodec<AnglerDataComponent> CODEC = BuilderCodec.builder(AnglerDataComponent.class, AnglerDataComponent::new)
            .append(new KeyedCodec<>("Catches", new MapCodec<>(CatchDetails.CODEC, HashMap::new, false)),
                    (data, val) -> data.catches = val, data -> data.catches).add()
            .append(new KeyedCodec<>("TotalCatches", Codec.INTEGER),
                    (data, val) -> data.totalCatches = val, data -> data.totalCatches).add()
            .build();

    public static class CatchDetails {
        public int count = 0;
        public Set<String> discoveredHabitats = new HashSet<>();

        public static final BuilderCodec<CatchDetails> CODEC = BuilderCodec.builder(CatchDetails.class, CatchDetails::new)
                .append(new KeyedCodec<>("Count", Codec.INTEGER),
                        (d, v) -> d.count = v, d -> d.count).add()
                .append(new KeyedCodec<>("Habitats", new ArrayCodec<>(Codec.STRING, String[]::new)),
                        (d, v) -> d.discoveredHabitats = new HashSet<>(Arrays.asList(v)),
                        d -> d.discoveredHabitats.toArray(new String[0])).add()
                .build();
    }

    @Override
    public Component<EntityStore> clone() {
        AnglerDataComponent clone = new AnglerDataComponent();
        clone.catches = new HashMap<>(this.catches);
        clone.totalCatches = this.totalCatches;
        return clone;
    }
}