package dev.rm20.anglersalmanac.MinigameManager;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.components.MinigameComponent_TensionBar;
import dev.rm20.anglersalmanac.interactions.LaunchBobberInteraction;
import dev.rm20.anglersalmanac.models.FishingContext;
import dev.rm20.anglersalmanac.models.FishingRodData;
import dev.rm20.anglersalmanac.models.ZoneInfo;
import dev.rm20.anglersalmanac.utils.EnvironmentParser;
import dev.rm20.anglersalmanac.utils.FishLootManager;
import dev.rm20.anglersalmanac.utils.TimeUtils;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public class MinigameManager {
    public static void StartGame(Ref<EntityStore> bobberRef, Player player, CommandBuffer<EntityStore> commandBuffer, int depth)
    {
        // Set rod mode
        //TODO add fail safes for hotbar item changing.
        //Assuming active hotbar item has not changed.
        ItemStack fishingRod = player.getInventory().getActiveHotbarItem();

        // Select which minigame to use from the config and set it up.
        switch(AnglersAlmanac.MOD_CONFIG.get().getMinigameToUse()){
            case "TensionBar":
                FishingRodData meta = fishingRod.getFromMetadataOrNull(FishingRodData.KEYED_CODEC);
                if(meta== null)
                {
                    LaunchBobberInteraction.cancelFishing(commandBuffer, player, fishingRod);
                    break;
                }
                Inventory inv  = player.getInventory();
                UUID minigameID = MinigameComponent_TensionBar.spawnMinigame(commandBuffer.getStore(),player.getReference(), bobberRef);
                LaunchBobberInteraction.updateMetadata(inv, inv.getActiveHotbarSlot(), inv.getActiveHotbarItem(), meta.getBoundBobber(), minigameID, 1);
                break;
            case "NoMinigame":
                DropLoot(FirstRoll(bobberRef, player, commandBuffer, depth), player, commandBuffer, bobberRef);
                LaunchBobberInteraction.cancelFishing(commandBuffer, player, fishingRod);
                break;
            default: // No Minigame, just reel fish.
                DropLoot(FirstRoll(bobberRef, player, commandBuffer, depth), player, commandBuffer, bobberRef);
                LaunchBobberInteraction.cancelFishing(commandBuffer, player, fishingRod);
                break;
        }

    }

    public static void CancelGame(CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> minigameRef){
        AnglersAlmanac.LOGGER.atInfo().log("Selecting Minigame to cancel:");
    // Select which minigame to use from the config and cancel it.
        switch(AnglersAlmanac.MOD_CONFIG.get().getMinigameToUse()){
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


    public static void DoMinigameInteraction(CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> minigameRef, @NonNull InteractionType interactionType, @NonNull InteractionContext context, @NonNull CooldownHandler cooldownHandler){
        switch(AnglersAlmanac.MOD_CONFIG.get().getMinigameToUse()){
            case "TensionBar":
                commandBuffer.getComponent(minigameRef, MinigameComponent_TensionBar.COMPONENT_TYPE).DoInteraction(interactionType, context, cooldownHandler);
                break;
            case "NoMinigame":
                break;
            default:
                break;
        }
    }


    public static String FirstRoll(Ref<EntityStore> bobberRef, Player player, CommandBuffer<EntityStore> commandBuffer, int depth) {
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
                timeKeyword,
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
        if(lootEntry == null)
        {
            return null;
        }
        String lootID = lootEntry.getItemID();
        plugin.getLogger().atInfo().log(lootID);

        //TODO 2nd roll depending on minigame

        return lootID;

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
        ItemUtils.throwItem(player.getReference(), loot, 0f, commandBuffer);

        //TODO
        //Rework to make it look like the fish is coming from the bobber and fly to the player?

    }

    public static void DropLoot(String lootID, Player player, CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> bobberRef){
        if(lootID ==null) return;
        ItemStack fishStack;
        fishStack = InventoryHelper.createItem(lootID);
        if (fishStack == null) {
            return;
        }
        DropItem(fishStack, player, commandBuffer, bobberRef);
    }

}
