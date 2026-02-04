package dev.rm20.anglersalmanac.components;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.physics.SimplePhysicsProvider;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.utils.TransformUtils;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class MinigameComponent_TensionBar implements Component<EntityStore> {
    public static ComponentType<EntityStore, MinigameComponent_TensionBar> COMPONENT_TYPE;
    // How long a player has been playing
    private float TimePlayed;
    // Total points that the player has
    private int Points;
    // PerfectScore
    private float perfectScore;

    public MinigameComponent_TensionBar() {

    }

    public float getTimePlayed() {
        return TimePlayed;
    }

    public void setTimePlayed(float timePlayed) {
        TimePlayed = timePlayed;
    }

    public int getPoints() {
        return Points;
    }

    public void setPoints(int points) {
        Points = points;
    }


    public float getPerfectScore() {
        return perfectScore;
    }

    public void setPerfectScore(float perfectScore) {
        this.perfectScore = perfectScore;
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
    public UUID minigameFishModelId;
    public UUID minigameBarModelId;
    public UUID audioPlayerId;
    public float minigameScale = 2f; // The visual size of the minigame display, adjusted based on distance from bobber.

    public String[] reelInSounds = {"AA_Fishing_Reel_Slow0", "AA_Fishing_Reel_Slow1", "AA_Fishing_Reel_Slow2", "AA_Fishing_Reel_Slow3"};
    public String[] escapeSounds = {"AA_Fishing_Line_Tension0", "AA_Fishing_Line_Tension1", "AA_Fishing_Line_Tension2", "AA_Fishing_Line_Tension3"};


    public MinigameComponent_TensionBar(Ref<EntityStore> ownerPlayerRef, Ref<EntityStore> bobberRef, UUID selfUUID){
        this.ownerRef = ownerPlayerRef;
        this.bobberRef = bobberRef;
        this.selfUUID = selfUUID;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        MinigameComponent_TensionBar component = new MinigameComponent_TensionBar(this.ownerRef, this.bobberRef, this.selfUUID);
        component.TimePlayed = this.TimePlayed;
        component.Points = this.Points;
        component.perfectScore = this.perfectScore;
        component.stateTrigger = this.stateTrigger;
        return component;
    }

    public static ComponentType<EntityStore, MinigameComponent_TensionBar> getComponentType() {
        return COMPONENT_TYPE;
    }



    public static UUID spawnMinigame(Store<EntityStore> store, Ref<EntityStore> playerRef, Ref<EntityStore> bobberRef){
        Vector3d bobberPos = store.getComponent(bobberRef, TransformComponent.getComponentType()).getPosition().clone();

        // Set up and spawn minigame entity:
        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

        UUID id = UUIDUtil.generateVersion3UUID();
        holder.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(id));

        // Minigame
        MinigameComponent_TensionBar game = new MinigameComponent_TensionBar(playerRef, bobberRef, id);
        holder.addComponent(MinigameComponent_TensionBar.COMPONENT_TYPE, game);


        store.getExternalData().getWorld().execute( () -> {
            store.addEntity(holder, AddReason.SPAWN);
        });


        // Adjust minigame initialisation variables:
        double distanceFromPlayer = bobberPos.distanceTo(store.getComponent(playerRef, TransformComponent.getComponentType()).getPosition());
        game.minigameScale = Math.clamp((float)distanceFromPlayer * AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().minigameScaleMultiplier, AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().minigameScaleMin, AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().minigameScaleMax);

        game.spawnMinigameModels(store);

        return id;
    }

    public void despawnSelf(World world){

        //AnglersAlmanac.LOGGER.atInfo().log("Fishing Minigame is despawning itself.");

        Store<EntityStore> store = world.getEntityStore().getStore();

        // Attempt to despawn additional models.
        if(minigameFishModelId != null) {
            Ref<EntityStore> fishModelRef = world.getEntityRef(minigameFishModelId);
            if (fishModelRef != null) {
                world.execute(() -> {
                    store.removeEntity(fishModelRef, RemoveReason.REMOVE);
                });
            }
        }

        if(minigameBarModelId != null) {
            Ref<EntityStore> barModelRef = world.getEntityRef(minigameBarModelId);
            if (barModelRef != null) {
                world.execute(() -> {
                    store.removeEntity(barModelRef, RemoveReason.REMOVE);
                });
            }
        }

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

    public void spawnMinigameModels(Store<EntityStore> store){

        Vector3d bobberPos = store.getComponent(bobberRef, TransformComponent.getComponentType()).getPosition().clone();

        // Also spawn audio
        AudioPlayerComponent apc = AudioPlayerComponent.spawnNewAudioPlayerEntity(bobberPos, store);
        apc.addSounds(reelInSounds);
        apc.allowedOverlap = 30000000;
        audioPlayerId = apc.selfUUID;


        // ------- FISH MODEL -------------------------------------------------------------------
        Holder<EntityStore> fishModelEntity = EntityStore.REGISTRY.newHolder();
        UUID fishModelId = UUIDUtil.generateVersion3UUID();
        fishModelEntity.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(fishModelId));
        minigameFishModelId = fishModelId;



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
        fishModelEntity.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));

        // Spawn the model in the world.
        World world = store.getExternalData().getWorld();
        world.execute(() -> {
            store.addEntity(fishModelEntity, AddReason.SPAWN);
            //AnglersAlmanac.LOGGER.atInfo().log("Spawned fish model at: %s", fishModelEntity.getComponent(TransformComponent.getComponentType()).getPosition());
        });


        //---------- BAR MODEL -----------------------------------------------------
        Holder<EntityStore> barModelEntity = EntityStore.REGISTRY.newHolder();
        UUID barModelEntityId = UUID.randomUUID();
        barModelEntity.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(barModelEntityId));
        minigameBarModelId = barModelEntityId;

        // Assign transform to minigame and move it above the bobber.
        barModelEntity.addComponent(TransformComponent.getComponentType(), new TransformComponent());
        Vector3d newBarPos = store.getComponent(bobberRef, TransformComponent.getComponentType()).getPosition().clone();
        //newBarPos = newBarPos.add(new Vector3d(0,bobber.minigameModelVerticalOffset,0));
        Vector3d playerPos = store.getComponent(ownerRef, TransformComponent.getComponentType()).getPosition().clone();
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



        // Attach network component.
        barModelEntity.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));

        // Spawn the model in the world.
        world.execute(() -> {
            store.addEntity(barModelEntity, AddReason.SPAWN);
            //AnglersAlmanac.LOGGER.atInfo().log("Spawned bar model at: %s", barModelEntity.getComponent(TransformComponent.getComponentType()).getPosition());
        });

    }

    public void updateMinigameModelPositions(Store<EntityStore> store){

        // Do fish logic.
        Ref<EntityStore> fishModelRef = store.getExternalData().getWorld().getEntityRef(minigameFishModelId);
        if(fishModelRef == null) return;
        if(!bobberRef.isValid()) return;
        // Do fish model motion.
        Vector3d newFishPos = Objects.requireNonNull(store.getComponent(bobberRef, TransformComponent.getComponentType())).getPosition().clone();

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
        TransformUtils.applyBillboardYOnly(minigameFishModelId, newFishPos, playerHeadPos ,new Vector3f(90,0,0), store);


        // Do bar logic.
        Ref<EntityStore> barModelRef = store.getExternalData().getWorld().getEntityRef(minigameBarModelId);
        if(barModelRef == null) return;

        // Do bar model motion.
        Vector3d newBarPos = store.getComponent(bobberRef, TransformComponent.getComponentType()).getPosition().clone();
        newBarPos = newBarPos.add(new Vector3d(0,AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().minigameModelVerticalOffset + (barPos * minigameScale) ,0));
        Vector3d playerPos = store.getComponent(ownerRef, TransformComponent.getComponentType()).getPosition().clone();

        Vector3d layering = newBarPos.clone().add(TransformUtils.moveAwayFrom(newBarPos.clone() ,playerPos, 0.2));
        newBarPos = new Vector3d(layering.x, newBarPos.y, layering.z);

        store.getComponent(barModelRef, TransformComponent.getComponentType()).setPosition(newBarPos);

        // Do bar rotation.
        TransformUtils.applyBillboardYOnly(minigameBarModelId, newBarPos, playerHeadPos, new Vector3f(0,0,0), store);


    }

    public void DoInteraction(@NonNull InteractionType interactionType, @NonNull InteractionContext context, @NonNull CooldownHandler cooldownHandler){
        //AnglersAlmanac.LOGGER.atInfo().log("Running TensionBar Minigame interaction");

        //Move bar up.
        barVelocity = Math.clamp(barVelocity + (AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barSpeed * AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barAcceleration)
                + (AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barGravity*AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barAcceleration)
                , AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barGravity, AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barSpeed);
    }
}
