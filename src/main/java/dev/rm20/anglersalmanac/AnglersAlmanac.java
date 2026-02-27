package dev.rm20.anglersalmanac;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.server.core.util.Config;
import dev.rm20.anglersalmanac.AlmanacBook.AlmanacDatabase;
import dev.rm20.anglersalmanac.components.BobberComponent;
import dev.rm20.anglersalmanac.config.AnglersAlmanacConfig;
import dev.rm20.anglersalmanac.config.MinigameConfig_TensionBar;
import dev.rm20.anglersalmanac.models.BookAssetData;
import dev.rm20.anglersalmanac.registration.*;
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
        LOGGER.atInfo().log("Setting up plugin " + this.getName()+":"+getManifest().getVersion().toString());
        RegisterManager.registerCommands(this);
        RegisterManager.registerEvents(this);
        AssetRegisterManager.registerAll(this);

        // Register FishLoot asset.
        AssetRegistry.register(HytaleAssetStore.builder(FishLootManager.class, new DefaultAssetMap<String, FishLootManager>())
                .setPath("AnglersAlmanac")
                .setCodec(FishLootManager.CODEC)
                .setKeyFunction(FishLootManager::getId)
                .build()
        );

        // Register MinigameRodStats asset.
        AssetRegistry.register(HytaleAssetStore.builder(MinigameRodStats.class, new DefaultAssetMap<String, MinigameRodStats>())
                .setPath("AnglersAlmanacRod")
                .setCodec(MinigameRodStats.CODEC)
                .setKeyFunction(MinigameRodStats::getId)
                .build()
        );


        // Register Components
        ComponentManager.registerComponent(this);
        // Register Interaction Codecs
        InteractionManager.registerInteractions(this);

        //System Interaction
        SystemRegisteration.registerSystem(this);


        //start database
        this.database = new AlmanacDatabase();
        MOD_CONFIG.save();
        MINIGAME_CONFIG_TENSIONBAR.save();

        //AlmanacBook.registerAlmanacBase();

        // Plugin Mod Analytics
        new HStats("55078602-d7a1-4794-b30c-f42529f3d1d4", getManifest().getVersion().toString());
    }


    @Override
    protected void shutdown() {
        super.shutdown();
        if (this.database != null) {
            this.database.close();
        }
    }


}
