package dev.rm20.anglersalmanac.MinigameManager;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import dev.rm20.anglersalmanac.AlmanacBook.BookPageManager;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Components.MinigameComponent_TensionBar;
import dev.rm20.anglersalmanac.Interactions.LaunchBobberInteraction;
import dev.rm20.anglersalmanac.Metadata.FishingContext;
import dev.rm20.anglersalmanac.Metadata.FishingRodData;
import dev.rm20.anglersalmanac.Metadata.ZoneInfo;
import dev.rm20.anglersalmanac.Models.FishLootManager;
import dev.rm20.anglersalmanac.Utils.EnvironmentParser;
import dev.rm20.anglersalmanac.Utils.TimeUtils;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import static com.hypixel.hytale.server.core.universe.world.WorldConfig.formatDisplayName;
import static dev.rm20.anglersalmanac.MinigameManager.Minigame.PerformanceRating.NIL;

public class MinigameManager {
    public static void StartGame(Ref<EntityStore> bobberRef, Player player, CommandBuffer<EntityStore> commandBuffer, int depth) {

        //Assuming active hotbar item has not changed.
        ItemStack fishingRod = player.getInventory().getActiveHotbarItem();


        // Select which minigame to use from the config and set it up.
        switch (AnglersAlmanac.MOD_CONFIG.get().getMinigameToUse()) {
            case "TensionBar":
                FishingRodData meta = fishingRod.getFromMetadataOrNull(FishingRodData.KEYED_CODEC);
                if (meta == null) {
                    LaunchBobberInteraction.cancelFishing(commandBuffer, player, fishingRod);
                    break;
                }
                Inventory inv = player.getInventory();
                MinigameComponent_TensionBar minigame = MinigameComponent_TensionBar.spawnMinigame(commandBuffer, player.getReference(), bobberRef, fishingRod.getItemId());
                LaunchBobberInteraction.updateMetadata(inv, inv.getActiveHotbarSlot(), inv.getActiveHotbarItem(), meta.getBoundBobber(), minigame.selfUUID, 1);
                break;
            case "NoMinigame":
                DropLoot(FirstRoll(bobberRef, player, commandBuffer, depth), player, commandBuffer, bobberRef, NIL);
                LaunchBobberInteraction.cancelFishing(commandBuffer, player, fishingRod);
                break;
            default: // No Minigame, just reel fish.
                DropLoot(FirstRoll(bobberRef, player, commandBuffer, depth), player, commandBuffer, bobberRef, NIL);
                LaunchBobberInteraction.cancelFishing(commandBuffer, player, fishingRod);
                break;
        }


    }

    public static void CancelGame(CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> minigameRef) {

        // Select which minigame to use from the config and cancel it.
        switch (AnglersAlmanac.MOD_CONFIG.get().getMinigameToUse()) {
            case "TensionBar":
                AnglersAlmanac.LOGGER.atInfo().log("Canceling TensionBar Minigame");
                commandBuffer.getComponent(minigameRef, MinigameComponent_TensionBar.COMPONENT_TYPE).despawnSelf(commandBuffer.getExternalData().getWorld());
                break;
            case "NoMinigame":
                break;
            default:
                break;
        }

    }


    public static void DoMinigameInteraction(CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> minigameRef, @NonNull InteractionType interactionType, @NonNull InteractionContext context, @NonNull CooldownHandler cooldownHandler) {
        switch (AnglersAlmanac.MOD_CONFIG.get().getMinigameToUse()) {
            case "TensionBar":
                commandBuffer.getComponent(minigameRef, MinigameComponent_TensionBar.COMPONENT_TYPE).DoInteraction(interactionType, context, cooldownHandler);
                break;
            case "NoMinigame":
                break;
            default:
                break;
        }
    }


    public static FishLootManager FirstRoll(Ref<EntityStore> bobberRef, Player player, CommandBuffer<EntityStore> commandBuffer, int depth) {
        AnglersAlmanac.LOGGER.atInfo().log("Doing first roll");
        AnglersAlmanac plugin = AnglersAlmanac.getInstance();
        Store<EntityStore> store = bobberRef.getStore();

        // Location
        var transform = store.getComponent(bobberRef, TransformComponent.getComponentType());
        if (transform == null) return null;

//        double x = transform.getPosition().getX();
        double y = transform.getPosition().getY();
//        double z = transform.getPosition().getZ();

        // time info
        WorldTimeResource timeResource = store.getResource(WorldTimeResource.getResourceType());
        var gameTime = timeResource.getGameTime().toString();
        String timeKeyword = TimeUtils.getTimeKeyword(gameTime);
        int moonPhase = timeResource.getMoonPhase();

        // Habitats info
        WorldMapTracker worldMapTracker = player.getWorldMapTracker();
        WorldMapTracker.ZoneDiscoveryInfo currentZone = worldMapTracker.getCurrentZone();
        String RawZone = currentZone.regionName();


        String Region = currentZone.zoneName();
        String Biome = worldMapTracker.getCurrentBiomeName();
        ZoneInfo info = EnvironmentParser.parse(RawZone);
        String zone = info.zone();
        int tier = info.tier();

        // Combine
        FishingContext LocationInfo = new FishingContext(
                TimeUtils.getTimePeriod(gameTime),
                moonPhase,
                zone,
                tier,
                Region,
                Biome,
                y,
                "clear",
                depth
        );
        // get fish
        FishLootManager lootEntry = FishLootManager.getRandomWeightedLoot(LocationInfo);
        if (lootEntry == null) {
            return null;
        }
        String lootID = lootEntry.getItemID();
        //plugin.getLogger().atInfo().log(lootID);

        //TODO 2nd roll depending on minigame

        //return lootID;
        return lootEntry;
        /*
        //Drop loot
        if(lootID ==null) return;
        ItemStack fishStack;
        fishStack = InventoryHelper.createItem(lootID);
        if (fishStack == null) {
            return;
        }
        DropItem(fishStack, player, commandBuffer, bobberRef);

         */

    }

    public static void DropItem(ItemStack loot, Player player, CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> bobberRef) {


        assert player.getReference() != null;
        TransformComponent transform = player.getReference().getStore().getComponent(player.getReference(), TransformComponent.getComponentType());

        ItemUtils.interactivelyPickupItem(player.getReference(), loot, transform.getPosition(),commandBuffer);

        //TODO
        //Rework to make it look like the fish is coming from the bobber and fly to the player?

    }

    public static void DropLoot(FishLootManager loot, Player player, CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> bobberRef, Minigame.PerformanceRating rating) {
        if (loot == null) return;
        if (loot.getItemID() == null) return;
        ItemStack fishStack;
        fishStack = InventoryHelper.createItem(loot.getItemID());

        if (fishStack == null) {
            return;
        }
        if (loot.getQuantity() != null) {
            int min = loot.getQuantity().min_amount;
            int max = loot.getQuantity().max_amount;

            if (max <= min) {
                fishStack.withQuantity(min);
            } else {
                int quantity = new Random().nextInt(min, max + 1);
                fishStack.withQuantity(quantity);
                // No idea if the thing above works need testing
            }
        }
        DropItem(fishStack, player, commandBuffer, bobberRef);
        SaveLoot(player, loot, rating);
    }

    public static void SaveLoot(Player player, FishLootManager loot, Minigame.PerformanceRating rating) {
        long startTime = System.currentTimeMillis();
        //save to database
        var playerRef = player.getReference();
        assert playerRef != null;
        UUIDComponent uuid = playerRef.getStore().getComponent(playerRef, UUIDComponent.getComponentType());
        com.hypixel.hytale.server.core.universe.PlayerRef playerRef1 = playerRef.getStore().getComponent(playerRef, PlayerRef.getComponentType());
        assert uuid != null;
        boolean isLegendary = loot.getRarity().equalsIgnoreCase("Legendary");

        CompletableFuture.supplyAsync(() -> {
            return AnglersAlmanac.getInstance().database.saveCatch(uuid.getUuid().toString(), loot.getId(), isLegendary, rating);
        }).thenAccept(isNewDiscovery -> {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            if (duration > 100) {
                AnglersAlmanac.LOGGER.atWarning().log("Slow DB Write: " +duration+"ms for player "+uuid.getUuid());
            }

            //AnglersAlmanac.LOGGER.atInfo().log("DB Write: " +duration+"ms for player "+uuid.getUuid());


            if (playerRef1 == null) return;
            if (isNewDiscovery) {
                String fishDisplayName = formatDisplayName(loot.getName());
                if (isLegendary) {
                    showDiscoveryUI(playerRef1, fishDisplayName, "LEGENDARY DISCOVERY", Color.YELLOW);
                    int audio = SoundEvent.getAssetMap().getIndex("AA_Fishing_Book_New_Fish_2");
                    assert player.getWorld() != null;
                    player.getWorld().execute(() -> {
                        SoundUtil.playSoundEvent2dToPlayer(playerRef1, audio, SoundCategory.UI);
                    });
                } else {
                    showDiscoveryUI(playerRef1, fishDisplayName, "New Fish Found", Color.GREEN);
                    int audio = SoundEvent.getAssetMap().getIndex("AA_Fishing_Book_New_Fish_1");
                    assert player.getWorld() != null;
                    player.getWorld().execute(() -> {
                        SoundUtil.playSoundEvent2dToPlayer(playerRef1, audio, SoundCategory.UI);
                    });
                }
            }
            BookPageManager.invalidateCache(String.valueOf(playerRef1.getUuid()));
        }).exceptionally(ex -> {
            AnglersAlmanac.LOGGER.atSevere().withCause(ex).log("Database error");
            return null;
        });
    }


    private static void showDiscoveryUI(PlayerRef ref, String fishName, String header, Color color) {
        Message fishDisplay = Message.raw(fishName).color(color);
        Message titleHeader = Message.raw(header);
        EventTitleUtil.showEventTitleToPlayer(ref, fishDisplay, titleHeader, false, null, 2, 0.5f, 0.5f);
    }


}
