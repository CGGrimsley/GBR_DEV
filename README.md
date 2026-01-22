# Gold's Beasts Rebalance

[![GitHub Release](https://img.shields.io/github/v/release/CGGrimsley/GBR_DEV)](https://github.com/CGGrimsley/GBR_DEV/releases)
[![CurseForge - Plugin](https://img.shields.io/badge/CurseForge-Download-orange)](https://www.curseforge.com/hytale/mods/golds-beasts-rebalance)
[![CurseForge - Assets](https://img.shields.io/badge/CurseForge-Download-orange)](https://www.curseforge.com/hytale/mods/golds-beast-rebalance-asset-pack)
[![License: Custom](https://img.shields.io/badge/License-Custom-blue.svg)](LICENSE)

> **For Players:** Download the compiled mod from [CurseForge](https://www.curseforge.com/hytale/mods/golds-beasts-rebalance)  
> **For Developers:** This repository contains the source code for building and contributing to the mod

---

## Description
Overhauls creature stats, combat behavior, spawning, and loot tables for more engaging and rewarding encounters. Adds elite variants with **conditional spawning system**, reduces spam mechanics, and ensures tough fights are worth your time.

**NEW in v2.0:** Elite creatures now spawn dynamically based on your kill count, making encounters progressively more challenging and rewarding!

---

## 🎮 For Players

### Installation

**Download the Plugin from [CurseForge](https://www.curseforge.com/hytale/mods/golds-beasts-rebalance)**
**Download the Asset Pack from [CurseForge](https://www.curseforge.com/hytale/mods/golds-beast-rebalance-asset-pack)**

⚠️ **Important:** You must download and install **BOTH** files:
- `golds-beasts-rebalance-2.0.2.jar` (Plugin)
- `golds-beasts-rebalance-assetpack-2.0.2.zip` (Asset Pack)

#### Dedicated Servers
1. Download **both** the `.jar` and `.zip` files from CurseForge
2. Place both files in `<server-directory>/mods/`
3. Restart the server

#### Singleplayer/Local Worlds
1. Download **both** the `.jar` and `.zip` files from CurseForge
2. Place both files in:
   - **Global:** `<data-path>/Hytale/UserData/Mods`
   - **Per-World:** `<data-path>/Hytale/UserData/Saves/<world-name>/mods`
3. For Global: Right-click your world → Settings → Enable both mods
4. Launch your world

**Data paths:**
- **Windows:** `%APPDATA%`
- **macOS:** `~/Library/Application Support`
- **Linux:** `~/.var/app/com.hypixel.HytaleLauncher/data`

---

## 🔥 Features

### Conditional Elite Spawning System
Elite variants spawn dynamically based on your performance:
- **Kill tracking**: Monitors wolves, grizzly bears, and battle hardened bears over 72 in-game hours
- **Progressive difficulty**: 5 kills → 60% spawn chance | 6+ kills → 100% guaranteed
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

# Output files:
# - target/golds-beasts-rebalance-2.0.2.jar (Plugin)
# - target/golds-beasts-rebalance-assetpack-2.0.2.zip (Asset Pack)
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
**Hytale API:** v2026.01.17-4b0f30090  
**Java Version:** 17+  
**Build System:** Maven  
**Distribution:** Separate plugin JAR + asset pack ZIP  
**License:** See [LICENSE](LICENSE)

### Key Systems
- **PlayerKillTrackerComponent**: Persistent kill tracking per player
- **KillTrackingSystem**: Monitors NPC deaths and updates kill counts
- **EliteSpawnSystem**: Handles conditional elite spawning logic
- **JSON-based status effects**: Bleed effect with custom icon

---

## Support & Links

- **Download Mod:** [CurseForge](https://www.curseforge.com/hytale/mods/golds-beasts-rebalance)
- **Report Issues:** [GitHub Issues](https://github.com/CGGrimsley/GBR_DEV/issues)
- **Source Code:** [GitHub Repository](https://github.com/CGGrimsley/GBR_DEV)

---

## Credits

**Author:** kdrgold  
**Team:** The Gold's Beasts Rebalance Team


