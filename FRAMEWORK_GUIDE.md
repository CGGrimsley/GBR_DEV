# Gold's Beasts Rebalance - Framework Development Guide

> **For Mod Developers:** Learn how to use GBR as a framework for building your own conditional spawning systems, player tracking, and dynamic difficulty mechanics.

---

## Table of Contents

1. [Framework Overview](#framework-overview)
2. [Core Systems](#core-systems)
3. [Getting Started](#getting-started)
4. [Building Your Own Tracking System](#building-your-own-tracking-system)
5. [Custom Conditional Spawning](#custom-conditional-spawning)
6. [Asset Pack Structure](#asset-pack-structure)
7. [Configuration & Customization](#configuration--customization)
8. [Best Practices](#best-practices)
9. [Advanced Examples](#advanced-examples)
10. [API Reference](#api-reference)

---

## Framework Overview

Gold's Beasts Rebalance provides a **reusable framework** for creating conditional gameplay mechanics based on player actions. The core systems can be adapted for:

- **Kill-based progression systems**
- **Dynamic difficulty scaling**
- **Player behavior tracking**
- **Time-windowed event triggers**
- **Elite/boss spawn mechanics**
- **Achievement and milestone systems**

### Key Features

✅ **Persistent Player Tracking** - Track player actions with automatic cleanup  
✅ **Time-Window Filtering** - Events tracked within configurable time windows  
✅ **ECS-Based Architecture** - Built on Hytale's Entity Component System  
✅ **Probability-Based Spawning** - Flexible spawn chance calculations  
✅ **Modular Configuration** - All settings centralized and easily modifiable  
✅ **Debug-Friendly** - Built-in logging and testing modes

---

## Core Systems

### 1. **PlayerKillTrackerComponent**
**Purpose:** Persistent storage of player actions with timestamps

**Key Methods:**
```java
void recordKill(String entityType, long currentTime)
int getKillCount(String entityType, long currentTime)
boolean hasEnoughKills(String entityType, int threshold, long currentTime)
void resetKills(String entityType)
```

**Use Cases:**
- Track any player action (kills, harvests, crafts, etc.)
- Maintain time-windowed event history
- Query recent player behavior
- Trigger events based on action counts

### 2. **KillTrackingSystem**
**Purpose:** Monitor events and update player tracking components

**Architecture:**
- Extends `DeathSystems.OnDeathSystem`
- Filters for relevant entity types
- Creates/updates PlayerKillTrackerComponent
- Logs milestones and thresholds

**Customization Points:**
- Change trigger events (deaths, harvests, interactions)
- Filter for different entity types
- Add custom logging/notifications
- Integrate with other systems

### 3. **EliteSpawnSystem**
**Purpose:** Execute conditional spawning based on tracked data

**Features:**
- Probability-based spawn mechanics
- Cooldown management
- Location calculation
- Player notifications
- Configurable spawn rules

**Customization Points:**
- Different spawn conditions
- Custom spawn chance formulas
- Multiple difficulty tiers
- Loot table integration

### 4. **ConditionalSpawnConfig**
**Purpose:** Centralized configuration hub

**Configuration Categories:**
- Entity type definitions
- Kill thresholds
- Time windows
- Spawn chances and distances
- Status effect parameters
- Debug settings

---

## Getting Started

### Step 1: Clone & Setup

```bash
# Clone the repository
git clone https://github.com/CGGrimsley/GBR_DEV.git
cd GBR_DEV

# Build to verify setup
mvn clean package
```

### Step 2: Update Dependencies

Edit `pom.xml` line 38 to point to your local `HytaleServer.jar`:

```xml
<systemPath>YOUR_PATH_HERE\HytaleServer.jar</systemPath>
```

### Step 3: Verify Build Outputs

After `mvn clean package`, you should see:
```
target/
├── golds-beasts-rebalance-2.0.1.jar          (Plugin)
└── golds-beasts-rebalance-assetpack-2.0.1.zip (Assets)
```

---

## Building Your Own Tracking System

### Real Example 1: Multiple NPC Type Tracking

Extend GBR to track different creature types separately with their own thresholds.

#### 1. Extend the Tracker Component

The existing `PlayerKillTrackerComponent` already supports multiple entity types! You just need to configure new trackable entities:

```java
package com.yourmod.config;

/**
 * Configuration for extended creature tracking and elite spawning.
 * This follows GBR's pattern but adds support for additional creature types.
 * 
 * IMPORTANT: Entity type strings MUST match exactly with NPC role names in your Server/NPC/Roles/ files
 */
public class ExtendedSpawnConfig {
    
    // ==================== ENTITY TYPE DEFINITIONS ====================
    // These strings must match the NPC role names defined in your Server assets
    // Example: "Deer_Common" corresponds to Server/NPC/Roles/Creature/Mammal/Deer_Common.json
    
    /**
     * Base entity type names - these are the NPCs that players will hunt
     * When a player kills these, it increments their kill counter
     */
    public static final String ENTITY_DEER = "Deer_Common";
    public static final String ENTITY_BOAR = "Boar_Common";
    public static final String ENTITY_RABBIT = "Rabbit_Common";
    
    /**
     * Elite variant names - these are the powerful versions that spawn conditionally
     * Must have corresponding NPC role files and model definitions in your assets
     */
    public static final String ENTITY_ALPHA_DEER = "Deer_Alpha";
    public static final String ENTITY_DIRE_BOAR = "Boar_Dire";
    public static final String ENTITY_VICIOUS_RABBIT = "Rabbit_Vicious";
    
    // ==================== SPAWN THRESHOLDS ====================
    // Number of base entity kills required before elite variants can spawn
    // Higher numbers = more challenging to trigger, lower = more frequent elites
    
    /**
     * Kill 15 common deer to unlock Alpha Deer spawns
     * At exactly 15 kills = 60% spawn chance
     * At 16+ kills = 100% guaranteed spawn
     */
    public static final int ALPHA_DEER_THRESHOLD = 15;
    
    /**
     * Kill 10 common boars to unlock Dire Boar spawns
     */
    public static final int DIRE_BOAR_THRESHOLD = 10;
    
    /**
     * Kill 20 rabbits to unlock Vicious Rabbit spawns
     * Higher threshold makes rabbits more challenging to hunt
     */
    public static final int VICIOUS_RABBIT_THRESHOLD = 20;
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Checks if an entity type should be tracked for conditional spawning.
     * Only entities that return true here will have their kills counted.
     * 
     * @param entityType The NPC role name to check
     * @return true if this entity type triggers elite spawns
     */
    public static boolean isTrackableEntity(String entityType) {
        return entityType.equals(ENTITY_DEER) 
            || entityType.equals(ENTITY_BOAR)
            || entityType.equals(ENTITY_RABBIT);
    }
    
    /**
     * Maps base entity types to their elite variants.
     * This is called when spawn conditions are met to determine what to spawn.
     * 
     * @param baseEntityType The NPC role that was killed
     * @return The elite variant role name, or null if no elite exists
     */
    public static String getEliteVariant(String baseEntityType) {
        switch (baseEntityType) {
            case ENTITY_DEER: return ENTITY_ALPHA_DEER;
            case ENTITY_BOAR: return ENTITY_DIRE_BOAR;
            case ENTITY_RABBIT: return ENTITY_VICIOUS_RABBIT;
            default: return null; // Not a trackable entity
        }
    }
    
    /**
     * Gets the kill threshold for a specific entity type.
     * This determines how many kills are needed before elite spawns become possible.
     * 
     * @param baseEntityType The entity type to check
     * @return The kill count threshold, or 0 if entity is not trackable
     */
    public static int getThreshold(String baseEntityType) {
        switch (baseEntityType) {
            case ENTITY_DEER: return ALPHA_DEER_THRESHOLD;
            case ENTITY_BOAR: return DIRE_BOAR_THRESHOLD;
            case ENTITY_RABBIT: return VICIOUS_RABBIT_THRESHOLD;
            default: return 0; // No threshold for non-trackable entities
        }
    }
}
```

#### 2. Create Extended Tracking System

Copy GBR's `KillTrackingSystem` but use your own config:

```java
package com.yourmod.systems;

import javax.annotation.Nonnull;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.*;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.kdrgold.gbr.components.tracking.PlayerKillTrackerComponent;
import com.yourmod.config.ExtendedSpawnConfig;

/**
 * Extended Kill Tracking System - Monitors additional creature types
 * 
 * This system extends GBR's kill tracking to support more creature types.
 * It listens for NPC death events and records them to the player's kill tracker.
 * 
 * KEY FEATURES:
 * - Extends DeathSystems.OnDeathSystem to receive death events from Hytale's ECS
 * - Filters for specific trackable NPCs defined in ExtendedSpawnConfig
 * - REUSES GBR's PlayerKillTrackerComponent (no need to create a new component!)
 * - Automatically cleans up old kills outside the time window
 * - Logs milestone events for debugging
 * 
 * HOW IT WORKS:
 * 1. Any entity death triggers onComponentAdded()
 * 2. System checks if dead entity is a trackable NPC
 * 3. Verifies the killer is a player
 * 4. Gets or creates PlayerKillTrackerComponent on the player
 * 5. Records the kill with timestamp
 * 6. Old kills automatically expire after 72 hours (configurable)
 * 
 * @author yourname
 * @version 1.0
 */
public class ExtendedKillTrackingSystem extends DeathSystems.OnDeathSystem {
    
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    
    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
    
    /**
     * Called automatically by Hytale's ECS when any entity dies.
     * This is where we filter for trackable NPCs and record player kills.
     * 
     * @param deadEntityRef Reference to the entity that died
     * @param deathComponent The death component containing death information
     * @param store Read-only access to the entity store
     * @param commandBuffer Write access to add/modify components
     */
    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> deadEntityRef,
                                 @Nonnull DeathComponent deathComponent,
                                 @Nonnull Store<EntityStore> store,
                                 @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        try {
            // STEP 1: Verify the dead entity is an NPC (not a player, item, projectile, etc.)
            NPCEntity npcComponent = store.getComponent(deadEntityRef, NPCEntity.getComponentType());
            if (npcComponent == null) return; // Not an NPC, ignore
            
            // STEP 2: Check if this NPC type is one we want to track
            // getRoleName() returns the NPC role from Server/NPC/Roles/ files
            String npcRole = npcComponent.getRoleName();
            if (!ExtendedSpawnConfig.isTrackableEntity(npcRole)) return; // Not trackable, ignore
            
            // STEP 3: Get the entity that killed this NPC
            Damage deathInfo = deathComponent.getDeathInfo();
            if (!(deathInfo.getSource() instanceof Damage.EntitySource)) return; // Environmental death, ignore
            
            // Extract the killer's entity reference
            Ref<EntityStore> killerRef = ((Damage.EntitySource) deathInfo.getSource()).getRef();
            if (killerRef == null || !killerRef.isValid()) return; // Invalid killer, ignore
            
            // STEP 4: Verify the killer is a player (not another NPC, turret, etc.)
            Player playerComponent = commandBuffer.getComponent(killerRef, Player.getComponentType());
            if (playerComponent == null) return; // Not a player kill, ignore
            
            // STEP 5: Get or create the kill tracker component on the player
            // KEY: We REUSE GBR's PlayerKillTrackerComponent - no need to create a new one!
            // This component stores kill timestamps in a map, automatically cleaned by time window
            PlayerKillTrackerComponent killTracker = commandBuffer.getComponent(killerRef, PlayerKillTrackerComponent.TYPE);
            if (killTracker == null) {
                // First kill ever for this player - create the tracker
                killTracker = new PlayerKillTrackerComponent();
                commandBuffer.addComponent(killerRef, PlayerKillTrackerComponent.TYPE, killTracker);
            }
            
            // STEP 6: Record the kill with timestamp
            // Game time is in ticks (20 ticks = 1 second)
            // The tracker will automatically clean up kills older than 72 hours
            long currentTime = getCurrentGameTime(store);
            killTracker.recordKill(npcRole, currentTime);
            
            // STEP 7: Log for debugging and admin monitoring
            // getKillCount() returns only kills within the time window (72 hours)
            LOGGER.atInfo().log("[YourMod] Player killed " + npcRole + 
                " (total in window: " + killTracker.getKillCount(npcRole, currentTime) + ")");
            
        } catch (Exception e) {
            // Always wrap in try-catch to prevent one error from breaking the entire system
            LOGGER.atSevere().log("[YourMod] Error tracking kill: " + e.getMessage());
        }
    }
    
    /**
     * Gets the current in-game time in ticks.
     * Uses Hytale's WorldTimeResource for accurate game time (not real-world time).
     * 
     * WHY USE GAME TIME?
     * - Game time continues when server is running
     * - Pauses when server stops (preserves time windows)
     * - 1 tick = 50ms, 20 ticks = 1 second
     * - Independent of server performance/lag
     * 
     * @param store The entity store to get time resource from
     * @return Current game time in ticks, or system time as fallback
     */
    private long getCurrentGameTime(Store<EntityStore> store) {
        try {
            // Get the world's time resource (tracks game time, day/night cycles, etc.)
            WorldTimeResource timeResource = store.getResource(WorldTimeResource.getResourceType());
            if (timeResource != null) {
                // Convert epoch milliseconds to ticks (1 tick = 50ms)
                return timeResource.getGameTime().toEpochMilli() / 50;
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("[YourMod] Could not get game time, using system time");
        }
        // Fallback to system time if game time unavailable (shouldn't normally happen)
        return System.currentTimeMillis() / 50;
    }
}
```

#### 3. Create Extended Spawn System

Copy GBR's `EliteSpawnSystem` pattern:

```java
package com.yourmod.systems;

import java.util.Random;
import javax.annotation.Nonnull;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.vector.*;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.*;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.kdrgold.gbr.components.tracking.PlayerKillTrackerComponent;
import com.yourmod.config.ExtendedSpawnConfig;
import it.unimi.dsi.fastutil.Pair;

/**
 * Spawns elite variants of extended creature types
 */
public class ExtendedEliteSpawnSystem extends DeathSystems.OnDeathSystem {
    
    private final Random random = new Random();
    
    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
    
    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> deadEntityRef,
                                 @Nonnull DeathComponent deathComponent,
                                 @Nonnull Store<EntityStore> store,
                                 @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        try {
            // Check for trackable NPC
            NPCEntity npcComponent = store.getComponent(deadEntityRef, NPCEntity.getComponentType());
            if (npcComponent == null) return;
            
            String npcRole = npcComponent.getRoleName();
            if (!ExtendedSpawnConfig.isTrackableEntity(npcRole)) return;
            
            // Get killer player
            Damage deathInfo = deathComponent.getDeathInfo();
            if (!(deathInfo.getSource() instanceof Damage.EntitySource)) return;
            
            Ref<EntityStore> killerRef = ((Damage.EntitySource) deathInfo.getSource()).getRef();
            if (killerRef == null || !killerRef.isValid()) return;
            
            Player playerComponent = store.getComponent(killerRef, Player.getComponentType());
            PlayerRef playerRef = store.getComponent(killerRef, PlayerRef.getComponentType());
            if (playerComponent == null || playerRef == null) return;
            
            // Get kill tracker
            PlayerKillTrackerComponent killTracker = commandBuffer.getComponent(killerRef, PlayerKillTrackerComponent.TYPE);
            if (killTracker == null) return;
            
            // Check threshold
            long currentTime = System.currentTimeMillis() / 50;
            int threshold = ExtendedSpawnConfig.getThreshold(npcRole);
            int killCount = killTracker.getKillCount(npcRole, currentTime);
            
            if (killCount < threshold) return;
            
            // Calculate spawn chance (60% at threshold, 100% above)
            double spawnChance = (killCount == threshold) ? 0.60 : 1.0;
            if (random.nextDouble() > spawnChance) return;
            
            // Get elite variant
            String eliteVariant = ExtendedSpawnConfig.getEliteVariant(npcRole);
            if (eliteVariant == null) return;
            
            // Get spawn location
            TransformComponent playerTransform = commandBuffer.getComponent(killerRef, TransformComponent.getComponentType());
            if (playerTransform == null) return;
            
            Vector3d spawnLocation = calculateSpawnLocation(playerTransform.getPosition());
            
            // Spawn elite NPC
            World world = ((EntityStore)store.getExternalData()).getWorld();
            if (world != null) {
                world.execute(() -> {
                    Pair<Ref<EntityStore>, com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter> result = 
                        NPCPlugin.get().spawnNPC(
                            world.getEntityStore().getStore(),
                            eliteVariant,
                            null,
                            spawnLocation,
                            new Vector3f(0, 0, 0)
                        );
                    
                    if (result != null) {
                        NotificationUtil.sendNotification(
                            playerRef.getPacketHandler(),
                            Message.raw("Elite Spawned!").bold(true).color("#FF4444"),
                            Message.raw(eliteVariant.replace("_", " ")).color("#FFAA00"),
                            eliteVariant
                        );
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private Vector3d calculateSpawnLocation(Vector3d playerPosition) {
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = 10 + (random.nextDouble() * 10); // 10-20 blocks
        return new Vector3d(
            playerPosition.getX() + Math.cos(angle) * distance,
            playerPosition.getY(),
            playerPosition.getZ() + Math.sin(angle) * distance
        );
    }
}
```

#### 4. Register Your Systems

```java
// In your plugin's setup() method

// Register extended tracking system
ExtendedKillTrackingSystem extendedTracking = new ExtendedKillTrackingSystem();
getEntityStoreRegistry().registerSystem(extendedTracking);

// Register extended spawn system
ExtendedEliteSpawnSystem extendedSpawning = new ExtendedEliteSpawnSystem();
getEntityStoreRegistry().registerSystem(extendedSpawning);

LOGGER.atInfo().log("[YourMod] Extended tracking active for Deer, Boar, and Rabbit!");
```

---

### Real Example 2: Player Damage Tracking

Track cumulative damage dealt to bosses to spawn reinforcements.

#### 1. Create Damage Tracker Component

```java
package com.yourmod.components;

import javax.annotation.Nonnull;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NPC Damage Tracker Component - Tracks cumulative damage dealt to boss NPCs
 * 
 * This component is attached to NPC entities (not players!) to track how much
 * total damage they've received. Used for boss mechanics like spawning reinforcements
 * at specific health thresholds.
 * 
 * KEY FEATURES:
 * - Accumulates all damage dealt to the NPC over its lifetime
 * - Tracks which thresholds have already been triggered (prevents duplicate spawns)
 * - Thread-safe using ConcurrentHashMap
 * - Persists across server saves (if codec is properly configured)
 * 
 * DIFFERENCE FROM KILL TRACKING:
 * - Kill tracking is on PLAYERS, tracks kills they made
 * - Damage tracking is on NPCs, tracks damage they received
 * 
 * TYPICAL USE CASES:
 * - Boss phases (spawn minions at 75%, 50%, 25% health)
 * - Environmental effects (building collapses when damaged enough)
 * - Dynamic difficulty (boss gets stronger as it takes more damage)
 * - Achievement tracking (deal X damage to a boss)
 * 
 * @author yourname
 * @version 1.0
 */
public class NPCDamageTrackerComponent implements Component<EntityStore> {
    
    // Component type registration - set during plugin initialization
    public static ComponentType<EntityStore, NPCDamageTrackerComponent> TYPE;
    
    /**
     * Codec for component serialization/deserialization.
     * This allows the component to persist across server restarts.
     * BuilderCodec uses reflection to serialize fields automatically.
     */
    public static final BuilderCodec<NPCDamageTrackerComponent> CODEC = BuilderCodec
            .builder(NPCDamageTrackerComponent.class, NPCDamageTrackerComponent::new)
            .build();
    
    /**
     * Cumulative damage dealt to this NPC since it spawned.
     * Includes all damage types (physical, fire, magic, etc.)
     */
    private float totalDamage = 0.0f;
    
    /**
     * Tracks which damage thresholds have already triggered effects.
     * Key = threshold value (e.g., 250, 500, 750)
     * Value = true if triggered, false/absent if not
     * 
     * This prevents spawning reinforcements multiple times at the same threshold.
     * ConcurrentHashMap is thread-safe for multi-threaded server environments.
     */
    private final Map<Integer, Boolean> triggeredThresholds = new ConcurrentHashMap<>();
    
    public NPCDamageTrackerComponent() {}
    
    public void addDamage(float amount) {
        totalDamage += amount;
    }
    
    public float getTotalDamage() {
        return totalDamage;
    }
    
    public boolean hasTriggeredThreshold(int threshold) {
        return triggeredThresholds.getOrDefault(threshold, false);
    }
    
    public void markThresholdTriggered(int threshold) {
        triggeredThresholds.put(threshold, true);
    }
    
    @Nonnull
    @Override
    public NPCDamageTrackerComponent clone() {
        NPCDamageTrackerComponent clone = new NPCDamageTrackerComponent();
        clone.totalDamage = this.totalDamage;
        clone.triggeredThresholds.putAll(this.triggeredThresholds);
        return clone;
    }
}
```

#### 2. Create Damage Tracking System

```java
package com.yourmod.systems;

import javax.annotation.Nonnull;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.*;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.*;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.yourmod.components.NPCDamageTrackerComponent;
import it.unimi.dsi.fastutil.Pair;

/**
 * Boss Reinforcement System - Spawns minions when boss reaches damage thresholds
 * 
 * This system demonstrates tracking cumulative damage to NPCs and triggering
 * events at specific thresholds. A common boss mechanic in many games.
 * 
 * HOW IT WORKS:
 * 1. Extends DamageSystems.OnDamageSystem to receive ALL damage events
 * 2. Filters for specific boss NPC type
 * 3. Accumulates damage in NPCDamageTrackerComponent
 * 4. When thresholds are reached, spawns reinforcement minions
 * 5. Tracks triggered thresholds to prevent duplicate spawns
 * 
 * EXAMPLE BOSS FIGHT:
 * - Boss has 1000 HP
 * - At 250 damage (75% HP remaining) → spawn 2 minions
 * - At 500 damage (50% HP remaining) → spawn 3 minions  
 * - At 750 damage (25% HP remaining) → spawn 4 minions
 * 
 * KEY DIFFERENCES FROM KILL TRACKING:
 * - Tracks damage to NPCs (not kills by players)
 * - Component attached to NPC entities (not player entities)
 * - Cumulative total (doesn't reset or have time windows)
 * - Uses DamageSystems instead of DeathSystems
 * 
 * @author yourname
 * @version 1.0
 */
public class BossReinforcementSystem extends DamageSystems.OnDamageSystem {
    
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    
    // ==================== CONFIGURATION ====================
    
    /**
     * The boss NPC role name to track.
     * Must match exactly with Server/NPC/Roles/[...]/Boss_Dragon.json
     */
    private static final String BOSS_TYPE = "Boss_Dragon";
    
    /**
     * The minion NPC role to spawn as reinforcements.
     * Must exist in your NPC roles and have proper assets.
     */
    private static final String MINION_TYPE = "Dragon_Whelp";
    
    /**
     * Damage thresholds for spawning reinforcements.
     * Example: For a 1000 HP boss, these represent 25%, 50%, 75% damage dealt.
     * 
     * FORMULA: threshold = maxHealth * (percentage / 100)
     * - 250 damage = 25% of 1000 HP → Boss at 75% health
     * - 500 damage = 50% of 1000 HP → Boss at 50% health  
     * - 750 damage = 75% of 1000 HP → Boss at 25% health
     * 
     * You can adjust these based on your boss's actual max health.
     */
    private static final int[] THRESHOLDS = {250, 500, 750};
    
    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
    
    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> damagedEntityRef,
                                 @Nonnull DamageComponent damageComponent,
                                 @Nonnull Store<EntityStore> store,
                                 @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        try {
            // Check if damaged entity is our tracked boss
            NPCEntity npcComponent = store.getComponent(damagedEntityRef, NPCEntity.getComponentType());
            if (npcComponent == null) return;
            
            String npcRole = npcComponent.getRoleName();
            if (!BOSS_TYPE.equals(npcRole)) return;
            
            // Get or create damage tracker
            NPCDamageTrackerComponent damageTracker = commandBuffer.getComponent(damagedEntityRef, NPCDamageTrackerComponent.TYPE);
            if (damageTracker == null) {
                damageTracker = new NPCDamageTrackerComponent();
                commandBuffer.addComponent(damagedEntityRef, NPCDamageTrackerComponent.TYPE, damageTracker);
            }
            
            // Add damage from this hit
            Damage damage = damageComponent.getDamage();
            float damageAmount = damage.getAmount();
            damageTracker.addDamage(damageAmount);
            
            float totalDamage = damageTracker.getTotalDamage();
            LOGGER.atInfo().log("[BossReinforcements] Boss took " + damageAmount + " damage (total: " + totalDamage + ")");
            
            // Check each threshold
            for (int threshold : THRESHOLDS) {
                if (totalDamage >= threshold && !damageTracker.hasTriggeredThreshold(threshold)) {
                    damageTracker.markThresholdTriggered(threshold);
                    spawnReinforcements(damagedEntityRef, store, commandBuffer, threshold);
                }
            }
            
        } catch (Exception e) {
            LOGGER.atSevere().log("[BossReinforcements] Error tracking damage: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void spawnReinforcements(Ref<EntityStore> bossRef, Store<EntityStore> store, 
                                    CommandBuffer<EntityStore> commandBuffer, int threshold) {
        try {
            // Get boss position
            TransformComponent bossTransform = commandBuffer.getComponent(bossRef, TransformComponent.getComponentType());
            if (bossTransform == null) return;
            
            Vector3d bossPosition = bossTransform.getPosition();
            World world = ((EntityStore)store.getExternalData()).getWorld();
            if (world == null) return;
            
            // Spawn 2-4 minions in a circle around the boss
            int minionCount = 2 + (threshold / 250); // 2 at 25%, 3 at 50%, 4 at 75%
            
            LOGGER.atInfo().log("[BossReinforcements] Spawning " + minionCount + " minions at " + threshold + " damage threshold!");
            
            for (int i = 0; i < minionCount; i++) {
                double angle = (2 * Math.PI * i) / minionCount;
                double distance = 5.0; // 5 blocks from boss
                
                Vector3d spawnPos = new Vector3d(
                    bossPosition.getX() + Math.cos(angle) * distance,
                    bossPosition.getY(),
                    bossPosition.getZ() + Math.sin(angle) * distance
                );
                
                world.execute(() -> {
                    Pair<Ref<EntityStore>, com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter> result = 
                        NPCPlugin.get().spawnNPC(
                            world.getEntityStore().getStore(),
                            MINION_TYPE,
                            null,
                            spawnPos,
                            new Vector3f(0, 0, 0)
                        );
                    
                    if (result != null) {
                        LOGGER.atInfo().log("[BossReinforcements] Spawned minion at " + spawnPos);
                    }
                });
            }
            
        } catch (Exception e) {
            LOGGER.atSevere().log("[BossReinforcements] Failed to spawn reinforcements: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

#### 3. Register the System

```java
// In your plugin's setup() method

// Register damage tracker component
NPCDamageTrackerComponent.TYPE = getEntityStoreRegistry().registerComponent(
    NPCDamageTrackerComponent.class,
    "yourmod:npc_damage_tracker",
    NPCDamageTrackerComponent.CODEC
);

// Register reinforcement system
BossReinforcementSystem reinforcementSystem = new BossReinforcementSystem();
getEntityStoreRegistry().registerSystem(reinforcementSystem);

LOGGER.atInfo().log("[YourMod] Boss reinforcement system active!");
```

**How it works:**
- Tracks damage dealt to specific boss NPCs
- At 25%, 50%, and 75% damage thresholds, spawns minions
- Each threshold only triggers once (tracked via `triggeredThresholds` map)
- Minions spawn in a circle around the boss
- Uses `DamageSystems.OnDamageSystem` to listen to all damage events

---

## Custom Conditional Spawning

### Spawn Chance Formulas

The framework supports flexible spawn probability calculations:

```java
// Linear scaling
public static double linearSpawnChance(int count, int threshold, int max) {
    if (count < threshold) return 0.0;
    if (count >= max) return 1.0;
    return (double)(count - threshold) / (max - threshold);
}

// Exponential scaling
public static double exponentialSpawnChance(int count, int threshold) {
    if (count < threshold) return 0.0;
    return Math.min(1.0, Math.pow(1.5, count - threshold) * 0.1);
}

// Step-based scaling (GBR default)
public static double stepSpawnChance(int count, int threshold) {
    if (count < threshold) return 0.0;
    if (count == threshold) return 0.60;
    return 1.0; // Guaranteed above threshold
}
```

### Multi-Tier Difficulty System

Implement progressive difficulty scaling:

```java
public class DifficultyConfig {
    // Tier 1: Common Elite (5 kills)
    public static final int TIER_1_THRESHOLD = 5;
    public static final String TIER_1_VARIANT = "Elite_Common";
    
    // Tier 2: Rare Elite (10 kills)
    public static final int TIER_2_THRESHOLD = 10;
    public static final String TIER_2_VARIANT = "Elite_Rare";
    
    // Tier 3: Legendary Elite (20 kills)
    public static final int TIER_3_THRESHOLD = 20;
    public static final String TIER_3_VARIANT = "Elite_Legendary";
    
    public static String getEliteTier(int killCount) {
        if (killCount >= TIER_3_THRESHOLD) return TIER_3_VARIANT;
        if (killCount >= TIER_2_THRESHOLD) return TIER_2_VARIANT;
        if (killCount >= TIER_1_THRESHOLD) return TIER_1_VARIANT;
        return null;
    }
}
```

### Custom Spawn Location Logic

```java
private Vector3d calculateCustomSpawnLocation(Player player, World world) {
    Vector3d playerPos = getPlayerPosition(player);
    
    // Option 1: Random circle around player
    double angle = random.nextDouble() * 2 * Math.PI;
    double distance = MIN_DISTANCE + random.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
    return new Vector3d(
        playerPos.getX() + Math.cos(angle) * distance,
        playerPos.getY(),
        playerPos.getZ() + Math.sin(angle) * distance
    );
    
    // Option 2: Find elevated position (dramatic entrance)
    Vector3d highGround = findNearestHighGround(playerPos, world, 20);
    return highGround != null ? highGround : playerPos;
    
    // Option 3: Behind player (ambush mechanic)
    Vector3f lookDir = player.getLookDirection();
    return playerPos.subtract(lookDir.getX() * 15, 0, lookDir.getZ() * 15);
}
```

---

## Asset Pack Structure

### Separate Asset Distribution

GBR uses a **dual-file distribution model** for better compatibility:

```
Distribution Files:
├── golds-beasts-rebalance-2.0.1.jar          (Plugin code)
└── golds-beasts-rebalance-assetpack-2.0.1.zip (Assets)
```

### Asset Organization

```
Asset Pack Structure:
├── manifest.json                    # Asset pack metadata
├── Common/                          # Client-side assets
│   ├── Icons/                      # Entity icons
│   │   └── ModelsGenerated/
│   │       ├── Battle_Hardened_Bear.png
│   │       ├── Battle_Scarred_Wolf.png
│   │       └── Bear_Adventurer.png
│   ├── NPC/                        # NPC models & textures
│   │   └── Beast/
│   │       ├── Battle_Hardened_Bear/
│   │       │   └── Models/
│   │       │       ├── Battlebear.blockymodel
│   │       │       └── Model_Textures/
│   │       │           └── Battlebear_texture.png
│   │       ├── Battle_Scarred_Wolf/
│   │       │   └── Models/
│   │       │       ├── Model.blockymodel
│   │       │       └── Model_Textures/
│   │       │           └── texture.png
│   │       └── Bear_Adventurer/
│   │           ├── Model.blockymodel
│   │           └── Model_Textures/
│   └── UI/                         # UI assets
│       └── StatusEffects/
│           └── Bleed.png
└── Server/                         # Server-side data
    ├── Drops/                      # Loot tables
    │   └── NPCs/
    │       └── Beast/
    │           ├── Drop_Battle_Hardened_Bear.json
    │           ├── Drop_Battle_Scarred_Wolf.json
    │           ├── Drop_Bear_Adventurer.json
    │           ├── Drop_Bear_Grizzly.json
    │           ├── Drop_Toad_Rhino_Magma.json
    │           └── Drop_Toad_Rhino.json
    ├── Entity/
    │   └── Effects/
    │       └── Status/
    │           └── Bleed.json
    ├── Models/                     # Entity model definitions
    │   ├── Battle_Hardened_Bear.json
    │   ├── Battle_Scarred_Wolf.json
    │   └── Bear_Adventurer.json
    └── NPC/                        # Spawning & behavior
        ├── Flocks/
        │   ├── Pack_Small
        │   └── Pack_Small.json
        ├── Roles/
        │   └── Creature/
        │       ├── Mammal/
        │       │   ├── Battle_Hardened_Bear.json
        │       │   ├── Battle_Scarred_Wolf.json
        │       │   ├── Bear_Adventurer.json
        │       │   ├── Bear_Grizzly.Json
        │       │   └── Wolf_Black.Json
        │       └── Reptile/
        │           ├── Toad_Rhino_Magma.json
        │           └── Toad_Rhino.Json
        └── Spawn/
            ├── Markers/
            │   ├── Battle_Hardened_Bear.json
            │   ├── Battle_Scarred_Wolf.json
            │   └── Bear_Adventurer.json
            └── World/
                ├── Zone1/
                │   ├── Spawns_Zone1_Forests_Predator.json
                │   ├── Spawns_Zone1_Mountains_Predator.json
                │   └── Spawns_Zone1_Plains_Predator.json
                └── Zone3/
                    └── Spawns_Zone3_Forests_Predator.json
```

### Build Configuration

The `pom.xml` uses maven-assembly-plugin to create the asset pack:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-assembly-plugin</artifactId>
    <version>3.6.0</version>
    <executions>
        <execution>
            <id>create-asset-pack</id>
            <phase>package</phase>
            <goals>
                <goal>single</goal>
            </goals>
            <configuration>
                <descriptors>
                    <descriptor>src/assembly/assetpack.xml</descriptor>
                </descriptors>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Asset Assembly Descriptor

`src/assembly/assetpack.xml`:

```xml
<assembly>
    <id>assetpack</id>
    <formats>
        <format>zip</format>
    </formats>
    <includeBaseDirectory>false</includeBaseDirectory>
    <fileSets>
        <fileSet>
            <directory>${project.basedir}</directory>
            <outputDirectory>/</outputDirectory>
            <includes>
                <include>manifest.json</include>
            </includes>
        </fileSet>
        <fileSet>
            <directory>${project.basedir}/Common</directory>
            <outputDirectory>Common</outputDirectory>
        </fileSet>
        <fileSet>
            <directory>${project.basedir}/Server</directory>
            <outputDirectory>Server</outputDirectory>
        </fileSet>
    </fileSets>
</assembly>
```

### Real Asset Examples

#### Status Effect Definition
`Server/Entity/Effects/Status/Bleed.json`:

```json
{
  "ApplicationEffects": {
    "EntityBottomTint": "#200000",
    "EntityTopTint": "#8B0000",
    "EntityAnimationId": "Hurt",
    "WorldSoundEventId": "SFX_Unarmed_Impact",
    "LocalSoundEventId": "SFX_Unarmed_Impact",
    "Particles": [
      {
        "SystemId": "Impact_Sword_Basic"
      }
    ]
  },
  "DamageCalculatorCooldown": 2,
  "DamageCalculator": {
    "BaseDamage": {
      "Physical": 5
    }
  },
  "DamageEffects": {
    "WorldSoundEventId": "SFX_Unarmed_Impact",
    "PlayerSoundEventId": "SFX_Unarmed_Impact"
  },
  "OverlapBehavior": "Extend",
  "RemovalBehavior": "Duration",
  "Infinite": false,
  "Debuff": true,
  "StatusEffectIcon": "UI/StatusEffects/Bleed.png",
  "Duration": 10
}
```

#### NPC Role Definition
`Server/NPC/Roles/Creature/Mammal/Battle_Hardened_Bear.json` (excerpt):

```json
{
  "Type": "Variant",
  "Reference": "Template_Predator",
  "Parameters": {
    "Appearance": {
      "Value": "Battle_Hardened_Bear",
      "Description": "Model to be used."
    },
    "DropList": {
      "Value": "Drop_Battle_Hardened_Bear",
      "Description": "Drop Items."
    },
    "MaxHealth": {
      "Value": 100,
      "Description": "Max Health."
    }
  },
  "Modify": {
    "MaxHealth": 500,
    "MaxSpeed": 8,
    "ViewRange": 8,
    "HearingRange": 12,
    "CombatBehaviorDistance": 6.5
  }
}
```

#### Entity Model Definition
`Server/Models/Battle_Hardened_Bear.json` (excerpt):

```json
{
  "Model": "NPC/Beast/Battle_Hardened_Bear/Models/Battlebear.blockymodel",
  "Texture": "NPC/Beast/Battle_Hardened_Bear/Models/Model_Textures/Battlebear_texture.png",
  "EyeHeight": 1.5,
  "HitBox": {
    "Max": { "X": 0.8, "Y": 1.8, "Z": 0.8 },
    "Min": { "X": -0.8, "Y": 0, "Z": -0.8 }
  },
  "MinScale": 1.2,
  "MaxScale": 1.4
}
```

---

## Configuration & Customization

### Centralized Configuration Pattern

Keep all settings in a single config class:

```java
public class YourModConfig {
    // Master toggles
    public static final boolean ENABLE_SYSTEM = true;
    public static final boolean DEBUG_MODE = false;
    
    // Entity definitions
    public static final String ENTITY_DEER = "Deer_Common";
    public static final String ENTITY_ALPHA_DEER = "Deer_Alpha";
    
    // Thresholds
    public static final int ALPHA_SPAWN_THRESHOLD = 10;
    
    // Time windows
    public static final long TRACKING_WINDOW_TICKS = 3_600_000L; // 50 hours
    public static final long SPAWN_COOLDOWN_TICKS = 6_000L; // 5 minutes
    
    // Spawn mechanics
    public static final double SPAWN_CHANCE_BASE = 0.50;
    public static final double SPAWN_DISTANCE_MIN = 15.0;
    public static final double SPAWN_DISTANCE_MAX = 30.0;
    
    // Helper methods
    public static boolean isTrackableEntity(String entityType) {
        return entityType.equals(ENTITY_DEER);
    }
    
    public static String getEliteVariant(String baseEntity) {
        if (baseEntity.equals(ENTITY_DEER)) return ENTITY_ALPHA_DEER;
        return null;
    }
}
```

### Dynamic Configuration (Advanced)

Load settings from YAML:

```java
// src/main/resources/yourmod_config.yml
spawning:
  enabled: true
  debug: false
  cooldown_ticks: 6000
  
thresholds:
  deer: 10
  wolf: 5
  
spawn_chances:
  at_threshold: 0.60
  guaranteed: 1.0
```

```java
// Load config in plugin setup
YamlConfiguration config = YamlConfiguration.loadConfiguration(
    new File(getDataFolder(), "yourmod_config.yml")
);

boolean enabled = config.getBoolean("spawning.enabled", true);
int deerThreshold = config.getInt("thresholds.deer", 10);
```

---

## Best Practices

### 1. **Use Debug Mode During Development**

```java
public static final boolean DEBUG_MODE = true; // Enable for testing

if (DEBUG_MODE) {
    LOGGER.atInfo().log("[YourMod] Kill count: " + count + ", threshold: " + threshold);
}
```

### 2. **Implement Cooldowns**

Prevent spawn spam with per-player or global cooldowns:

```java
private final Map<UUID, Long> playerCooldowns = new ConcurrentHashMap<>();

private boolean isOnCooldown(UUID playerId, long currentTime) {
    Long lastSpawn = playerCooldowns.get(playerId);
    if (lastSpawn == null) return false;
    return (currentTime - lastSpawn) < SPAWN_COOLDOWN_TICKS;
}

private void setSpawnCooldown(UUID playerId, long currentTime) {
    playerCooldowns.put(playerId, currentTime);
}
```

### 3. **Validate Entity References**

Always check refs before accessing:

```java
if (entityRef == null || !entityRef.isValid()) {
    LOGGER.atWarning().log("[YourMod] Invalid entity reference");
    return;
}
```

### 4. **Clean Up Old Data**

The tracker component auto-cleans, but you can force cleanup:

```java
// In your tracking component
public void cleanupOldEntries(long currentTime, long maxAge) {
    killTimestamps.values().forEach(list -> 
        list.removeIf(time -> (currentTime - time) > maxAge)
    );
}
```

### 5. **Use ECS Patterns**

Follow Hytale's ECS architecture:
- **Components** = Data storage only
- **Systems** = Logic and behavior
- **Resources** = Global/world state

### 6. **Error Handling**

Wrap critical operations:

```java
try {
    // Your spawn logic
    spawnEliteEntity(entityRef, store, commandBuffer);
} catch (Exception e) {
    LOGGER.atSevere().log("[YourMod] Spawn failed: " + e.getMessage());
    e.printStackTrace();
}
```

### 7. **Player Notifications**

Inform players of important events:

```java
NotificationUtil.sendActionBarMessage(
    playerRef,
    Message.text("⚔ Elite Variant Spawned! ⚔").style(TextFormatting.RED)
);
```

---

## Advanced Examples

### Example 1: Multi-Entity Combo System

Spawn special boss when player kills multiple entity types:

```java
public class ComboTrackingComponent implements Component<EntityStore> {
    private Set<String> recentKillTypes = new HashSet<>();
    
    public void addKillType(String type) {
        recentKillTypes.add(type);
    }
    
    public boolean hasCombo(Set<String> required) {
        return recentKillTypes.containsAll(required);
    }
    
    public void resetCombo() {
        recentKillTypes.clear();
    }
}

// In your system
Set<String> requiredCombo = Set.of("Wolf", "Bear", "Boar");
if (comboTracker.hasCombo(requiredCombo)) {
    spawnSpecialBoss();
    comboTracker.resetCombo();
}
```

### Example 2: Dynamic Loot Scaling

Adjust loot based on kill streak:

```java
public class LootScalingSystem {
    
    public static float getLootMultiplier(int killCount, int threshold) {
        if (killCount < threshold) return 1.0f;
        
        // 10% increase per kill above threshold, max 3x
        float bonus = 1.0f + (0.1f * (killCount - threshold));
        return Math.min(bonus, 3.0f);
    }
    
    public static void applyBonusLoot(NPCEntity npc, float multiplier) {
        // Modify loot table or drop quantities
        npc.setLootMultiplier(multiplier);
    }
}
```

### Example 3: Adaptive Difficulty

Spawn harder variants as player progresses:

```java
public enum DifficultyTier {
    NORMAL(0, 1.0f),
    VETERAN(10, 1.5f),
    ELITE(25, 2.0f),
    CHAMPION(50, 3.0f);
    
    private final int killsRequired;
    private final float statMultiplier;
    
    public static DifficultyTier fromKillCount(int kills) {
        for (int i = values().length - 1; i >= 0; i--) {
            if (kills >= values()[i].killsRequired) {
                return values()[i];
            }
        }
        return NORMAL;
    }
}

// Apply tier modifiers
DifficultyTier tier = DifficultyTier.fromKillCount(playerKills);
npc.setHealthMultiplier(tier.statMultiplier);
npc.setDamageMultiplier(tier.statMultiplier);
```

### Example 4: Seasonal Events

Time-based spawn modifications:

```java
public class SeasonalSpawnModifier {
    
    public static boolean isEventActive(long currentTime) {
        // Check for special event periods
        LocalDateTime now = Instant.ofEpochMilli(currentTime * 50)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
        
        // Halloween event (October)
        if (now.getMonthValue() == 10) {
            return true;
        }
        
        return false;
    }
    
    public static double getEventSpawnBonus(long currentTime) {
        return isEventActive(currentTime) ? 2.0 : 1.0;
    }
}
```

---

## API Reference

### PlayerKillTrackerComponent

**Package:** `com.kdrgold.gbr.components.tracking`

| Method | Parameters | Returns | Description |
|--------|-----------|---------|-------------|
| `recordKill` | `String entityType, long currentTime` | `void` | Records a kill with timestamp |
| `getKillCount` | `String entityType, long currentTime` | `int` | Gets valid kills in time window |
| `hasEnoughKills` | `String entityType, int threshold, long currentTime` | `boolean` | Checks if threshold met |
| `resetKills` | `String entityType` | `void` | Clears all kills for entity type |
| `clone` | - | `PlayerKillTrackerComponent` | Creates deep copy |

### ConditionalSpawnConfig

**Package:** `com.kdrgold.gbr.config`

| Constant | Type | Default | Description |
|----------|------|---------|-------------|
| `ENABLE_CONDITIONAL_SPAWNING` | `boolean` | `true` | Master toggle |
| `DEBUG_MODE` | `boolean` | `false` | Verbose logging |
| `ENTITY_WOLF` | `String` | `"Wolf_Black"` | Base wolf entity name |
| `ENTITY_GRIZZLY_BEAR` | `String` | `"Bear_Grizzly"` | Base grizzly bear name |
| `ENTITY_BATTLE_HARDENED_BEAR` | `String` | `"Battle_Hardened_Bear"` | Elite bear name |
| `ENTITY_BATTLE_SCARRED_WOLF` | `String` | `"Battle_Scarred_Wolf"` | Elite wolf name |
| `ENTITY_ADVENTURER_BEAR` | `String` | `"Bear_Adventurer"` | Legendary bear name |
| `BATTLE_SCARRED_WOLF_THRESHOLD` | `int` | `5` | Wolf kills needed |
| `BATTLE_HARDENED_BEAR_THRESHOLD` | `int` | `5` | Grizzly kills needed |
| `ADVENTURER_BEAR_THRESHOLD` | `int` | `2` | Elite bear kills needed |
| `KILL_WINDOW_TICKS` | `long` | `5184000` | 72-hour window |
| `SPAWN_COOLDOWN_TICKS` | `long` | `200` | Spawn cooldown (10s for testing, 6000 for production) |
| `SPAWN_CHANCE_AT_THRESHOLD` | `double` | `0.60` | 60% at threshold |
| `SPAWN_CHANCE_GUARANTEED` | `double` | `1.0` | 100% above threshold |
| `SPAWN_DISTANCE_MIN` | `double` | `10.0` | Min spawn distance |
| `SPAWN_DISTANCE_MAX` | `double` | `20.0` | Max spawn distance |
| `BLEED_DAMAGE_PER_TICK` | `float` | `0.1f` | Bleed damage per tick |
| `BLEED_TICK_INTERVAL` | `float` | `1.0f` | Interval between damage |
| `BLEED_DURATION_TICKS` | `int` | `200` | Total bleed duration |

| Method | Parameters | Returns | Description |
|--------|-----------|---------|-------------|
| `calculateSpawnChance` | `int killCount, int threshold` | `double` | Calculates spawn probability |
| `getEliteVariant` | `String baseEntityType` | `String` | Gets elite variant name |
| `isTrackableEntity` | `String entityType` | `boolean` | Checks if entity tracked |
| `getThreshold` | `String entityType` | `int` | Gets spawn threshold |

### KillTrackingSystem

**Package:** `com.kdrgold.gbr.systems`  
**Extends:** `DeathSystems.OnDeathSystem`

Monitors NPC deaths and updates player kill trackers.

### EliteSpawnSystem

**Package:** `com.kdrgold.gbr.systems`  
**Extends:** `DeathSystems.OnDeathSystem`

Spawns elite variants based on kill thresholds and probabilities.

---

## Testing & Debugging

### Enable Debug Logging

```java
public static final boolean DEBUG_MODE = true;
```

Provides verbose output:
```
[GBR] KillTrackingSystem.onComponentAdded triggered!
[GBR] NPC died with role: Wolf_Black
[GBR] Player killed Wolf_Black (total in 72h window: 5)
[GBR] Player reached threshold for Wolf_Black - elite spawn chance activated!
[GBR] Spawn check: 5 kills, 60.0% chance, rolled 43.2%
[GBR] Elite spawn success! Battle_Scarred_Wolf spawned
```

### Reduce Cooldowns for Testing

```java
// Change from 5 minutes to 10 seconds
public static final long SPAWN_COOLDOWN_TICKS = 200L;
```

### Manual Testing Commands

Create admin commands for testing:

```java
@CommandHandler(name = "testspawn", permission = "yourmod.admin")
public void testSpawnCommand(Player player, String entityType) {
    // Force spawn for testing
    spawnEliteAtPlayer(player, entityType);
}

@CommandHandler(name = "setkills", permission = "yourmod.admin")
public void setKillsCommand(Player player, String entityType, int count) {
    // Set kill count for testing
    PlayerKillTrackerComponent tracker = getTrackerComponent(player);
    for (int i = 0; i < count; i++) {
        tracker.recordKill(entityType, getCurrentTime());
    }
}
```

---

## Support & Contribution

### Getting Help

- **GitHub Issues:** [Report bugs or request features](https://github.com/CGGrimsley/GBR_DEV/issues)
- **Discussions:** [Ask questions about framework usage](https://github.com/CGGrimsley/GBR_DEV/discussions)
- **Source Code:** [Browse the codebase](https://github.com/CGGrimsley/GBR_DEV)

### Contributing to GBR

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes
4. Test thoroughly in-game
5. Commit: `git commit -m 'Add amazing feature'`
6. Push: `git push origin feature/amazing-feature`
7. Open a Pull Request

### Sharing Your Mods

Built something cool with GBR? We'd love to see it!

- Tag your mods with `#GBR-Framework`
- Credit Gold's Beasts Rebalance in your documentation
- Share your mods on CurseForge or Modrinth
- Consider contributing improvements back to GBR

---

## License & Attribution

**Gold's Beasts Rebalance** is licensed under a custom license. See [LICENSE](LICENSE) for details.

When using GBR as a framework:
- ✅ You may use the code patterns and architecture
- ✅ You may extend the systems for your own mods
- ✅ You may distribute your own mods built on this framework
- ✅ Please credit "Gold's Beasts Rebalance" in your mod documentation

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 2.0.1 | Jan 2026 | Split distribution (JAR + asset pack), dependency management |
| 2.0.0 | Jan 2026 | Initial framework release with conditional spawning |

---

**Author:** kdrgold  
**Framework Version:** 2.0.1  
**Hytale API:** v2026.01.17-4b0f30090  
**Java Version:** 17+

**Happy Modding! 🎮**
