package dev.rm20.anglersalmanac;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.server.core.util.Config;
import dev.rm20.anglersalmanac.AlmanacBook.AlmanacBook;
import dev.rm20.anglersalmanac.AlmanacBook.AlmanacDatabase;
import dev.rm20.anglersalmanac.components.AudioPlayerComponent;
import dev.rm20.anglersalmanac.components.BobberComponent;
import dev.rm20.anglersalmanac.components.MinigameComponent_TensionBar;
import dev.rm20.anglersalmanac.components.PhysicsComponent;
import dev.rm20.anglersalmanac.config.AnglersAlmanacConfig;
import dev.rm20.anglersalmanac.config.MinigameConfig_TensionBar;
import dev.rm20.anglersalmanac.interactions.LaunchBobberInteraction;
import dev.rm20.anglersalmanac.interactions.MinigameInteraction;
import dev.rm20.anglersalmanac.interactions.OpenBookInteraction;
import dev.rm20.anglersalmanac.models.BookAssetData;
import dev.rm20.anglersalmanac.registration.RegisterManager;
import dev.rm20.anglersalmanac.registration.SystemRegisteration;
import dev.rm20.anglersalmanac.utils.FishLootManager;
import dev.rm20.anglersalmanac.utils.MinigameRodStats;


import javax.annotation.Nonnull;

public class AnglersAlmanac extends JavaPlugin {
    private static AnglersAlmanac instance;
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static ComponentType<EntityStore, BobberComponent> bobberComponent;

    public static Config<MinigameConfig_TensionBar> MINIGAME_CONFIG_TENSIONBAR;
    public static Config<AnglersAlmanacConfig> MOD_CONFIG;
    public AlmanacDatabase database;

    public FishLootManager fishLootManager;
    public BookAssetData bookAssetData;
    public AnglersAlmanac(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        MINIGAME_CONFIG_TENSIONBAR = this.withConfig(MinigameConfig_TensionBar.KEY, MinigameConfig_TensionBar.CODEC);
        MOD_CONFIG = this.withConfig(AnglersAlmanacConfig.KEY, AnglersAlmanacConfig.CODEC);
        //LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
    }
    public static AnglersAlmanac getInstance() {
        return instance;
    }


    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());
        RegisterManager.registerCommands(this);

        // Register FishLoot asset.
        AssetRegistry.register(HytaleAssetStore.builder(FishLootManager.class, new DefaultAssetMap<String, FishLootManager>())
                .setPath("AnglersAlmanac")
                .setCodec(FishLootManager.CODEC)
                .setKeyFunction(FishLootManager::getId)
                .build()
        );

        AssetRegistry.register(HytaleAssetStore.builder(BookAssetData.class, new DefaultAssetMap<String, BookAssetData>())
                .setPath("AnglersAlmanacBook")
                .setCodec(BookAssetData.CODEC)
                .setKeyFunction(BookAssetData::getId)
                .build()
        );

        // Register MinigameRodStats asset.
        AssetRegistry.register(HytaleAssetStore.builder(MinigameRodStats.class, new DefaultAssetMap<String, MinigameRodStats>())
                .setPath("AnglersAlmanac")
                .setCodec(MinigameRodStats.CODEC)
                .setKeyFunction(MinigameRodStats::getId)
                .build()
        );


        // Register Components
        bobberComponent = this.getEntityStoreRegistry().registerComponent(BobberComponent.class, BobberComponent::new);
        MinigameComponent_TensionBar.COMPONENT_TYPE = this.getEntityStoreRegistry().registerComponent(MinigameComponent_TensionBar.class, MinigameComponent_TensionBar::new);
        AudioPlayerComponent.COMPONENT_TYPE = this.getEntityStoreRegistry().registerComponent(AudioPlayerComponent.class, AudioPlayerComponent::new);
        ComponentType<EntityStore, PhysicsComponent> type = this.getEntityStoreRegistry().registerComponent(PhysicsComponent.class, PhysicsComponent::new);

        // Register Interaction Codecs
        this.getCodecRegistry(Interaction.CODEC).register("launch_bobber_interaction", LaunchBobberInteraction.class, LaunchBobberInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("minigame_interaction", MinigameInteraction.class, MinigameInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("open_almanac_interaction", OpenBookInteraction.class, OpenBookInteraction.CODEC);

        PhysicsComponent.setComponentType(type);
        SystemRegisteration.registerSystem(this);

        var fishLootManagerStore = FishLootManager.getAssetStore();
        if (fishLootManagerStore != null && fishLootManagerStore.getAssetMap() != null) {
            int fishCount = fishLootManagerStore.getAssetMap().getAssetCount();
            LOGGER.atInfo().log("FishLootManager registered. Currently " + fishCount + " assets in store (Assets load asynchronously).");
        } else {
            LOGGER.atInfo().log("FishLootManager registered via Builder. Assets will be loaded during the asset phase.");
        }

        var BookStore = BookAssetData.getAssetStore();
        if (BookStore != null && BookStore.getAssetMap() != null) {
            LOGGER.atInfo().log("BookAssetData registered");
        }


        //start database
        this.database = new AlmanacDatabase();
        MOD_CONFIG.save();
        MINIGAME_CONFIG_TENSIONBAR.save();

        //AlmanacBook.registerAlmanacBase();

    }


}
