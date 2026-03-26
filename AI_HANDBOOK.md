# 📖 AI DEVELOPER HANDBOOK: MANDATORY RULES OF ENGAGEMENT
**VoucherVault** (`com.addmrp.vault`)
Read and acknowledge these rules internally before writing a single line of code. Failure to adhere = failed build.

---

## Rule 1: No Placeholders for Core Logic
Do not write `// TODO: Implement logic here` for critical functions like database saving, state management, or UI navigation. Write the actual, functional Kotlin code. Every function must have a real implementation or throw `NotImplementedError` with a clear reason.

---

## Rule 2: Absolute State Safety (UDF Pattern)
UI must be entirely driven by **StateFlow** (Unidirectional Data Flow). Every screen gets:
- A `@HiltViewModel` class with a single `StateFlow<ScreenUiState>`
- All events flow **up** via `ViewModel` functions, state flows **down** via `collectAsStateWithLifecycle()`
- Every UI state data class must handle: **Loading**, **Success**, and **Error** states
- Never allow the app to crash on null data; use default fallbacks
- All `combine` flows must use `SharingStarted.WhileSubscribed(5000)` to prevent memory leaks

### Example Pattern
```kotlin
data class WalletUiState(
    val vouchers: List<Voucher> = emptyList(),
    val totalAssets: Double = 0.0,
    val isLoading: Boolean = true,
    val error: String? = null
)
```

---

## Rule 3: Pixel-Perfect Componentization
Do not build massive, monolithic Composable functions. Every UI element that appears more than once or encapsulates non-trivial logic **must** be a separate, reusable `@Composable` function in the `ui/components/` package.

### Required Components
| Component | File | Purpose |
|-----------|------|---------|
| `GlowingCard` | `GlowingCard.kt` | Glassmorphic card wrapper with configurable glow |
| `VoucherCard` | `VoucherCard.kt` | Premium voucher display with source badges |
| `CategoryChip` | `CategoryChip.kt` | Animated horizontal filter chip |
| `BottomNavBar` | `BottomNavBar.kt` | 4-tab bottom navigation (Wallet/Scan/Concierge/Settings) |

---

## Rule 4: Timezone & Expiry Rigidity
Expiry dates are the most critical data in this app.
- **Storage**: All timestamps stored in **UTC** (epoch millis) in both Room and Firestore
- **Conversion**: Only convert to the user's local timezone (IST) **at the UI layer**
- **Library**: Use `java.time.Instant` and `java.time.Duration` — never `java.util.Date`
- **Countdown**: `Voucher.expiryCountdownText()` must produce human-readable strings ("3D", "12H", "45M", "EXPIRED")

---

## Rule 5: Testability First
Write functions as **pure functions** wherever possible.
- `CalculateTotalAssetsUseCase` must be a pure function: `(List<Voucher>) -> Double`
- Unit tests must cover: empty list, all expired, all redeemed, mixed states
- Use JUnit4 + MockK for testing; never rely on Android instrumentation for business logic tests
- Test files live in `app/src/test/java/com/addmrp/vault/`

---

## Rule 6: The "Fat Finger" Rule
All clickable areas (buttons, cards, chips, icon buttons) must have a minimum touch target of **48.dp**. This is a Material Design accessibility requirement. Use `Modifier.size(48.dp)` on `IconButton` elements and ensure `padding` on chips yields ≥48dp total height.

---

## Rule 7: Obsidian Glass Design System (MANDATORY PALETTE)

> **⚠️ CRITICAL**: All UI work MUST use the token names defined below. Never hardcode hex values in composables. Always reference `VaultPrimary`, `VaultSecondary`, etc.

### Theme Identity
| Property | Value |
|----------|-------|
| **Theme Name** | Obsidian Glass |
| **Mode** | Dark Only (no light theme) |
| **Source File** | `ui/theme/Color.kt` |

### Color Palette

#### Core Axes (from the Obsidian Glass spec)
| Token | Hex | Role |
|-------|-----|------|
| `VaultPrimary` | `#4285F4` | Buttons, active nav tab, main CTA, GPay badge |
| `VaultPrimaryDim` | `#2A5DB8` | Disabled/dimmed primary states |
| `VaultPrimaryGlow` | `#404285F4` | Card glow / shadow halo (25% alpha) |
| `VaultPrimaryLight` | `#7AACFF` | Links, inline highlights |
| `VaultSecondary` | `#5F259F` | PhonePe badge, secondary CTA, purple accents |
| `VaultSecondaryDim` | `#3D1870` | Dimmed secondary |
| `VaultSecondaryGlow` | `#405F259F` | Purple glow on secondary cards |
| `VaultSecondaryLight` | `#9B6DD7` | Lighter purple text |
| `VaultGold` | `#D4AF37` | AI Logic badge, premium tags, Concierge accents |
| `VaultGoldDim` | `#9B7D20` | Dimmed gold |
| `VaultGoldGlow` | `#40D4AF37` | Gold glow for AI recommendation cards |
| `VaultGoldLight` | `#E8D07F` | Light gold for on-tertiary text |

#### Backgrounds (Neutral Axis — base #0A0A0B)
| Token | Hex | Usage |
|-------|-----|-------|
| `VaultBlack` | `#0A0A0B` | Root background, status bar, nav bar |
| `VaultDarkSurface` | `#111114` | App bar, bottom nav background |
| `VaultSurface` | `#19191E` | Input fields, search bars |
| `VaultCardSurface` | `#1E1E26` | Card backgrounds |
| `VaultElevatedSurface` | `#28283A` | Chips, badges, dropdowns, elevated elements |

#### Text Hierarchy
| Token | Hex | Usage |
|-------|-----|-------|
| `VaultTextPrimary` | `#E8E8F0` | Headlines, titles, primary content |
| `VaultTextSecondary` | `#9090A0` | Subtitles, descriptions, labels |
| `VaultTextTertiary` | `#60607A` | Hints, disabled text, metadata |

#### Status Colors
| Token | Hex | Usage |
|-------|-----|-------|
| `VaultGreen` | `#4CAF50` | Active/Success indicators |
| `VaultRed` | `#EF5350` | Error/Expired/Sign-out |
| `VaultOrange` | `#FF9800` | Warning/Expiring soon |

#### Fintech Source Badges
| Token | Hex | Platform |
|-------|-----|----------|
| `VaultGPayBlue` | `#4285F4` | Google Pay (= Primary) |
| `VaultPhonePePurple` | `#5F259F` | PhonePe (= Secondary) |
| `VaultCredMint` | `#2BD9A8` | CRED |

### Usage Rules
1. **Buttons**: Use `VaultPrimary` for main CTA, `VaultGold` for AI/premium actions, `VaultRed.copy(alpha=0.15f)` for destructive actions
2. **Cards**: Always use `GlowingCard` with `VaultPrimaryGlow` for normal cards, `VaultGoldGlow` for AI cards, `VaultRed.copy(alpha=0.3f)` for expiring-soon cards
3. **Chips**: Selected = `VaultPrimary` background with white text; Unselected = `VaultElevatedSurface` with `VaultTextSecondary`
4. **Switches**: Checked track = `VaultPrimary`; Unchecked track = `VaultElevatedSurface`
5. **Source badges**: Use platform-specific colors with `0.2f` alpha backgrounds and `0.3f` alpha borders

### Legacy Aliases
To prevent breaking changes, the following aliases exist in `Color.kt`:
```
VaultNeonBlue    → VaultPrimary
VaultNeonBlueDim → VaultPrimaryDim
VaultNeonBlueGlow → VaultPrimaryGlow
```
When writing **new** code, always use the canonical `VaultPrimary` / `VaultSecondary` / `VaultGold` names.

---

## Rule 8: Architecture Decisions (Immutable)

### Package Structure
```
com.addmrp.vault/
├── data/
│   ├── local/          # Room DB, DAO, Entity
│   ├── mapper/         # Domain ↔ Entity ↔ Firestore converters
│   ├── remote/         # FirestoreDataSource
│   └── repository/     # VoucherRepositoryImpl
├── di/                 # Hilt modules (AppModule, RepositoryModule)
├── domain/
│   ├── model/          # Voucher, VoucherCategory, RedemptionSource
│   ├── repository/     # VoucherRepository interface
│   └── usecase/        # GetVouchers, AddVoucher, CalculateTotalAssets
├── ui/
│   ├── components/     # GlowingCard, VoucherCard, CategoryChip, BottomNavBar
│   ├── concierge/      # ConciergeScreen + ViewModel
│   ├── navigation/     # Screen sealed class, NavGraph
│   ├── scan/           # ScanScreen + ViewModel
│   ├── settings/       # SettingsScreen + ViewModel
│   ├── theme/          # Color.kt, Type.kt, Theme.kt
│   └── wallet/         # WalletScreen + ViewModel
├── MainActivity.kt
└── VaultApplication.kt
```

### Key Architectural Decisions
| Decision | Choice | Rationale |
|----------|--------|-----------|
| **State holding** | `StateFlow` + `combine` | Reactive, lifecycle-aware, prevents memory leaks with `WhileSubscribed` |
| **DI approach** | Dagger Hilt | Standard for Android, compile-time safety vs Koin's runtime resolution |
| **DB strategy** | Room-first, Firestore sync | Offline-first: Room provides instant UI response; Firestore syncs asynchronously |
| **Timestamp storage** | Epoch millis (UTC) | Avoids timezone serialization bugs; `Instant.ofEpochMilli()` for conversion |
| **Mapper pattern** | Static `object VoucherMapper` | Single source of truth for all 3 mapping directions (Domain ↔ Entity ↔ Firestore) |
| **Deletion strategy** | `deletedVouchers` subcollection | Prevents zombie resurrection when offline device syncs stale data |
| **Sync pattern** | Single centralized listener | `FirestoreDataSource.observeVouchers()` is the only snapshot listener; no duplicates |
| **Navigation** | Compose Navigation with `popUpTo + saveState + restoreState` | Prevents back-stack explosion in bottom nav |
| **Build targets** | `minSdk=26`, `targetSdk=35`, `compileSdk=35` | API 26 for `java.time` without desugaring; latest target for Play Store compliance |
| **Proguard** | Enabled for release with entity/model keep rules | Crash-free obfuscation without breaking Room/Firebase reflection |

### Sync Flow (Critical Path)
```
User Action → Room (immediate) → UI updates instantly
                ↓ (async, fire-and-forget)
            Firestore (cloud sync)
                ↓ (snapshot listener)
            Room upsert (merge cloud → local, filter zombies)
```

### Indian Market Considerations
- Currency formatting: `NumberFormat.getCurrencyInstance(Locale("en", "IN"))` — always ₹ symbol
- Source platforms: GPay, PhonePe, CRED with brand-specific badge colors
- SMS/Email scraping permissions declared but gated behind runtime permission flows
- Date format: `dd/MM/yyyy` (Indian convention) in UI

---

## Rule 9: Component API Contracts

### GlowingCard
```kotlin
GlowingCard(
    modifier: Modifier,
    glowColor: Color = VaultPrimaryGlow,  // Use VaultGoldGlow for AI cards
    backgroundColor: Color = VaultCardSurface,
    cornerRadius: Dp = 20.dp,  // Soft rounded — never below 16.dp
    elevation: Dp = 8.dp,
    content: @Composable () -> Unit
)
```

### VoucherCard
- Must show: brand initial circle, brand name, category, source badge, value, expiry countdown, redeem button
- Glow color changes to `VaultRed.copy(alpha=0.3f)` when `isExpiringSoon == true`
- Copy-to-clipboard icon on voucher code — always 48.dp touch target

### CategoryChip
- Animated color transitions using `animateColorAsState`
- Minimum height 48.dp (10.dp vertical padding + text)
- Uses `Role.Tab` for accessibility

---

## Rule 10: File Naming & Code Style
- **Screens**: `{Feature}Screen.kt` (e.g., `WalletScreen.kt`)
- **ViewModels**: `{Feature}ViewModel.kt` with `{Feature}UiState` data class in same file
- **Components**: PascalCase composable name = file name (e.g., `GlowingCard.kt`)
- **No wildcard imports**: except for theme tokens (`import com.addmrp.vault.ui.theme.*`)
- **Suppress warnings**: Use `@Suppress("UNCHECKED_CAST")` only where Firestore map casting requires it

---

## Rule 11: Seamless Intent Hand-off & OCR Resilience

> **⚠️ CRITICAL**: The "Share to Vault" flow is the primary user acquisition funnel. It MUST be bulletproof.

### Intent Safety
- When handling `ACTION_SEND` intents, the app **must never crash** on large bitmaps or malformed URIs
- Use `ContentResolver` safely: always wrap `InputImage.fromFilePath()` in try/catch
- If the Uri is null, invalid, or the image is too large, fail gracefully with a user-friendly error and fall back to manual entry
- Handle `onNewIntent()` for `singleTop` launch mode — the app may already be open when a share arrives

### Non-Blocking Loading State
- While ML Kit OCR is processing, show a **non-blocking shimmer effect** (`OcrProcessingBanner` composable)
- The user must be able to scroll and interact with the form while OCR runs in the background
- Use `Dispatchers.IO` for the OCR coroutine — never block the main thread
- Disable the "Add to Vault" button during OCR to prevent premature saves

### OCR Failure Handling
- If OCR fails (exception, timeout, unreadable image): **leave form fields blank**, show error message, and prompt manual entry
- If OCR returns low-confidence/garbage text: prefer leaving fields blank over inserting bad data
- Always show a gold `isPrefilledFromOcr` banner so the user knows data was auto-detected and should be reviewed

### Pipeline Architecture
```
Share Sheet → ACTION_SEND intent
    → MainActivity.extractSharedImageUri()
        → NavGraph passes Uri to ScanScreen
            → LaunchedEffect triggers ScanViewModel.processSharedImage()
                → ProcessShareImageUseCase.execute() [Dispatchers.IO]
                    → ML Kit TextRecognition → raw text
                    → parseOcrText() → OcrResult(brand, code, value, expiryDate)
                → ScanUiState updated with pre-filled data
                → User reviews and taps "Add to Vault"
```

### Files Involved
| File | Responsibility |
|------|---------------|
| `AndroidManifest.xml` | `<intent-filter>` for `ACTION_SEND` + `image/*` |
| `MainActivity.kt` | Extract Uri, navigate to Scan, handle `onNewIntent` |
| `NavGraph.kt` | Pass `sharedImageUri` parameter to `ScanScreen` |
| `ProcessShareImageUseCase.kt` | ML Kit OCR + text parsing (brand, code, value, expiry) |
| `ScanViewModel.kt` | `processSharedImage()` — OCR on IO, pre-fill state |
| `ScanScreen.kt` | `OcrProcessingBanner` shimmer + OCR success banner |

### Known Brands for OCR Matching
The `ProcessShareImageUseCase` contains a curated list of 40+ Indian brands (Zomato, Swiggy, Amazon, Flipkart, etc.). When adding new brands, update the `knownBrands` list in that file.

### Date Parsing Formats
OCR date extraction supports 8 formats common in Indian fintech:
`dd/MM/yyyy`, `dd-MM-yyyy`, `dd.MM.yyyy`, `yyyy-MM-dd`, `dd MMM yyyy`, `dd MMMM yyyy`, `MMM dd, yyyy`, `MMMM dd, yyyy`

---

# MVP2 ADDENDUM: THE "ADVISOR" PROTOCOL

> **🧠 ETHICAL CORE (NON-NEGOTIABLE)**: The AI's goal is to **Save Money, not encourage spending**. Never suggest a purchase just to hit a milestone. No gamification of spending. Achievement streaks must be for "savings achieved", never for "money spent".

---

## Rule 12: The Frugality Bias

> **⚠️ CRITICAL**: This rule overrides all other optimization logic.

- If a user asks "How can I get more points?", the AI must **first ask**: "Do you have an upcoming necessary expense?"
- If not, respond: *"The best way to save is to not spend. However, for your existing monthly bills, here is the optimal strategy."*
- The `OptimalSwiperUseCase` must **never** suggest a new purchase — only optimize existing/planned spend
- The `SpendAuditorUseCase` must frame advice as "You could have saved ₹X" — never as "You should spend more on Y"
- **Advice Filter**: Every `AdvisorInsight` object must pass through a `FrugalityFilter` before reaching the UI

### Implementation Contract
```kotlin
// The FrugalityFilter is a pure function — must be unit tested
object FrugalityFilter {
    fun filter(insight: AdvisorInsight): AdvisorInsight {
        // Strip any language that encourages new spending
        // Reframe as savings, not rewards
        // If user has debt flag → replace with debt payoff tip
    }
}
```

---

## Rule 13: Hidden Cost Exposure

When suggesting a credit card, the AI must **always** factor in:

| Hidden Cost | How to Surface |
|-------------|---------------|
| **Redemption Fees** | Many Indian banks charge ₹99 + GST per redemption. Deduct from savings calculation |
| **Reward Caps** | "You've already reached your ₹5,000 monthly cashback limit on this card" |
| **Forex Markups** | For international transactions, factor in 2-3.5% markup |
| **Annual Fees** | If card has an annual fee, amortize it into savings calculation |
| **Minimum Spend Requirements** | Some reward tiers require ₹X/month spend — warn if user is below threshold |

### Implementation Contract
```kotlin
data class CardRewardRule(
    val category: SpendCategory,
    val cashbackPercent: Double,       // Direct cashback (e.g., 5%)
    val pointsPerRupee: Double,        // Reward points (e.g., 2 pts/₹100)
    val pointValueInRupees: Double,    // 1 point = ₹X
    val monthlyCashbackCap: Double?,   // Max cashback/month (e.g., ₹5000)
    val redemptionFee: Double,         // ₹99 + GST = ₹116.82
    val minSpendForReward: Double?,    // Minimum spend to unlock tier
    val forexMarkupPercent: Double     // 2-3.5% for international
)
```

---

## Rule 14: 95% Testability Mandate

> **⚠️ CRITICAL**: Every use case, mapper, parser, and filter MUST have corresponding unit tests. Target: **95% line coverage on domain and data/mapper layers**.

### Test Architecture
```
app/src/test/java/com/addmrp/vault/
├── domain/
│   ├── model/          # VoucherExpiryTest, CreditCardTest
│   └── usecase/        # OptimalSwiperTest, SpendAuditorTest,
│                       # RewardValuationTest, DebtDetectorTest,
│                       # CalculateTotalAssetsTest
├── data/
│   ├── mapper/         # VoucherMapperTest, CreditCardMapperTest
│   └── sms/            # SmsTransactionParserTest (40+ SMS format tests)
└── advisor/
    └── FrugalityFilterTest
```

### Testability Rules
1. **All use cases must be pure functions** — accept data in, return data out, no side effects
2. **All mappers must have round-trip tests** — Domain→Entity→Domain must produce identical objects
3. **SMS parser must have format-specific tests** — one test class per bank (HDFC, SBI, ICICI, Axis, Kotak)
4. **MockK for repository tests** — mock Room DAOs and Firestore, verify correct calls
5. **No Android instrumentation for business logic** — all domain tests must run on JVM
6. **FrugalityFilter must be tested** — verify it strips spending-encouragement language
7. **DebtDetector must be tested** — verify it triggers debt mode correctly
8. **Edge cases are mandatory** — empty lists, null values, overflow amounts, negative values

### Test Naming Convention
```kotlin
@Test
fun `given user with HDFC Millennia, when buying groceries at Blinkit, then SBI Cashback recommended`()
```

---

## Rule 15: Absolute Privacy (Local-First Financial Data)

> **Financial data is radioactive.** Treat it with extreme care.

### Processing Rules
| Data Type | Processing Location | Storage |
|-----------|-------------------|---------|
| SMS parsing | **On-device only** (ML Kit / Regex) | Room DB (encrypted) |
| Transaction history | **On-device** | Room DB (encrypted), optional Firestore sync |
| Credit card details | **On-device only** | Room DB (**must** use `@ColumnInfo` with encrypted name) |
| Reward point balances | **On-device** | Room DB, optional Firestore sync |
| Statement PDFs | **On-device** (local LLM or regex) | Never stored — parsed and discarded |
| Bank balances/limits | **On-device only** | Room DB (**AES-256 encryption at rest**) |

### Implementation Rules
1. SMS `BroadcastReceiver` must only activate with **explicit user consent** (toggle in Settings)
2. Never log financial data in production builds — use `BuildConfig.DEBUG` guards
3. The `PrivacyShieldBadge` component must be visible on every screen that processes financial data
4. Show "🔒 Processing locally" when using on-device ML Kit
5. Show "☁️ Syncing to cloud (encrypted)" when syncing to Firestore
6. **Never** send raw SMS content to any remote endpoint
7. Statement PDFs must be processed in a `CoroutineScope` and the file reference must be cleared after parsing
8. All Firestore financial data must use Firebase Security Rules that restrict reads to `request.auth.uid == resource.data.userId`

### Future: RBI Account Aggregator (AA)
For production-grade financial data, consider integrating the **RBI Account Aggregator Framework** via providers like Sahamati or Setu. This provides bank-verified data with a single user OTP — more reliable than SMS scraping.

---

## Rule 16: Habit Protection (Debt Safety Net)

> **If a user is paying 42% interest on revolving credit, reward points are irrelevant.**

### Detection Logic (`DebtDetectorUseCase`)
```
IF any card shows:
  - "Minimum Amount Due" paid (instead of full balance)
  - OR credit utilization > 70%
  - OR interest charges detected in statement
THEN:
  - Set global flag: debtModeActive = true
  - Disable all "Reward Maximization" tips
  - Replace with "Debt Payoff" tips
  - Show DebtWarningBanner on home screen
```

### Debt Mode Behavior
| Normal Mode | Debt Mode |
|-------------|-----------|
| "Use SBI card for 5% cashback" | "Pay off your HDFC balance first — you're paying 42% interest" |
| "You have 12,500 reward points" | "Your interest charges (₹3,200) far exceed your rewards (₹1,250)" |
| Savings Dial shows savings | Savings Dial shows debt cost |
| Gold accent on AI cards | Red accent on AI cards |

### Non-Negotiable
- Never use "streaks" or "rewards" that trigger dopamine for spending money
- Streaks/rewards may ONLY celebrate "savings achieved" or "debt reduced"
- The `FrugalityFilter` must check `debtModeActive` flag before any advice reaches the UI

---

## Rule 17: Notification "Nudge" Copywriting

> **Financial Advisor Persona**: Notifications focus on **Loss of Value**, not generic technical updates.

### Notification Schedule
Upon saving a voucher (via Scan, Share, or Manual), schedule two local notifications:

| Timing | Importance | Channel |
|--------|-----------|---------|
| **24 hours before expiry** | DEFAULT | `vault_expiry` |
| **2 hours before expiry** | HIGH (heads-up) | `vault_expiry_urgent` |

### Copywriting Templates

**24h Alert:**
> "Don't let ₹[Value] slip away! Your [Brand] voucher expires tomorrow. Think if you have any necessary [Category] needs today."

**2h Alert (URGENT):**
> "Final Window: Your ₹[Value] [Brand] reward expires in 2 hours. Use it now or it's gone forever."

**Redemption Success Toast:**
> "Great save! You just kept ₹[Value] in your pocket."

### State Management
- If voucher is marked as `isRedeemed = true` or **deleted** → immediately cancel all pending alarms
- On device reboot → `BootRescheduleReceiver` reschedules all active alarms from Room DB
- Use `AlarmManager.setExactAndAllowWhileIdle()` for precision

### Implementation Files
| File | Responsibility |
|------|---------------|
| `notification/VoucherNotificationManager.kt` | Schedule/cancel/reschedule alarms |
| `notification/VoucherAlarmReceiver.kt` | Fire notifications with advisor copy |
| `notification/BootRescheduleReceiver.kt` | Reschedule after reboot |
| `VaultApplication.kt` | Create `NotificationChannel`s |

---

## Rule 18: Family Sharing Privacy

> **Privacy Gate**: Group members see brand, value, and expiry — but **NEVER** the promo code until the owner explicitly reveals it.

### Group Rules
| Rule | Detail |
|------|--------|
| Max members | 5 (family-sized household) |
| Code visibility | Hidden by default — owner must tap "Reveal" |
| Data shared | Brand name, value, expiry date, category |
| Data NOT shared | Promo code, notes, source details |

### Firestore Security Rules
```
match /vaultGroups/{groupId} {
  allow read, write: if request.auth.uid in resource.data.memberUids;
}
match /vouchers/{voucherId} {
  allow read: if request.auth.uid == resource.data.ownerId
    || request.auth.uid in resource.data.sharedWith;
}
```

### Implementation Files
| File | Responsibility |
|------|---------------|
| `domain/model/VaultGroup.kt` | Group + member models |
| `data/remote/VaultGroupDataSource.kt` | Firestore CRUD with transactions |
| `ui/sharing/VaultSharingScreen.kt` | Invite/remove UI with privacy banner |

---

## Rule 19: The "Uri Permission" Safety

> **System Resilience**: When receiving an image Uri via `ACTION_SEND`, the app must immediately copy the image to its internal cache.

This prevents `SecurityException` if the originating app (e.g., WhatsApp, Gallery) closes before ML Kit finishes OCR.

## Rule 20: Deep Link Privacy

> **Secure Outward Sharing**: Deep links must **NOT** contain the raw voucher code in the URL string.

- Format: `https://vouchervault.addmrp.com/reward/{rewardId}`
- **Implementation**: The app intercepts `ACTION_VIEW` in `MainActivity.kt`. It extracts `deepLinkRewardId` and passes it to the `VaultNavGraph`. The data is retrieved securely from the Firestore `vouchers` collection (since it's already observed via `collectionGroup` sync), ensuring the user is authorized to view it. The raw promo code is never transmitted outside of Firebase.

## Rule 21: Zero-Interruption Ingestion

> **Seamless Inward Sharing**: If the user shares a screenshot while VoucherVault is already open, use a **Bottom Sheet** to show OCR progress and pre-fill data.

- Do not force-navigate away from their current screen.
- Maintain their flow, allowing them to save the voucher and dismiss the sheet immediately.

## Rule 22: Conflict Resolution

For shared rewards, implement a **Last-Writer-Wins** or **Optimistic Concurrency** model.
- If two people update a reward at the same time, show a "Syncing..." state and resolve to the latest server timestamp.
- **Implementation**: `FirestoreDataSource` uses `collectionGroup("vouchers").whereArrayContains("sharedWith", uid)` to ensure everyone in the `VaultGroup` receives updates simultaneously.
- **Real-time Lock**: When User A taps "Redeem" for a shared reward, `VoucherRepositoryImpl.acquireLock()` sets `inUseByUserId` to User A's `uid`. If User B attempts to view or redeem it, `WalletScreen` intercepts this and displays a "Reward Locked 🔒" dialog indicating the other family member is currently viewing it, completely avoiding double-redemption accidents.