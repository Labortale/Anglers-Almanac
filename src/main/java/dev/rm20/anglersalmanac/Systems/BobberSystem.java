package dev.rm20.anglersalmanac.Systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.components.BobberComponent;
import dev.rm20.anglersalmanac.interactions.LaunchBobberInteraction;
import dev.rm20.anglersalmanac.metadata.FishingRodData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;

public class BobberSystem extends EntityTickingSystem<EntityStore> {
    private final Random random = new Random();
    ItemStack fishingRod = null;
    byte slot = 0;
    @Override
    public void tick(float v, int i, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        BobberComponent component = archetypeChunk.getComponent(i, BobberComponent.getComponentType());
        TransformComponent transform = archetypeChunk.getComponent(i, TransformComponent.getComponentType());
        if(component !=null)
        {
            Player player = component.getPlayer();
            if (player != null) {
                ItemStack heldItem = player.getInventory().getItemInHand();
                FishingRodData meta = (heldItem != null) ? heldItem.getFromMetadataOrNull(FishingRodData.KEY, FishingRodData.CODEC) : null;
                UUIDComponent uuidComponent = archetypeChunk.getComponent(i, UUIDComponent.getComponentType());
                if(meta != null)
                {
                    if(meta.getBoundBobber()==null||!Objects.requireNonNull(uuidComponent).getUuid().equals(meta.getBoundBobber()))
                    {
                        if(fishingRod == null)
                        {
                            commandBuffer.removeEntity(archetypeChunk.getReferenceTo(i), RemoveReason.REMOVE);
                        }
                        else
                        {
                            LaunchBobberInteraction.cancelFishing(commandBuffer,player,fishingRod,slot);
                        }
                        //LaunchBobberInteraction.cancelFishing(commandBuffer,player,fishingRod,slot);
                        //commandBuffer.removeEntity(archetypeChunk.getReferenceTo(i), RemoveReason.REMOVE);
                        return;
                    }
                    else{
                        slot = player.getInventory().getActiveHotbarSlot();
                        fishingRod = heldItem;
                    }
                }
                if (meta == null || !Objects.requireNonNull(uuidComponent).getUuid().equals(meta.getBoundBobber())) {
                    //AnglersAlmanac.getInstance().getLogger().atInfo().log("Removed bobber - Rod swapped or dropped");
                    //commandBuffer.removeEntity(archetypeChunk.getReferenceTo(i), RemoveReason.REMOVE);
                    assert fishingRod != null;
                    //AnglersAlmanac.getInstance().getLogger().atInfo().log(fishingRod.toString());
                    LaunchBobberInteraction.cancelFishing(commandBuffer,player,fishingRod,slot);
                    //AnglersAlmanac.getInstance().getLogger().atInfo().log(fishingRod.toString());
                    return;
                }
            }
            else
            {
                commandBuffer.removeEntity(archetypeChunk.getReferenceTo(i), RemoveReason.REMOVE);
                return;
            }
        }

        if (component == null || !component.InWater()) return;

        float newAge = component.getBobberAge() + v;
        component.setBobberAge(newAge);

        if (newAge < 4.0f) return;

        if (component.isCanCatch()) {
            float catchTime = component.getCatchTimer() - v;
            if (catchTime <= 0) {
                // Fish escaped
                component.setCanCatch(false);
                resetWaitTimer(component);
            } else {
                component.setCatchTimer(catchTime);
            }
        } else {
            float timeUntilCatch = component.getTimeUntilCatch();
            if (timeUntilCatch <= 0) {
                // Fish bite logic
                component.setCanCatch(true);
                ParticleUtil.spawnParticleEffect("Fish_Alert", transform.getPosition().clone().add(0, 0.5, 0), store);
                //Audio
                int audio = SoundEvent.getAssetMap().getIndex("AA_Fishing_Bubble");
                World world = store.getExternalData().getWorld();
                EntityStore store2 = world.getEntityStore();
                world.execute(() -> {
                    SoundUtil.playSoundEvent3d(audio, SoundCategory.SFX, transform.getPosition(), store2.getStore());
                });
                // Reaction window: How long the player has to click.
                // Making this slightly more random (e.g., 0.8s to 3.0s)
                float reactionWindow = 0.8f + random.nextFloat() * 2.2f;
                component.setCatchTimer(reactionWindow);
            } else {
                component.setTimeUntilCatch(timeUntilCatch - v);
            }
        }
    }

    /**
     * Resets the timer with a much wider, longer range for a "realistic" feel.
     */
    private void resetWaitTimer(BobberComponent component) {
        // This makes fishing feel like a secondary activity rather than a rapid-fire minigame.
        float minWait = 5.0f;
        float maxWait = 35.0f;
        float randomWait = minWait + random.nextFloat() * (maxWait - minWait);

        component.setTimeUntilCatch(randomWait);
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(BobberComponent.getComponentType());
    }
}
