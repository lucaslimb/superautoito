# Super Auto Ito

> An Android auto-battler card game featuring characters from Junji Ito's horror manga universe.

---

## About the Project

**Super Auto Ito** is a strategic auto-battler game where you assemble a team of iconic Junji Ito characters and watch them battle automatically. Inspired by the auto-battler genre, the game challenges you to think ahead: the order you place your characters, the abilities they carry, and the synergies you build between them will decide whether you win or lose each round.

The game features **32 unique characters** drawn from Junji Ito's works — each with their own attack, defense, and a special ability that triggers at a specific moment in battle. Play solo against a CPU opponent or challenge a friend over a local Wi-Fi network.

---

## Features

- **32 unique characters** — each with distinct stats and special abilities
- **Solo mode** — battle against a CPU opponent with random AI decisions
- **Local multiplayer** — discover and join games on the same Wi-Fi network
- **Strategic deck management** — arrange your 6-card main team and 2-card reserve bench
- **Shop system** — after a defeat, pick a new character card to add to your roster
- **Complex ability system** — buffs, debuffs, invocations, deck manipulation, and more

---

## How It Works

### Game Flow

```
Main Menu
  │
  ├── Solo Mode ────────────────────────────────┐
  │                                             │
  └── Multiplayer Mode                          │
        ├── Host: Create room (NSD)             │
        └── Join: Discover rooms (NSD)          │
                                                │
                     ┌──────────────────────────┘
                     ▼
         32 characters are shuffled.
         Each player receives 8 cards.
                     │
                     ▼
           Team Setup Screen
           • Arrange your 6 main cards
           • Place 2 cards in reserve
           • (After a loss) Visit the shop
                     │
                     ▼
            Battle Screen
           • Pre-battle abilities fire
           • Characters fight automatically
           • Death triggers resolve
           • Round winner is declared
                     │
                     ▼
         Repeat until max rounds or
         one team is fully eliminated.
```

### Battle Engine

Each round is divided into three phases:

1. **Pre-Battle Phase** — Characters with a `BEFORE_BATTLE` trigger activate their abilities before any combat begins.
2. **Combat Phase** — The front character of each team attacks the other. ATK is applied against the opposing character's DEF. If a character is killed, `ON_KILL` abilities fire on the attacker.
3. **Death Resolution** — Dead characters trigger `ON_DEATH` abilities before being removed from the board. This can invoke new characters or modify surviving allies.

The loop repeats until one team has no characters left. The team with survivors wins the round.

### Character Ability System

Each character has a **trigger**, a **power type**, a **target**, and a **stat amount**:

| Trigger | Description |
|---|---|
| `BEFORE_BATTLE` | Activates at the start of the round |
| `ON_KILL` | Activates when this character kills an opponent |
| `ON_DEATH` | Activates when this character dies |

| Power Type | Description |
|---|---|
| `BUFF_ATA` / `BUFF_DEF` | Increase an ally's stats |
| `DEBUFF_ATA` / `DEBUFF_DEF` | Reduce an enemy's stats |
| `INVOKE` | Summon a token or copy a character onto the board |
| `DECK_CHANGE` | Swap stats, ban a character, or reorder the deck |
| `GENERAL` | Unique special effects (e.g., steal defense, copy powers) |

Targets can be `SELF`, `ALLY`, `ENEMY`, `ALL`, `RANDOM`, or type-specific (`CHILD`, `SUPERNATURAL`, `CORPSE`, `FEM`, `MASC`).

### Multiplayer Networking

Multiplayer uses **Android NSD (Network Service Discovery)** for automatic room discovery on local Wi-Fi — no manual IP addresses needed.

- The **host** registers an NSD service (`_superautoito._tcp`) and opens a TCP server socket.
- **Clients** browse for available services and connect to the selected room.
- Character data is exchanged via **JSON over TCP sockets** (serialised with Gson).
- All battle logic runs locally on each device.

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin 1.9.24 |
| **Platform** | Android (min SDK 27 / Android 8.1+, target SDK 36) |
| **Build system** | Gradle 8.9.1 with Kotlin DSL |
| **UI framework** | Android Views + ConstraintLayout |
| **Design system** | Material Design 3 |
| **Async** | Kotlin Coroutines (`kotlinx-coroutines-android` 1.7.3) |
| **Serialisation** | Gson 2.10.1 |
| **Networking** | Android NsdManager + Java TCP Sockets |
| **Testing** | JUnit 4, AndroidX Espresso |

### Key Dependencies

```toml
[versions]
agp                = "8.9.1"
kotlin             = "1.9.24"
coreKtx            = "1.17.0"
appcompat          = "1.7.1"
material           = "1.13.0"
activity           = "1.11.0"
constraintlayout   = "2.2.1"
coroutines         = "1.7.3"
gson               = "2.10.1"
```

---

## Project Structure

```
superautoito/
├── app/src/main/
│   ├── java/lucaslimb/com/github/superautoito/
│   │   ├── MainActivity.kt          # Main menu, room creation & discovery
│   │   ├── engine/
│   │   │   ├── BattleEngine.kt      # Battle phase orchestration
│   │   │   ├── AbilityExecutor.kt   # Ability resolution logic
│   │   │   └── AbilityContext.kt    # Data class for ability execution
│   │   ├── model/
│   │   │   ├── Character.kt         # 32 character definitions & stats
│   │   │   ├── GameState.kt         # Round and player state tracking
│   │   │   └── Player.kt            # Player data wrapper
│   │   ├── network/
│   │   │   └── NetworkManager.kt    # NSD registration, discovery & sockets
│   │   └── screens/
│   │       ├── BattleActivity.kt    # Battle UI and game loop
│   │       ├── TeamSetupActivity.kt # Deck builder and shop
│   │       └── GameTipsActivity.kt  # In-game tutorial
│   └── res/                         # Layouts, drawables, strings, themes
├── gradle/libs.versions.toml        # Dependency version catalog
├── build.gradle.kts                 # Root Gradle configuration
└── app/build.gradle.kts             # App-level Gradle configuration
```

---

## Getting Started

### Requirements

- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 27+
- A device or emulator running Android 8.1 (Oreo) or higher

### Build & Run

```bash
# Clone the repository
git clone https://github.com/lucaslimb/superautoito.git
cd superautoito

# Open in Android Studio, or build from the command line:
./gradlew assembleDebug

# Install on a connected device
./gradlew installDebug
```

### Multiplayer

Both devices must be connected to the **same Wi-Fi network**. One player creates a room (host) and the other discovers and joins it — no configuration required.

---

## Author

**Lucas Limb** — [github.com/lucaslimb](https://github.com/lucaslimb) · lucasdelimabzr@gmail.com
