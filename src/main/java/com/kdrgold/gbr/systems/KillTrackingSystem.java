package com.kdrgold.gbr.systems;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.kdrgold.gbr.components.tracking.PlayerKillTrackerComponent;
import com.kdrgold.gbr.config.ConditionalSpawnConfig;

/**
 * System that tracks when players kill trackable NPCs.
 * Uses DeathSystems.OnDeathSystem for proper death event handling.
 * 
 * @author kdrgold
 * @version 2.0.0
 */
public class KillTrackingSystem extends DeathSystems.OnDeathSystem {
    
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    
    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        // Track all entity deaths - we'll filter for NPCs in the handler
        return Archetype.empty();
    }
    
    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> deadEntityRef,
                                 @Nonnull DeathComponent deathComponent,
                                 @Nonnull Store<EntityStore> store,
                                 @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        try {
            if (ConditionalSpawnConfig.DEBUG_MODE) {
                LOGGER.atInfo().log("[GBR] KillTrackingSystem.onComponentAdded triggered!");
            }
            
            // Check if the dead entity is an NPC
            NPCEntity npcComponent = store.getComponent(deadEntityRef, NPCEntity.getComponentType());
            if (npcComponent == null) {
                if (ConditionalSpawnConfig.DEBUG_MODE) {
                    LOGGER.atInfo().log("[GBR] Entity is not an NPC, skipping");
                }
                return; // Not an NPC
            }
            
            // Get NPC role name
            String npcRole = npcComponent.getRoleName();
            if (ConditionalSpawnConfig.DEBUG_MODE) {
                LOGGER.atInfo().log("[GBR] NPC died with role: " + npcRole);
            }
            
            if (npcRole == null || !ConditionalSpawnConfig.isTrackableEntity(npcRole)) {
                if (ConditionalSpawnConfig.DEBUG_MODE) {
                    LOGGER.atInfo().log("[GBR] NPC role '" + npcRole + "' is not trackable, skipping");
                }
                return; // Not a trackable entity type
            }
            
            // Get death info to find killer
            Damage deathInfo = deathComponent.getDeathInfo();
            if (deathInfo == null || !(deathInfo.getSource() instanceof Damage.EntitySource)) {
                return; // No valid killer
            }
            
            Damage.EntitySource source = (Damage.EntitySource) deathInfo.getSource();
            Ref<EntityStore> killerRef = source.getRef();
            
            if (killerRef == null || !killerRef.isValid()) {
                return;
            }
            
            // Check if killer is a player
            Player playerComponent = commandBuffer.getComponent(killerRef, Player.getComponentType());
            if (playerComponent == null) {
                return; // Killer is not a player
            }
            
            // Get or create kill trastore.getComponent(killerRef, Player.getComponentType());
            if (playerComponent == null) {
                return; // Killer is not a player
            }
            
            // Get or create kill tracker component on player
            PlayerKillTrackerComponent killTracker = commandBuffer.getComponent(killerRef, PlayerKillTrackerComponent.TYPE);
            if (killTracker == null) {
                killTracker = new PlayerKillTrackerComponent();
                commandBuffer.addComponent(killerRef, PlayerKillTrackerComponent.TYPE, killTracker);
            }
            
            // Get current game time
            long currentTime = getCurrentGameTime(store);
            
            // Record the kill
            killTracker.recordKill(npcRole, currentTime);
            
            int killCount = killTracker.getKillCount(npcRole, currentTime);
            
            if (ConditionalSpawnConfig.DEBUG_MODE) {
                LOGGER.atInfo().log("[GBR] Player killed " + npcRole + " (total in 72h window: " + killCount + ")");
            }
            
            // Log milestone messages
            int threshold = ConditionalSpawnConfig.getThreshold(npcRole);
            if (killCount == threshold) {
                LOGGER.atInfo().log("[GBR] Player reached threshold for " + npcRole + " - elite spawn chance activated!");
            } else if (killCount > threshold) {
                LOGGER.atInfo().log("[GBR] Player exceeded threshold for " + npcRole + " - guaranteed elite spawn!");
            }
            
        } catch (Exception e) {
            LOGGER.atSevere().log("[GBR] Error tracking kill: " + e.getMessage());
            e.printStackTrace();
        }
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
        return System.currentTimeMillis() / 50; // Fallback to approximate ticks
    }
}
