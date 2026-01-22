package com.kdrgold.gbr.systems;

import java.util.Random;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.kdrgold.gbr.components.tracking.PlayerKillTrackerComponent;
import com.kdrgold.gbr.config.ConditionalSpawnConfig;

import it.unimi.dsi.fastutil.Pair;

/**
 * System that spawns elite NPCs based on player kill counts.
 * Uses DeathSystems.OnDeathSystem for proper death event handling.
 * 
 * @author kdrgold
 * @version 2.0.0
 */
public class EliteSpawnSystem extends DeathSystems.OnDeathSystem {
    
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final Random random = new Random();
    
    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        // Track all entity deaths - we'll filter for trackable NPCs in the handler
        return Archetype.empty();
    }
    
    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> deadEntityRef,
                                 @Nonnull DeathComponent deathComponent,
                                 @Nonnull Store<EntityStore> store,
                                 @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        try {
            if (ConditionalSpawnConfig.DEBUG_MODE) {
                LOGGER.atInfo().log("[GBR] EliteSpawnSystem.onComponentAdded triggered!");
            }
            
            // Check if the dead entity is a trackable NPC
            NPCEntity npcComponent = store.getComponent(deadEntityRef, NPCEntity.getComponentType());
            if (npcComponent == null) {
                return;
            }
            
            String npcRole = npcComponent.getRoleName();
            if (npcRole == null || !ConditionalSpawnConfig.isTrackableEntity(npcRole)) {
                return;
            }
            
            if (ConditionalSpawnConfig.DEBUG_MODE) {
                LOGGER.atInfo().log("[GBR] Trackable NPC died: " + npcRole + ", checking spawn conditions");
            }
            
            // Get the killer (must be a player)
            Damage deathInfo = deathComponent.getDeathInfo();
            if (deathInfo == null || !(deathInfo.getSource() instanceof Damage.EntitySource)) {
                return;
            }
            
            Damage.EntitySource source = (Damage.EntitySource) deathInfo.getSource();
            Ref<EntityStore> killerRef = source.getRef();
            
            if (killerRef == null || !killerRef.isValid()) {
                return;
            }
            
            Player playerComponent = store.getComponent(killerRef, Player.getComponentType());
            if (playerComponent == null) {
                return;
            }
            
            // Get PlayerRef for notifications
            PlayerRef playerRef = store.getComponent(killerRef, PlayerRef.getComponentType());
            if (playerRef == null) {
                return;
            }
            
            // Get player's kill tracker - DO NOT CREATE, only KillTrackingSystem creates it
            PlayerKillTrackerComponent killTracker = commandBuffer.getComponent(killerRef, PlayerKillTrackerComponent.TYPE);
            if (killTracker == null) {
                // Component doesn't exist yet - KillTrackingSystem will create it
                // This system runs AFTER KillTrackingSystem so this shouldn't happen
                if (ConditionalSpawnConfig.DEBUG_MODE) {
                    LOGGER.atInfo().log("[GBR] No kill tracker found - KillTrackingSystem should run first");
                }
                return;
            }
            
            // Make final reference for lambda
            final PlayerKillTrackerComponent finalKillTracker = killTracker;
            
            // Get current game time
            long currentTime = getCurrentGameTime(store);
            
            // Get threshold and current kill count
            int threshold = ConditionalSpawnConfig.getThreshold(npcRole);
            int killCount = finalKillTracker.getKillCount(npcRole, currentTime);
            
            if (ConditionalSpawnConfig.DEBUG_MODE) {
                LOGGER.atInfo().log("[GBR] Kill count: " + killCount + ", threshold: " + threshold);
            }
            
            if (killCount < threshold) {
                return; // Below threshold
            }
            
            // Calculate spawn chance
            double spawnChance = ConditionalSpawnConfig.calculateSpawnChance(killCount, threshold);
            double roll = random.nextDouble();
            
            if (ConditionalSpawnConfig.DEBUG_MODE) {
                LOGGER.atInfo().log("[GBR] Spawn check: " + killCount + " kills, " + (spawnChance * 100) + "% chance, rolled " + (roll * 100) + "%");
            }
            
            if (roll > spawnChance) {
                LOGGER.atInfo().log("[GBR] Elite spawn failed roll (" + (roll * 100) + "% > " + (spawnChance * 100) + "%)");
                return; // Failed spawn chance
            }
            
            // Get elite variant to spawn
            String eliteVariant = ConditionalSpawnConfig.getEliteVariant(npcRole);
            if (eliteVariant == null) {
                return;
            }
            
            // Get spawn location near player
            TransformComponent playerTransform = commandBuffer.getComponent(killerRef, TransformComponent.getComponentType());
            if (playerTransform == null) {
                return;
            }
            
            Vector3d spawnLocation = calculateSpawnLocation(playerTransform.getPosition());
            
            // Spawn the elite NPC using NPCPlugin API
            World world = ((EntityStore)store.getExternalData()).getWorld();
            if (world != null) {
                try {
                    // Create default rotation (facing player's direction or default)
                    Vector3f spawnRotation = new Vector3f(0.0f, 0.0f, 0.0f);
                    
                    // Attempt to spawn the elite NPC
                    world.execute(() -> {
                        try {
                            Pair<Ref<EntityStore>, INonPlayerCharacter> result = NPCPlugin.get().spawnNPC(
                                world.getEntityStore().getStore(),
                                eliteVariant,
                                null, // groupType
                                spawnLocation,
                                spawnRotation
                            );
                            
                            if (result != null) {
                                LOGGER.atInfo().log("[GBR] Successfully spawned elite " + eliteVariant + " at " + spawnLocation);
                                
                                // Send notification to player
                                String displayName = eliteVariant.replace("_", " ");
                                NotificationUtil.sendNotification(
                                    playerRef.getPacketHandler(),
                                    Message.raw("Elite Variant Spawned").bold(true).color("#FF4444"),
                                    Message.raw(displayName + " has appeared nearby!").color("#FFAA00"),
                                    eliteVariant  // Icon - use the elite's model ID
                                );
                            } else {
                                LOGGER.atWarning().log("[GBR] Failed to spawn elite " + eliteVariant + " - spawn returned null");
                            }
                        } catch (Exception e) {
                            LOGGER.atSevere().log("[GBR] Error spawning elite NPC: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                    
                } catch (Exception e) {
                    LOGGER.atSevere().log("[GBR] Error preparing elite spawn: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
        } catch (Exception e) {
            LOGGER.atSevere().log("[GBR] Error attempting elite spawn: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Vector3d calculateSpawnLocation(Vector3d playerPosition) {
        // Random angle
        double angle = random.nextDouble() * 2 * Math.PI;
        
        // Random distance between min and max
        double distance = ConditionalSpawnConfig.SPAWN_DISTANCE_MIN + 
                         (random.nextDouble() * (ConditionalSpawnConfig.SPAWN_DISTANCE_MAX - ConditionalSpawnConfig.SPAWN_DISTANCE_MIN));
        
        // Calculate offset
        double offsetX = Math.cos(angle) * distance;
        double offsetZ = Math.sin(angle) * distance;
        
        return new Vector3d(
            playerPosition.getX() + offsetX,
            playerPosition.getY(),
            playerPosition.getZ() + offsetZ
        );
    }
    
    private long getCurrentGameTime(Store<EntityStore> store) {
        try {
            WorldTimeResource timeResource = store.getResource(WorldTimeResource.getResourceType());
            if (timeResource != null) {
                return timeResource.getGameTime().toEpochMilli() / 50; // Convert to ticks
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("[GBR] Could not get game time: " + e.getMessage());
        }
        return System.currentTimeMillis() / 50; // Fallback
    }
}
