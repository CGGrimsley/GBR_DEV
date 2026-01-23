# Gold's Beasts Rebalance v2.1.0 - Release Notes

[![GitHub Release](https://img.shields.io/github/v/release/CGGrimsley/GBR_DEV)](https://github.com/CGGrimsley/GBR_DEV/releases)
[![CurseForge - Plugin](https://img.shields.io/badge/CurseForge-Download-orange)](https://www.curseforge.com/hytale/mods/golds-beasts-rebalance)
[![CurseForge - Assets](https://img.shields.io/badge/CurseForge-Download-orange)](https://www.curseforge.com/hytale/mods/golds-beast-rebalance-asset-pack)
[![License: Custom](https://img.shields.io/badge/License-Custom-blue.svg)](LICENSE)

> **For Players:** Download the compiled mod from [CurseForge](https://www.curseforge.com/hytale/mods/golds-beasts-rebalance)  
> **For Developers:** This repository contains the source code for building and contributing to the mod

---

## Description
Overhauls creature stats, combat behavior, spawning, and loot tables for more engaging and rewarding encounters. Adds elite variants with **conditional spawning system**, reduces spam mechanics, and ensures tough fights are worth your time.

**NEW in v2.1.0:** Two new elite reptile variants - Umbra Rex and Umbra Toad! Elite spawns now include deadly cave dwellers with enhanced loot!  
**NEW in v2.0.2:** Modular pack system! Choose to install just the base assets, or add the optional rebalance pack for stat changes!  
**NEW in v2.0.1:** Proper Asset pack and plugin generation when compiled!  
**NEW in v2.0:** Elite creatures now spawn dynamically based on your kill count, making encounters progressively more challenging and rewarding!

---

## 🎮 For Players

### Installation

**Download from [CurseForge](https://www.curseforge.com/hytale/mods/golds-beasts-rebalance)**

⚠️ **Required Files:**
- `golds-beasts-v2.1.0.jar` (Plugin - Required)
- `golds-beasts-asset-pack-v2.1.0.zip` (Assets - Required)

📦 **Optional:**
- `golds-beasts-rebalanced-v2.1.0.zip` (Rebalance Pack - Optional)

The **Rebalance Pack** contains stat changes, spawn adjustments, and elite variants. Install it if you want the full rebalanced experience!

#### Dedicated Servers
1. Download the `.jar` and `.zip` file(s) from CurseForge
2. Place all files in `<server-directory>/mods/`
3. Restart the server

#### Singleplayer/Local Worlds
1. Download the `.jar` and `.zip` file(s) from CurseForge
2. Place all files in:
   - **Global:** `<data-path>/Hytale/UserData/Mods`
   - **Per-World:** `<data-path>/Hytale/UserData/Saves/<world-name>/mods`
3. For Global: Right-click your world → Settings → Enable the mods
4. Launch your world

**Data paths:**
- **Windows:** `%APPDATA%`
- **macOS:** `~/Library/Application Support`
- **Linux:** `~/.var/app/com.hypixel.HytaleLauncher/data`

---

## 🔥 Features

### Conditional Elite Spawning System
Elite variants spawn dynamically based on your performance:
- **Kill tracking**: Monitors wolves, grizzly bears, battle hardened bears, magma rhino toads, and cave rexes over 72 in-game hours
- **Progressive difficulty**: Kill thresholds trigger elite spawns (60%/100% spawn rates based on kill count)
- **Smart spawning**: Elites spawn 10-20 blocks away from kill location
- **Rewarding gameplay**: More kills = tougher encounters with better loot

### Elite Variants

#### 🐺 Battle Scarred Wolf
**Spawns after killing 5-6 wolves**
- Increased health
- **Bleed attack**: 25 damage over 10 seconds (5 dmg/tick)
- Custom bleed status icon
- Enhanced loot drops
- Solo hunter

#### 🐻 Battle Hardened Bear
**Spawns after killing 5-6 grizzly bears**
- Increased health and damage
- Wears scavenged helmet
- Increased loot
- Aggressive combat behavior

#### ⚔️ Bear Adventurer  
**Spawns after killing 2-3 Battle Hardened Bears**
- Ultimate elite tier
- Wears full scavenged armor
- Highest stats of all bear variants
- Rare spawn with Increased loot

#### 🦎 Umbra Toad
**Spawns after killing 3-4 Magma Rhino Toads**
- 350 health (vs 160 for standard Magma Rhino Toad)
- Enhanced combat stats with increased aggression range
- Custom dark-themed model and texture
- Enhanced loot drops including ore drops
- Fire immunity retained from base variant

#### 🦖 Umbra Rex
**Spawns after killing 2-3 Cave Rexes**
- 1000 health - ultimate predator tier
- **Bleed attack**: Inflicts bleeding damage over time
- Massive 125 base physical damage per hit
- Extended view range (20 blocks) and combat persistence
- Premium loot including ingredient bars up to Adamantite:
  - Iron, Gold, Silver, Thorium, Cobalt, and Adamantite bars
  - 5-8 Heavy Hide
  - 6-8 Raw Wildmeat
- Custom dark Rex model with increased size (1.4-1.5x scale)

---

## BALANCE CHANGES

### Rhino Toads (Both Variants)

**Combat:**
- Attack cooldown: 0.53s → 1.6s
- Tongue Attack Buffer: 0.33s → 1s
- Damage: 35 → 25
- Knockback force: 14 → 10
- Vertical knockback: 1 → 2

**Stats:**
- Health: 124 → 160
- Speed: 8 → 9
- View range: 12 → 10
- Hearing range: 8 → 7
- Flee range: 15 → 12
- Combat distance: 9 → 8

### Magma Rhino Toad

**Guaranteed Drops:**
- Raw Wildmeat: 2-4 (was 1-2)
- Medium Hide: 2-4 (was 1-2)

**Bonus Drops:**
- Bone Fragments: 50% chance for 1-2
- Copper Ore: 20% chance for 1
- Iron Ore: 12% chance for 1
- Silver Ore: 4% chance for 1
- Gold Ore: 1% chance for 1

### Grizzly Bear

**Stats:**
- Health: 124 → 150
- Chase range: 11 → 8

**Loot:**
- Heavy Hide: 1-3 (was 1-2)
- Raw Wildmeat: 3-4 (was 2-3)

### Black Wolf

**Stats:**
- Health: 103 → 80

**Spawning:**
- Pack size: 2-3 → 3-4 wolves
- Now spawns in Mountain biomes
- Now spawns in Forest biomes

---

## Compatibility
May conflict with mods that modify the same creatures, loot tables, spawning systems, or NPC behavior.

---

## 🛠️ For Developers

### Building from Source

**Requirements:**
- Java 17+
- Maven 3.6+
- Hytale Server API (included via system dependency)

**Build commands:**
```bash
# Clone the repository
git clone https://github.com/CGGrimsley/GBR_DEV.git
cd GBR_DEV

# Clean and build
mvn clean package

# Output files (in target/):
# - golds-beasts-v2.1.0.jar (Plugin)
# - golds-beasts-asset-pack-v2.1.0.zip (Asset Pack)
# - golds-beasts-rebalanced-v2.1.0.zip (Rebalance Pack)
```

### Project Structure
```
├── Common/                    # Client assets (models, textures, UI)
│   ├── Icons/                # Entity icons
│   ├── NPC/                  # NPC models and textures
│   └── UI/                   # Status effect icons
├── Server/                    # Server assets (data-driven)
│   ├── Drops/                # Loot tables
│   ├── Entity/Effects/       # Status effects (Bleed)
│   ├── Models/               # NPC model definitions
│   └── NPC/                  # Roles, spawning, flocks
├── src/main/java/            # Java plugin code
│   └── com/kdrgold/gbr/
│       ├── components/       # Kill tracking, data storage
│       ├── config/           # Configuration constants
│       └── systems/          # Kill tracking, elite spawning
└── src/main/resources/       # Plugin metadata
```

### Configuration

Settings are in [ConditionalSpawnConfig.java](src/main/java/com/kdrgold/gbr/config/ConditionalSpawnConfig.java):
- Kill thresholds (5 for 60%, 6 for 100%)
- Time window (72 hours)
- Entity mappings
- Bleed damage (5/tick, 10s duration)
- Debug mode

See [config.yml](src/main/resources/config.yml) for reference values.

### Contributing

1. Fork this repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Test thoroughly in-game
5. Commit (`git commit -m 'Add amazing feature'`)
6. Push to your fork (`git push origin feature/amazing-feature`)
7. Open a Pull Request

### Development Setup

1. Set up your IDE with Java 17+
2. Update `pom.xml` line 38 to point to your local HytaleServer.jar
3. Run `mvn clean install` to verify setup
4. Make changes and test with `mvn clean package`

---

## Technical Details

**Version:** 2.0.2  
**Version:** 2.0.2  
**Hytale API:** v2026.01.17-4b0f30090  
**Java Version:** 17+  
**Build System:** Maven  
**Distribution:** Plugin JAR + Asset Pack ZIP + Optional Rebalance Pack ZIP  
**License:** See [LICENSE](LICENSE)

### Key Systems
- **PlayerKillTrackerComponent**: Persistent kill tracking per player
- **KillTrackingSystem**: Monitors NPC deaths and updates kill counts
- **EliteSpawnSystem**: Handles conditional elite spawning logic
- **JSON-based status effects**: Bleed effect with custom icon
- **Modular Pack System**: Separates base assets from balance changes for flexibility

---

## Support & Links

- **Download Mod:** [CurseForge](https://www.curseforge.com/hytale/mods/golds-beasts-rebalance)
- **Report Issues:** [GitHub Issues](https://github.com/CGGrimsley/GBR_DEV/issues)
- **Source Code:** [GitHub Repository](https://github.com/CGGrimsley/GBR_DEV)

---

## Credits

**Author:** kdrgold  
**Team:** The Gold's Beasts Rebalance Team


