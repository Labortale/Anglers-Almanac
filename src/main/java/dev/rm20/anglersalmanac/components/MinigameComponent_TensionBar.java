package dev.rm20.anglersalmanac.components;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.MinigameManager.Minigame;
import dev.rm20.anglersalmanac.MinigameManager.MinigameManager;
import dev.rm20.anglersalmanac.MinigameManager.RodStats;
import dev.rm20.anglersalmanac.config.MinigameConfig_TensionBar;
import dev.rm20.anglersalmanac.utils.FishLootManager;
import dev.rm20.anglersalmanac.utils.MinigameRodStats;
import dev.rm20.anglersalmanac.utils.TransformUtils;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class MinigameComponent_TensionBar  extends Minigame implements Component<EntityStore> {
    public static ComponentType<EntityStore, MinigameComponent_TensionBar> COMPONENT_TYPE;


    public MinigameComponent_TensionBar() {

    }

    // Internal use:
    public float fightProgress = 0.25f; // The progress to successful catch. Success when progress is at 1f.
    public float fishPos = 0f; // The position of the fish in the bar as a scale from 0 - 1.
    public float barPos = 0f; // The position of the catch bar.
    public float barVelocity = 0f; // The current direction and speed of the fishing bar.
    public float nextFishMoveTime = 0.5f; // The time until the next fish movement.
    public float fishMoveTimer = 0f; // Counts up until next fish move.
    public float fishVelocity = 0f; // The movement of the fish.
    public Ref<EntityStore> ownerRef;
    public Ref<EntityStore> bobberRef;
    public UUID selfUUID;
    public enum Trigger {NOTRIGGER, FISHMOVE, SUCCESS, FAIL}
    public Trigger stateTrigger = Trigger.NOTRIGGER;
    //public UUID minigameFishModelId;
    //public UUID minigameBarModelId;
    public HashMap<String, UUID> gameModels = new HashMap<>();
    public UUID audioPlayerId;
    public float minigameScale = 2f; // The visual size of the minigame display, adjusted based on distance from bobber.

    public String[] reelInSounds = {"AA_Fishing_Reel_Slow0", "AA_Fishing_Reel_Slow1", "AA_Fishing_Reel_Slow2", "AA_Fishing_Reel_Slow3"};
    public String[] escapeSounds = {"AA_Fishing_Line_Tension0", "AA_Fishing_Line_Tension1", "AA_Fishing_Line_Tension2", "AA_Fishing_Line_Tension3"};

    public MinigameConfig_TensionBar gameConfig;
    public FishLootManager.MinigameStats fishStats;

    // Used for calculating performance:
    public int ticksReeling = 0;
    public int ticksEscaping = 0;


    public MinigameComponent_TensionBar(Ref<EntityStore> ownerPlayerRef, Ref<EntityStore> bobberRef, UUID selfUUID){
        this.ownerRef = ownerPlayerRef;
        this.bobberRef = bobberRef;
        this.selfUUID = selfUUID;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        MinigameComponent_TensionBar component = new MinigameComponent_TensionBar(this.ownerRef, this.bobberRef, this.selfUUID);
        component.setTimePlayed(getTimePlayed());
        component.setPoints(getPoints());
        component.setPerfectScore(getPerfectScore());
        component.stateTrigger = this.stateTrigger;
        return component;
    }

    public static ComponentType<EntityStore, MinigameComponent_TensionBar> getComponentType() {
        return COMPONENT_TYPE;
    }



    public static MinigameComponent_TensionBar spawnMinigame(CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> playerRef, Ref<EntityStore> bobberRef, String rodAssetId){
        Vector3d bobberPos = commandBuffer.getComponent(bobberRef, TransformComponent.getComponentType()).getPosition().clone();

        // Set up and spawn minigame entity:
        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

        UUID id = UUIDUtil.generateVersion3UUID();
        holder.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(id));

        Vector3d spawnPos = commandBuffer.getComponent(bobberRef, TransformComponent.getComponentType()).getPosition().clone();
        holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(spawnPos, Vector3f.ZERO));


        //  --- Minigame --------
        MinigameComponent_TensionBar game = new MinigameComponent_TensionBar(playerRef, bobberRef, id);
            // Set minigame config as defaults.
        game.gameConfig = AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().clone();
            // Assign fish and apply modifiers.
        game.fishHooked = MinigameManager.FirstRoll(bobberRef, commandBuffer.getComponent(playerRef, Player.getComponentType()),commandBuffer, commandBuffer.getComponent(bobberRef, BobberComponent.getComponentType()).getWaterDepth());
        assert game.fishHooked != null;
        AnglersAlmanac.LOGGER.atInfo().log("Loading modifiers for fish: %s", game.fishHooked.getName());
        game.applyFishModifiers(game.fishHooked.getMinigameStats());
            // Apply rod modifiers.
        //AnglersAlmanac.LOGGER.atInfo().log("RodAssetId String = %s", rodAssetId);
        RodStats rodStats = MinigameRodStats.getRodStatsFromRodId(rodAssetId);
        game.applyRodModifiers(rodStats);
        holder.addComponent(MinigameComponent_TensionBar.COMPONENT_TYPE, game);

        //-----------------------

        // DEBUG
        AnglersAlmanac.LOGGER.atInfo().log("Fish diffiucly = %s, behaviour = %s, stamina = %s", game.fishHooked.getMinigameStats().difficulty, game.fishHooked.getMinigameStats().behavior, game.fishHooked.getMinigameStats().stamina);


        // Adjust minigame initialisation variables:
        double distanceFromPlayer = bobberPos.distanceTo(commandBuffer.getComponent(playerRef, TransformComponent.getComponentType()).getPosition());
        game.minigameScale = Math.clamp((float)distanceFromPlayer * AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().minigameScaleMultiplier, AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().minigameScaleMin, AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().minigameScaleMax);

        commandBuffer.getExternalData().getWorld().execute( () -> {
            commandBuffer.addEntity(holder, AddReason.SPAWN);
        });

        game.spawnMinigameAdditionals(commandBuffer);

        return game;
    }

    public void despawnSelf(World world){

        //AnglersAlmanac.LOGGER.atInfo().log("Fishing Minigame is despawning itself.");

        Store<EntityStore> store = world.getEntityStore().getStore();

        // Attempt to despawn additional models.
        for(UUID id : gameModels.values()){
            Ref<EntityStore> ref = world.getEntityRef(id);
            if(ref != null){
                world.execute(() -> {
                    store.removeEntity(ref, RemoveReason.REMOVE);
                });
            }
        }

        /*
        if(gameModels.get("fish") != null) {
            Ref<EntityStore> fishModelRef = world.getEntityRef(gameModels.get("fish"));
            if (fishModelRef != null) {
                world.execute(() -> {
                    store.removeEntity(fishModelRef, RemoveReason.REMOVE);
                });
            }
        }

        if(gameModels.get("bar") != null) {
            Ref<EntityStore> barModelRef = world.getEntityRef(gameModels.get("bar"));
            if (barModelRef != null) {
                world.execute(() -> {
                    store.removeEntity(barModelRef, RemoveReason.REMOVE);
                });
            }
        }

         */

        if(audioPlayerId != null) {
            Ref<EntityStore> audioPlayerRef = world.getEntityRef(audioPlayerId);
            if (audioPlayerRef != null) {
                world.execute(() -> {
                    store.removeEntity(audioPlayerRef, RemoveReason.REMOVE);
                });
            }
        }


        // Despawn self.
        world.execute(() -> {
            store.removeEntity(store.getExternalData().getRefFromUUID(selfUUID), RemoveReason.REMOVE);
        });
    }

    public void spawnMinigameAdditionals(CommandBuffer<EntityStore> commandBuffer){

        Vector3d bobberPos = commandBuffer.getComponent(bobberRef, TransformComponent.getComponentType()).getPosition().clone();

        // Spawn audio player
        AudioPlayerComponent apc = AudioPlayerComponent.spawnNewAudioPlayerEntity(bobberPos, commandBuffer);
        apc.addSounds(reelInSounds);
        apc.allowedOverlap = 30000000;
        audioPlayerId = apc.selfUUID;


        // ------- FISH MODEL -------------------------------------------------------------------
        Holder<EntityStore> fishModelEntity = EntityStore.REGISTRY.newHolder();
        UUID fishModelId = UUIDUtil.generateVersion3UUID();
        fishModelEntity.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(fishModelId));
        gameModels.put("fish", fishModelId);



        // Assign transform to minigame and move it above the bobber.
        fishModelEntity.addComponent(TransformComponent.getComponentType(), new TransformComponent());
        Vector3d newPos = bobberPos.clone();
        newPos = newPos.add(new Vector3d(0,AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().minigameModelVerticalOffset,0));
        fishModelEntity.getComponent(TransformComponent.getComponentType()).setPosition(newPos);
        //TransformUtils.applyBillboard(store.getExternalData().getRefFromUUID(fishModelId), ownerRef, new Vector3f(90,0,0), store);

        // Add model.
        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("SSF_FishIcon");
        if (modelAsset == null) modelAsset = ModelAsset.DEBUG;
        Model model = Model.createScaledModel(modelAsset, 0.5f * minigameScale);
        fishModelEntity.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
        fishModelEntity.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
        fishModelEntity.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));

        // Attach network component.
        fishModelEntity.addComponent(NetworkId.getComponentType(), new NetworkId(commandBuffer.getExternalData().takeNextNetworkId()));

        // Spawn the model in the world.
        World world = commandBuffer.getExternalData().getWorld();
        world.execute(() -> {
            commandBuffer.addEntity(fishModelEntity, AddReason.SPAWN);
            //AnglersAlmanac.LOGGER.atInfo().log("Spawned fish model at: %s", fishModelEntity.getComponent(TransformComponent.getComponentType()).getPosition());
        });


        //---------- BAR MODEL -----------------------------------------------------
        Holder<EntityStore> barModelEntity = EntityStore.REGISTRY.newHolder();
        UUID barModelEntityId = UUID.randomUUID();
        barModelEntity.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(barModelEntityId));
        gameModels.put("bar", barModelEntityId);

        // Assign transform to minigame and move it above the bobber.
        barModelEntity.addComponent(TransformComponent.getComponentType(), new TransformComponent());
        Vector3d newBarPos = commandBuffer.getComponent(bobberRef, TransformComponent.getComponentType()).getPosition().clone();
        //newBarPos = newBarPos.add(new Vector3d(0,bobber.minigameModelVerticalOffset,0));
        Vector3d playerPos = commandBuffer.getComponent(ownerRef, TransformComponent.getComponentType()).getPosition().clone();
        newBarPos = newBarPos.add(TransformUtils.moveAwayFrom(newBarPos ,playerPos, 2));
        barModelEntity.getComponent(TransformComponent.getComponentType()).setPosition(newBarPos);
        //HelperTransforms.applyBillboard(barModelEntityId, bobber.ownerID, new Vector3f(90,0,0), store);


        // Add model.
        ModelAsset barModelAsset = ModelAsset.getAssetMap().getAsset("SSF_FishingBar");
        if (barModelAsset == null) barModelAsset = ModelAsset.DEBUG;
        Model barModel = Model.createScaledModel(barModelAsset, (AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barRadius * 2f) * minigameScale);
        barModelEntity.addComponent(PersistentModel.getComponentType(), new PersistentModel(barModel.toReference()));
        barModelEntity.addComponent(ModelComponent.getComponentType(), new ModelComponent(barModel));
        barModelEntity.addComponent(BoundingBox.getComponentType(), new BoundingBox(barModel.getBoundingBox()));

        //---------- Frame MODEL -----------------------------------------------------
        Holder<EntityStore> frameUpperModelEntity = EntityStore.REGISTRY.newHolder();
        UUID frameUpperModelEntityId = UUID.randomUUID();
        frameUpperModelEntity.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(frameUpperModelEntityId));
        gameModels.put("frameUpper", frameUpperModelEntityId);

        // Assign transform to minigame and move it above the bobber.
        frameUpperModelEntity.addComponent(TransformComponent.getComponentType(), new TransformComponent());
        Vector3d upperFramePos = commandBuffer.getComponent(bobberRef, TransformComponent.getComponentType()).getPosition().clone();
        upperFramePos = upperFramePos.add(TransformUtils.moveAwayFrom(upperFramePos ,playerPos, 2));
        frameUpperModelEntity.getComponent(TransformComponent.getComponentType()).setPosition(upperFramePos.add(new Vector3d(0, AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().minigameModelVerticalOffset + (1.0 * minigameScale),0)));

        Holder<EntityStore> frameLowerModelEntity = EntityStore.REGISTRY.newHolder();
        UUID frameLowerModelEntityId = UUID.randomUUID();
        frameLowerModelEntity.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(frameLowerModelEntityId));
        gameModels.put("frameLower", frameUpperModelEntityId);

        // Assign transform to minigame and move it above the bobber.
        frameLowerModelEntity.addComponent(TransformComponent.getComponentType(), new TransformComponent());
        Vector3d lowerFramePos = commandBuffer.getComponent(bobberRef, TransformComponent.getComponentType()).getPosition().clone();
        lowerFramePos = lowerFramePos.add(TransformUtils.moveAwayFrom(lowerFramePos ,playerPos, 2));
        frameLowerModelEntity.getComponent(TransformComponent.getComponentType()).setPosition(lowerFramePos);



        // Add model.
        ModelAsset frameModelAsset = ModelAsset.getAssetMap().getAsset("SSF_MinigameFrame");
        if (frameModelAsset == null) frameModelAsset = ModelAsset.DEBUG;
        Model frameUpperModel = Model.createScaledModel(frameModelAsset, minigameScale);
        frameUpperModelEntity.addComponent(PersistentModel.getComponentType(), new PersistentModel(frameUpperModel.toReference()));
        frameUpperModelEntity.addComponent(ModelComponent.getComponentType(), new ModelComponent(frameUpperModel));
        frameUpperModelEntity.addComponent(BoundingBox.getComponentType(), new BoundingBox(frameUpperModel.getBoundingBox()));

        Model frameLowerModel = Model.createScaledModel(frameModelAsset, minigameScale);
        frameLowerModelEntity.addComponent(PersistentModel.getComponentType(), new PersistentModel(frameLowerModel.toReference()));
        frameLowerModelEntity.addComponent(ModelComponent.getComponentType(), new ModelComponent(frameLowerModel));
        frameLowerModelEntity.addComponent(BoundingBox.getComponentType(), new BoundingBox(frameLowerModel.getBoundingBox()));



        // Attach network component.
        barModelEntity.addComponent(NetworkId.getComponentType(), new NetworkId(commandBuffer.getExternalData().takeNextNetworkId()));
        frameUpperModelEntity.addComponent(NetworkId.getComponentType(), new NetworkId(commandBuffer.getExternalData().takeNextNetworkId()));
        frameLowerModelEntity.addComponent(NetworkId.getComponentType(), new NetworkId(commandBuffer.getExternalData().takeNextNetworkId()));

        // Spawn the model in the world.
        world.execute(() -> {
            commandBuffer.addEntity(barModelEntity, AddReason.SPAWN);
            //AnglersAlmanac.LOGGER.atInfo().log("Spawned bar model at: %s", barModelEntity.getComponent(TransformComponent.getComponentType()).getPosition());
        });

    }

    public void updateMinigameModelPositions(Store<EntityStore> store){

        Ref<EntityStore> game = store.getExternalData().getWorld().getEntityRef(selfUUID);
        assert game != null;

        // Do fish logic.
        Ref<EntityStore> fishModelRef = store.getExternalData().getWorld().getEntityRef(gameModels.get("fish"));
        if(fishModelRef == null) return;
        if(!bobberRef.isValid()) return;
        // Do fish model motion.

        Vector3d newFishPos = Objects.requireNonNull(store.getComponent(game, TransformComponent.getComponentType())).getPosition().clone();

        /*
        // Move fish to player based on progress.
        Ref<EntityStore> playerRef = store.getExternalData().getRefFromUUID(bobber.ownerID);
        if(playerRef != null) {
            Vector3d playerPos = store.getComponent(playerRef, TransformComponent.getComponentType()).getPosition().clone();
            newFishPos = HelperTransforms.moveTowards(newFishPos, playerPos, bobber.fightProgress/100f);
        }

         */

        // Adjust fish height based on minigame fishPos.
        newFishPos = newFishPos.add(new Vector3d(0,AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().minigameModelVerticalOffset + (fishPos * minigameScale),0));



        store.getComponent(fishModelRef, TransformComponent.getComponentType()).setPosition(newFishPos);
        // Do Fish rotation.
        float camOffset =  store.getComponent(ownerRef, ModelComponent.getComponentType()).getModel().getEyeHeight();
        Vector3d playerHeadPos = store.getComponent(ownerRef, TransformComponent.getComponentType()).getPosition().clone().add(new Vector3d(0, camOffset,0));
        TransformUtils.applyBillboardYOnly(gameModels.get("fish"), newFishPos, playerHeadPos ,new Vector3f(90,0,0), store);


        // Do bar logic.
        Ref<EntityStore> barModelRef = store.getExternalData().getWorld().getEntityRef(gameModels.get("bar"));
        if(barModelRef == null) return;

        // Do bar model motion.
        Vector3d newBarPos = store.getComponent(game, TransformComponent.getComponentType()).getPosition().clone();
        newBarPos = newBarPos.add(new Vector3d(0,AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().minigameModelVerticalOffset + (barPos * minigameScale) ,0));
        Vector3d playerPos = store.getComponent(ownerRef, TransformComponent.getComponentType()).getPosition().clone();

        Vector3d layering = newBarPos.clone().add(TransformUtils.moveAwayFrom(newBarPos.clone() ,playerPos, 0.2));
        newBarPos = new Vector3d(layering.x, newBarPos.y, layering.z);

        store.getComponent(barModelRef, TransformComponent.getComponentType()).setPosition(newBarPos);

        // Do bar rotation.
        TransformUtils.applyBillboardYOnly(gameModels.get("bar"), newBarPos, playerHeadPos, new Vector3f(0,0,0), store);


    }

    public void DoInteraction(@NonNull InteractionType interactionType, @NonNull InteractionContext context, @NonNull CooldownHandler cooldownHandler){
        //AnglersAlmanac.LOGGER.atInfo().log("Running TensionBar Minigame interaction");

        //Move bar up.
        barVelocity = Math.clamp(barVelocity + (AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barSpeed * AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barAcceleration)
                + (AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barGravity*AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barAcceleration)
                , AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barGravity, AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barSpeed);
    }


    @Override
    public int getPerformancePercentage(){
        int totalGameTicks  = ticksReeling + ticksEscaping;
        int performancePercentage = (int)( ((float)ticksReeling  / (float)totalGameTicks) * 100);
        AnglersAlmanac.LOGGER.atInfo().log("Minigame performance percentage = %s", performancePercentage);
        return performancePercentage;
    }

// ----------  GAME MODIFIER ADAPTION  ----------------------------------------------------------------------------
// ================================================================================================================

    @Override
    public void applyDifficultyModifer(FishLootManager.MinigameStats stats) {
        gameConfig.fishMaxVeocity += (float) stats.difficulty / 20f;
        gameConfig.fishChangeDirectionMaxInterval = gameConfig.fishChangeDirectionMaxInterval / ((float) stats.difficulty / 8f);
        AnglersAlmanac.LOGGER.atInfo().log("Applying fish difficulty! stat: %s, fishMaxVelocity: %s", stats.difficulty, gameConfig.fishMaxVeocity);
    }

    @Override
    public void applyFishBehaviourModifer(FishLootManager.MinigameStats stats) {
        switch (stats.behavior){
            case "darting":
                // Stays still, then max speed, then still again, repeat.
                // Behaviour is mostly assigned in minigame system when triggering FISHMOVE.
                gameConfig.fishMinSpeed = 1.0f;
                break;
            case "floater":
                gameConfig.fishBouyancy += gameConfig.fishMaxVeocity * 0.6f;
                break;
            case "sinker:":
                gameConfig.fishBouyancy -= gameConfig.fishMaxVeocity * 0.6f;
                break;
            case "heavy_sinker":
                gameConfig.fishMaxVeocity *= 1.2f;
                gameConfig.fishBouyancy -= gameConfig.fishMaxVeocity * 0.9f;

                break;
            case "aggressive":
                // Never slow, turns rapidly.
                gameConfig.fishMaxVeocity *= 0.9f;
                gameConfig.fishMinSpeed = 0.7f;
                gameConfig.fishChangeDirectionMaxInterval *= 0.5f;
                break;
            case "erratic":
                // Changes direction very frequently.
                gameConfig.fishMaxVeocity *= 0.6f;
                gameConfig.fishChangeDirectionMaxInterval *= 0.1f;
                break;
            case "steady":
                // Rarely changes direction. Very predictable.
                gameConfig.fishMaxVeocity *= 0.7f;
                gameConfig.fishMinSpeed = 0.5f;
                gameConfig.fishChangeDirectionMaxInterval += 0.2f;
                break;

        }
        AnglersAlmanac.LOGGER.atInfo().log("Applying fish behaviour! %s", stats.behavior);
    }

    @Override
    public void applyFishStaminaModifer(FishLootManager.MinigameStats stats) {
        // 30 represents standard stamina, with stats higher than 30 increasing time to catch, and stats lower than 30 reducing time to catch.
        // The stamina difference is dampened to prevent extreme modifications.
        AnglersAlmanac.LOGGER.atInfo().log("Base reelRate = %s", gameConfig.fishReelRate);
        float dampeningFactor = 0.33f;
        float dampenedStamina = stats.stamina + (30f - stats.stamina) * dampeningFactor;
        float modifier = 30f / dampenedStamina;
        gameConfig.fishReelRate *= modifier;
        AnglersAlmanac.LOGGER.atInfo().log("Applying fish stamina! stamina = %s, modifier: %s,  reelRate = %s", stats.stamina,modifier,  gameConfig.fishReelRate);
    }

    @Override
    public void applyRodControlModifer(RodStats rodStats) {
        AnglersAlmanac.LOGGER.atInfo().log("Applying rod control");
    }

    @Override
    public void applyRodDifficultyModifer(RodStats rodStats) {
        AnglersAlmanac.LOGGER.atInfo().log("Applying rod difficulty");
    }

    @Override
    public void applyRodForgivenessModifer(RodStats rodStats) {
        AnglersAlmanac.LOGGER.atInfo().log("Applying rod forgiveness");
    }

    @Override
    public void applyRodStaminaModifer(RodStats rodStats) {
        AnglersAlmanac.LOGGER.atInfo().log("Applying rod stamina");
    }

    @Override
    public void applyRodFishWeightModifer(RodStats rodStats) {
        AnglersAlmanac.LOGGER.atInfo().log("Applying fish weight modfifer");
    }

    @Override
    public void applyRodRarityModifer(RodStats rodStats) {
        AnglersAlmanac.LOGGER.atInfo().log("Applying fish rarity modifier");
    }
}
