package com.kdrgold.gbr;

import javax.annotation.Nonnull;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.kdrgold.gbr.components.tracking.PlayerKillTrackerComponent;
import com.kdrgold.gbr.config.ConditionalSpawnConfig;
import com.kdrgold.gbr.systems.EliteSpawnSystem;
import com.kdrgold.gbr.systems.KillTrackingSystem;

/**
 * Main plugin class for Gold's Beasts Rebalance
 * 
 * Features:
 * - Rebalanced creature stats and combat behavior
 * - Enhanced loot tables
 * - Conditional elite spawning system
 * - Progressive difficulty scaling
 * 
 * SPAWN SYSTEM CONFIGURATION (72-hour window):
 * - Kill 5 wolves → 60% chance Battle Scarred Wolf spawns
 * - Kill 6+ wolves → 100% guaranteed Battle Scarred Wolf spawn
 * - Kill 5 grizzly bears → 60% chance Battle Hardened Bear spawns
 * - Kill 6+ grizzly bears → 100% guaranteed Battle Hardened Bear spawn
 * - Kill 2 battle hardened bears → 60% chance Adventurer Bear spawns
 * - Kill 3+ battle hardened bears → 100% guaranteed Adventurer Bear spawn
 * - Kill 3 Toad Rhino Magma → 60% chance Umbra Toad spawns
 * - Kill 4+ Toad Rhino Magma → 100% guaranteed Umbra Toad spawn
 * - Kill 2 Rex Cave → 60% chance Umbra Rex spawns (with Bleed effect)
 * - Kill 3+ Rex Cave → 100% guaranteed Umbra Rex spawn
 * 
 * @author kdrgold
 * @version 2.0.0
 */
public class GBRPlugin extends JavaPlugin {
    
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static GBRPlugin INSTANCE;
    
    public GBRPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        INSTANCE = this;
        LOGGER.atInfo().log("===========================================");
        LOGGER.atInfo().log("Gold's Beasts Rebalance v2.0.0");
        LOGGER.atInfo().log("Initializing plugin...");
        LOGGER.atInfo().log("===========================================");
    }
    
    @Override
    protected void setup() {
        LOGGER.atInfo().log("[GBR] Starting plugin setup...");
        
        // Register conditional spawn components if feature is enabled
        if (ConditionalSpawnConfig.ENABLE_CONDITIONAL_SPAWNING) {
            LOGGER.atInfo().log("[GBR] Registering components...");
            
            // Register PlayerKillTrackerComponent
            PlayerKillTrackerComponent.TYPE = getEntityStoreRegistry().registerComponent(
                PlayerKillTrackerComponent.class,
                "gbr:kill_tracker",
                PlayerKillTrackerComponent.CODEC
            );
            
            LOGGER.atInfo().log("[GBR] Components registered successfully");
            
            // Register the kill tracking and elite spawn systems
            try {
                // Register kill tracking system
                KillTrackingSystem killTrackingSystem = new KillTrackingSystem();
                getEntityStoreRegistry().registerSystem(killTrackingSystem);
                LOGGER.atInfo().log("[GBR] Registered KillTrackingSystem");
                
                // Register elite spawn system
                EliteSpawnSystem eliteSpawnSystem = new EliteSpawnSystem();
                getEntityStoreRegistry().registerSystem(eliteSpawnSystem);
                LOGGER.atInfo().log("[GBR] Registered EliteSpawnSystem");
                
                LOGGER.atInfo().log("[GBR] Conditional spawning systems active!");
                LOGGER.atInfo().log("[GBR] - 5 kills in 72h → 60% elite spawn chance");
                LOGGER.atInfo().log("[GBR] - 6+ kills → 100% guaranteed spawn");
                LOGGER.atInfo().log("[GBR] - Wolves → Battle Scarred Wolf");
                LOGGER.atInfo().log("[GBR] - Grizzly Bears → Battle Hardened Bear");
                LOGGER.atInfo().log("[GBR] - Battle Hardened Bears (2 kills) → Adventurer Bear");
                LOGGER.atInfo().log("[GBR] - Toad Rhino Magma (3 kills) → Umbra Toad");
                LOGGER.atInfo().log("[GBR] - Rex Cave (2 kills) → Umbra Rex (Bleed attack)");
            } catch (Exception e) {
                LOGGER.atSevere().log("[GBR] Error registering systems: " + e.getMessage());
                e.printStackTrace();
            }
            
            if (ConditionalSpawnConfig.DEBUG_MODE) {
                LOGGER.atWarning().log("[GBR] DEBUG MODE ENABLED - Verbose logging active");
            }
        }
        
        LOGGER.atInfo().log("===========================================");
        LOGGER.atInfo().log("Gold's Beasts Rebalance fully loaded!");
        LOGGER.atInfo().log("Elite spawn systems ready!");
        LOGGER.atInfo().log("===========================================");
    }
}
