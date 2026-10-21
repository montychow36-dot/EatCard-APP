# 16-Bit GBA / MOTHER 3 Restaurant Card Collector & Social Duel

An authentic 16-bit GBA / MOTHER 3-style restaurant card collection and social dining duel Android app built with native Jetpack Compose, stepped pixel polygon borders, offline Room persistence, and zero technical debt.

### User Review & Critical Decisions

> [!IMPORTANT]
> The architectural direction and specifications confirmed in Phase 1 are locked as follows:
> - **Platform & UI Engine**: 100% Native Jetpack Compose using custom 16-bit stepped pixel border shapes (`GenericShape` polygon clipping), MOTHER 3 dual-bevel layered shadows, and crisp pixelated styling.
> - **Data Persistence**: Offline-first Room Database (`RestaurantCardEntity`, `FoodMasterProfileEntity`, `FoodDuelRecordEntity`) with reactive `Flow` observation for instant performance without network reliance.
> - **Card Art & Input**: Zero-permission Android Photo Picker (`ActivityResultContracts.PickVisualMedia`) with manual input for dishes, prices, signature items, flaws ("伏位"), star ratings, reviews, and active/closed ("追憶墓碑卡") status.

---

### 1. Overview & Core Concept

- **Core Mission 1: Restaurant Card Collection & Binder Management (餐廳記錄與卡牌收集)**  
  Every dining experience is converted into a retro 16-bit trading card with comprehensive RPG stats: Price, Signature Dish ("最出色"), Flaws/Traps ("伏位"), Star Rating, and Food Review. Cards are organized in an authentic 3x3 dense binder grid, with support for filtering, searching, and turning permanently closed restaurants into greyscale "Memorial Tombstones" (追憶墓碑卡).

- **Core Mission 2: Dining Duel & Decision Maker (社交決鬥與去邊度食決策)**  
  Solves dining indecision through interactive 16-bit card battles (comparing Attack/Taste, Defense/Flaw-resistance, Value, and Rarity) or generating a one-click formatted WhatsApp link (`whatsapp://send?text=...`) containing chosen restaurant cards, allowing friends to quickly vote and decide where to eat.

- **Ancient Map Scroll & Food Master ID (古代地圖卷軸與食家身分證)**  
  - **Ancient Map Scroll**: Wood-spindle and parchment parchment interface presenting Hong Kong's 18 districts, highlighting visited districts, restaurant heat levels, and culinary conquest progress.
  - **Food Master ID Card**: A pixelated trainer-license ID card displaying player avatar, culinary title (e.g., "Novice Foodie", "Street Food Veteran", "Michelin Slayer"), total cards forged, and conquest completion rate.

---

### 2. User Experience & Visual Design

#### Key User Flows

1. **Card Binder & Exploration (Tab 1)**:
   - Top pixelated search bar with district and tag filters (Open / Closed Tombstones / Rating).
   - 3x3 dense retro card grid with stepped pixel borders and dual-tone bevels.
   - Tap any card to open the **Full Retro Card Inspection Dialog** showing detailed stats, photo, review notes, and duel rating.
   - Floating Action Button `[+] Forge Card` (鍛造新食卡) launches the card creation flow.

2. **Card Forging (`[+] Forge Card`)**:
   - Step 1: Pick food photo via zero-permission Android Photo Picker.
   - Step 2: Enter Restaurant Name, District (from HK 18 districts), Signature Dish ("最出色"), Flaws/Traps ("伏位"), Price Range (HK$), and Star Rating (1–5).
   - Step 3: Toggle status (Active restaurant or Closed "Memorial Tombstone").
   - Step 4: Real-time preview of the GBA card with calculated Battle Power (戰鬥力) before saving directly to Room.

3. **Dining Duel & WhatsApp Share**:
   - Select 2 or more cards for a 16-bit "Dining Battle" (食界對決) comparing Taste Score vs Value vs Hazard Rating.
   - One-tap "WhatsApp Decision Generator" creates a formatted message:  
     `⚔️【今日開餐決鬥！】朋友仔，由食卡怪獸決定去邊度食！候選餐廳：1. [餐廳A] (招牌: xxx) 2. [餐廳B] (招牌: xxx) ...`

4. **Ancient Map Scroll (Tab 2)**:
   - Horizontal scrolling ancient parchment map with pixelated roller spindles.
   - 18 Districts categorized by Hong Kong Island, Kowloon, and New Territories.
   - Heat indicators display the count of collected restaurant cards per district with discovery badges.

5. **Food Master ID (Tab 3)**:
   - Top: Horizontal retro ID badge with custom pixel avatar, Level, Title, Card Count, and District Conquest %.
   - Bottom: Pixel-styled management actions (Export JSON backup, Reset demo data, Help & Game Manual).

#### Visual Identity & 16-Bit MOTHER 3 Design System

- **Stepped Pixel Polygon Borders**: Custom Jetpack Compose `Shape` implementation (`PixelStepShape`) simulating `clip-path: polygon()` with 4dp/8dp hard stair-step cutoffs at corners—zero modern smooth rounded borders.
- **MOTHER 3 Dual Bevel & Inset Shadows**: Layered canvas drawing with 100% crisp, unblurred borders (Dark Outer Rim `#1E1B18`, Pure White/Cream Inner Highlight `#FBF8EF`, and Rich Indigo/Burgundy Bevel Fill).
- **Retro Color Tokens**:
  - Primary Game Background: `#2C2621` (Warm Dark Retro Slate) & `#EEDEB0` (Parchment Tan)
  - Card Window Surface: `#F7F2DF` (Vintage Game Boy CRT Ivory)
  - Accent Gold: `#E6A817`
  - Tombstone Ash: `#6E6D6B` (Greyscale with Memorial Ribbon `#B22222`)
  - HP / Attack / Value Accents: `#E55B3C` (Fire Red), `#3CAFE5` (Water Blue), `#5BE53C` (Grass Green)
- **Typography & Icons**: Crisp pixel/monospaced fonts with uppercase headers, high-contrast labels, and 16-bit retro icon glyphs.

---

### 3. Key Product Decisions & Trade-Offs

- **Native Jetpack Compose with Custom Pixel Shapes vs WebView SPA**:
  - *Chosen*: 100% Native Jetpack Compose utilizing custom `Path` stepped pixel geometry.
  - *Rationale*: Guarantees 60/120 FPS buttery smooth scrolling, native camera/photo picker integration, instant cold startup, zero webview overhead, and robust offline Android lifecycle management.
- **Room Database vs Cloud Storage**:
  - *Chosen*: Room with SQLite on local storage.
  - *Rationale*: In accordance with the user's preference, ensures absolute privacy, instantaneous load times, zero login barriers, and completely offline card deck management.
- **Memorial Tombstone Mechanism (追憶墓碑卡)**:
  - When a restaurant is marked as closed, its card adopts a desaturated greyscale pixel palette with a RIP header band and reduced duel resistance, honoring culinary nostalgia.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        MainActivity (Compose Host)                     │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
       ┌────────────────────────────┼────────────────────────────┐
       ▼                            ▼                            ▼
┌──────────────┐             ┌──────────────┐             ┌──────────────┐
│  Tab 1:      │             │  Tab 2:      │             │  Tab 3:      │
│  Card Binder │             │  Ancient Map │             │  Food Master │
│  (3x3 Grid & │             │  Scroll (18  │             │  ID Profile  │
│  Forge FAB)  │             │  Districts)  │             │  & Settings  │
└──────┬───────┘             └──────┬───────┘             └──────┬───────┘
       │                            │                            │
       └────────────────────────────┼────────────────────────────┘
                                    ▼
                     ┌─────────────────────────────┐
                     │    FoodCardViewModel        │
                     │  (StateFlow<FoodCardState>) │
                     └──────────────┬──────────────┘
                                    │
                     ┌──────────────▼──────────────┐
                     │    FoodCardRepository       │
                     └──────────────┬──────────────┘
                                    │
                     ┌──────────────▼──────────────┐
                     │  AppDatabase (Room SQLite)  │
                     │  - RestaurantCardEntity     │
                     │  - FoodMasterProfileEntity  │
                     └─────────────────────────────┘
```

#### Room Entities

```kotlin
@Entity(tableName = "restaurant_cards")
data class RestaurantCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val district: String,              // e.g., "油尖旺", "中西區", "灣仔"
    val signatureDish: String,         // 最出色招牌菜
    val flawTrap: String,              // 伏位 / 注意事項
    val priceAvg: Int,                 // 人均消費 HK$
    val ratingStars: Int,              // 1 to 5
    val tastingNotes: String,          // 食評隨筆
    val photoUri: String?,             // 本地相片路徑
    val isClosedMemorial: Boolean = false, // 追憶墓碑卡 (灰階)
    val battleTastePower: Int = 85,    // 自動依評分與招牌運算出的數值
    val battleFlawDef: Int = 70,       // 伏位防禦力
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "food_master_profile")
data class FoodMasterProfileEntity(
    @PrimaryKey val id: Int = 1,
    val masterName: String = "赤子食客",
    val title: String = "街坊美食勇者",
    val avatarStyle: String = "chef_hero"
)
```

#### Key Components & Modules

1. **`ui/pixel/PixelShapes.kt` & `PixelBevel.kt`**:
   - `PixelStepShape(stepSizeDp: Dp)`: Mathematical polygon path generator creating 90-degree stair-stepped corners.
   - `Modifier.pixelBevel(...)`: Custom draw-behind modifier rendering authentic MOTHER 3 bevel borders with inner highlights.
2. **`ui/binder/CardBinderScreen.kt`**:
   - 3x3 dense grid layout with smooth scrolling, filter chips, card search, and `ForgeCardDialog`.
3. **`ui/binder/CardDetailDialog.kt`**:
   - Inspect single card with retro RPG stats, full photo, tombstone badge, edit/delete options, and "Add to Duel" action.
4. **`ui/duel/DuelBattleScreen.kt`**:
   - Stat comparison between selected cards and instant WhatsApp share URL launcher.
5. **`ui/map/AncientMapScrollScreen.kt`**:
   - Parchment paper scroll visualization displaying all 18 districts with restaurant counts and discovery status.
6. **`ui/profile/FoodMasterIdScreen.kt`**:
   - 16-bit Food Master ID card with custom title, total cards count, and HK exploration rate.
