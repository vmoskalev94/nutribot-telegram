# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

NutriBot is a Telegram bot for athletes that tracks workouts and provides personalized micronutrient recommendations. The bot calculates Training Stress Score (TSS) from workouts and uses scientific formulas to recommend 13 essential nutrients (vitamins B6, B12, C, D3, E, K, Folate, Calcium, Iron, Iodine, Magnesium, Selenium, Zinc, Omega-3) based on user profile, training load, and lifestyle factors.

**Tech Stack:** Spring Boot 3.5.7, Java 21, PostgreSQL, Spring Data JDBC (not JPA), Flyway migrations, Pengrad Telegram Bot API

## Build & Run Commands

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Run tests
./gradlew test

# Run a specific test class
./gradlew test --tests "ClassName"

# Run a specific test method
./gradlew test --tests "ClassName.methodName"

# Clean build artifacts
./gradlew clean
```

### Environment Variables

Required for running:
- `TELEGRAM_BOT_TOKEN` - Bot token from @BotFather (required)
- `SPRING_DATASOURCE_USERNAME` - PostgreSQL username (default: nutribot)
- `SPRING_DATASOURCE_PASSWORD` - PostgreSQL password (default: nutribot)
- `DB_HOST` - Database host (default: localhost)
- `DB_PORT` - Database port (default: 5432)
- `DB_NAME` - Database name (default: nutribot)
- `TELEGRAM_NUTRIENT_EXPLAIN` - Enable verbose nutrient calculation explanations (default: false)

### Docker

```bash
# Run with Docker Compose (includes PostgreSQL)
docker-compose up

# Rebuild and run
docker-compose up --build
```

## Architecture

### Update Dispatcher Pattern (Chain of Responsibility)

The bot uses a Chain of Responsibility pattern for routing Telegram updates:

1. **UpdateDispatcher** (`UpdateDispatcher.java`) receives raw Telegram `Update` objects
2. Converts them to `UpdateContext` (chatId, userId, text, message, callbackQuery)
3. Iterates through all `BotUpdateHandler` beans sorted by `getOrder()` (lower = higher priority)
4. First handler where `canHandle(ctx)` returns true processes the update via `handle(ctx)`

**Handler Priority Order** (lower number = higher priority):
- 4: `CancelCommandHandler` - `/cancel` command
- 5: `StartCommandHandler` - `/start` command
- 6: `OnboardingMenuHandler` - onboarding menu button from main menu
- 10: `PingCommandHandler` - `/ping` diagnostic command
- 15: `OnboardingFlowHandler`, `HelpHandler` - mandatory first-time setup, help command
- 20: `MainMenuInlineHandler` - main menu inline buttons
- 30: `ProfileMenuHandler` - profile menu text button handler
- 31: `ProfileEditHandler`, `ProfileTrainingLevelInlineHandler` - profile editing flows
- 40: `WorkoutsMenuHandler` - workouts list and menu
- 45: `StrengthWorkoutAddFlowHandler` - strength workout creation with multi-line input
- 46: `CardioWorkoutAddFlowHandler` - cardio workout creation flow
- 50: `NutrientsMenuHandler`, `BackToMainMenuHandler` - nutrients menu, generic back button
- 220: `LifestyleLocationHandler` - location sharing for climate/sun exposure data
- 600: `WorkoutNutrientsHandler` - nutrient calculations for specific workouts
- 605: `NutrientVerboseInlineHandler` - toggle verbose nutrient explanations
- 606: `NutrientExplainHandler` - detailed nutrient calculation explanations with chunking
- 610: `LifestyleInlineHandler` - lifestyle factors input (smoking, vegan, pregnant)
- Integer.MAX_VALUE: `FallbackEchoHandler` - catch-all fallback handler

All handlers implement `BotUpdateHandler` interface:
```java
boolean canHandle(UpdateContext ctx);
void handle(UpdateContext ctx);
int getOrder(); // lower = higher priority
```

### State Management (Hybrid Approach)

**Database State** (long-term):
- `user_onboarding_state` - tracks current onboarding step
- `onboarding_completed` flag in `app_user` table

**In-Memory State** (short-term):
- `WorkoutCreationStateStore` - strength/cardio workout creation
- `CardioCreationStateStore` - cardio-specific flow state
- `ProfileEditStateStore` - profile editing
- Implementation: `ConcurrentHashMap<Long, State>` (userId → state)
- **Important:** In-memory state is lost on restart. This is acceptable for MVP as flows are short-lived.

### Mandatory Onboarding Flow

New users **must** complete onboarding before accessing any features. The flow is controlled by:
- `OnboardingFlowHandler` (order: 15) intercepts all updates if user has active onboarding state
- Steps defined in `OnboardingStep` enum: A1_GREETING → A2_NAME → A3_SEX → A4_AGE → A5_HEIGHT → A6_WEIGHT → A7_TRAINING_LEVEL → A8_CITY_OR_GEO → A9_PHONE → A10_FINISH
- Each step validates input, updates `app_user` table, advances to next step
- After completion: sets `onboarding_completed = true`, clears `user_onboarding_state`

### Workout Input Features

**Strength Workouts:**
- Multi-line set input: users can enter all sets at once, separated by newlines:
  ```
  80,10,2
  80,9,1
  75,10,2
  ```
- Format: `weight,reps[,RIR]` where RIR = Reps In Reserve (0-8)
- Validation happens for all lines before creating any sets
- See `StrengthWorkoutAddFlowHandler.handleMultipleSetsInput()` at line 256

**Cardio Workouts:**
- Sequential input flow: activity type → duration → distance → intensity (RPE or Low/Moderate/High)
- State managed in `CardioCreationStateStore` with step enum

### Nutrient Calculation System

Core calculation happens in `NutrientCalculatorService.calculateWithContext()`:

1. **Context Building** (`buildContext()`):
   - Loads user profile from `app_user`
   - Loads workout details (strength exercises/sets or cardio details)
   - Calculates training load metrics via `TssService`:
     - **SS (Strain Score)**: Raw workout difficulty (strength sets or cardio effort)
     - **TSS (Training Stress Score)**: Age-adjusted training stress
     - Strength: `StrengthTssCalculator` - based on weight, reps, RIR
     - Cardio: `CardioTssCalculator` - based on duration, intensity, RPE
   - Calculates body composition via `BodyCompositionCalculator`:
     - **FFM (Fat-Free Mass)**: Lean body mass from height, weight, sex, training level
     - **Bone Mass**: Skeletal mass estimation
     - **Sweat Rate**: Expected fluid loss during exercise
   - Loads lifestyle factors from `user_lifestyle` (smoking, vegan, pregnant)
   - Loads sun exposure data (sunExposureMinutes) if available
   - Assembles `NutrientContext` (25+ fields including derived booleans and utility methods)

2. **Formula Execution** (`calculateForContext()`):
   - Iterates through all `ExplainableNutrientFormula` beans (14+ formulas including Copper)
   - Each formula is a Spring `@Component` implementing:
     ```java
     String code();  // e.g., "VITAMIN_D3"
     double calculate(NutrientContext ctx);
     String template(); // human-readable formula template
     Map<String, Object> vars(NutrientContext ctx); // variable values for explanation
     ```
   - All formulas must use `NutrientLimits` class to clamp results within MIN/MAX boundaries
   - Formulas use constants from `MvpConstants` for baseline nutrient values
   - Example: `VitaminD3Formula` considers age, weight, TSS, sun exposure
   - Returns `List<NutrientRecommendation>` with value, unit, name

3. **Formula Location & Architecture:**
   - All 14+ formulas in `src/main/java/com/nutribot/bot/nutrition/formulas/`
   - Each formula encapsulates scientific calculation logic
   - `NutrientLimits.java` - defines MIN/MAX bounds for all nutrients (clamping pattern)
   - `MvpConstants.java` - baseline nutrient values and calculation constants
   - Example: `MagnesiumFormula` adds 30% for strength training, bonus for high TSS

### Nutrient Explanation & Lifestyle Features

**Nutrient Explanation System** (controlled by `TELEGRAM_NUTRIENT_EXPLAIN` environment variable):
- **Verbose Mode Toggle** (`NutrientVerboseInlineHandler`): Users can enable/disable detailed explanations
- **Calculation Explanations** (`NutrientExplainHandler`): Shows formula template with actual variable values
- **Message Chunking** (`NutrientExplanationService`): Splits long explanations at 3800 chars for Telegram limits
- **Database State**: `app_user.nutrient_verbose` flag tracks user preference
- **Configuration**: `nutribot.features.nutrient-explain.user-visible` property controls feature visibility

**Lifestyle System Expansion:**
- **Location Sharing** (`LifestyleLocationHandler`): Captures GPS/city for sun exposure calculations
- **Lifestyle Factors** (`LifestyleInlineHandler`): Collects smoking, vegan, pregnancy status
- **Data Storage**: `user_lifestyle` table (smoke_packs_per_day, vegan, pregnant)
- **Integration**: Lifestyle data flows into `NutrientContext` for formula adjustments
- **Sun Exposure**: Uses location data to estimate vitamin D3 needs

### Database Schema

Key tables:
```sql
app_user (id, telegram_id, name, sex, age, height_cm, weight_kg,
          training_level, onboarding_completed, nutrient_verbose, created_at)

user_onboarding_state (id, user_id, current_step)

user_lifestyle (user_id PK, smoke_packs_per_day, vegan, pregnant)

workout (id, user_id, type, status, started_at)
  ├─ strength_exercise (id, workout_id, name, order_index)
  │   └─ strength_set (id, exercise_id, weight, reps, rir, order_index)
  └─ cardio_workout_details (workout_id PK, activity_type, duration_min,
                              distance_km, intensity, rpe)

nutrient_definition (code PK, name, unit, description)
```

**Important:**
- Uses Spring Data JDBC (not JPA) - lighter and faster
- Flyway migrations in `src/main/resources/db/migration/`
- Workout status: `DRAFT` (during creation) → `ACTIVE` (saved) or `DELETED` (soft delete)

### Dependency Injection

All services and handlers use constructor injection:
```java
@Component
@RequiredArgsConstructor  // Lombok generates constructor
public class OnboardingFlowHandler implements BotUpdateHandler {
    private final OnboardingService onboardingService;
    private final UserService userService;
    private final TelegramClient tg;
}
```

### Key Business Logic Files

- **Onboarding:** `OnboardingFlowHandler.java` - 10-step mandatory setup
- **Strength Workouts:** `StrengthWorkoutAddFlowHandler.java` - exercise/set input with multi-line support
- **Cardio Workouts:** `CardioWorkoutAddFlowHandler.java` - sequential step-by-step input
- **Nutrient Calculation:** `NutrientCalculatorService.java` - context building + formula execution
- **Nutrient Explanation:** `NutrientExplanationService.java` - builds human-readable explanations with message chunking
- **Nutrient Constraints:** `NutrientLimits.java` - MIN/MAX bounds for all nutrients
- **Nutrient Constants:** `MvpConstants.java` - baseline values and calculation constants
- **TSS Calculation:** `TssService.java` - orchestrates SS and TSS calculation
- **Strength Score:** `SsCalculator.java` - strength-specific strain score calculation
- **Body Composition:** `BodyCompositionCalculator.java` - FFM, bone mass, sweat rate calculations
- **Lifestyle Management:** `UserLifestyleService.java` - manages smoking, vegan, pregnancy factors
- **Update Routing:** `UpdateDispatcher.java` - Chain of Responsibility dispatcher
- **Nutrient Formulas:** `src/main/java/com/nutribot/bot/nutrition/formulas/` - 14+ formula implementations

### Configuration

- `application.yaml` - main config with environment variable placeholders
- Active profile: `local` (default)
- Database connection configured via environment variables
- Telegram bot token required via `TELEGRAM_BOT_TOKEN`

## Important Implementation Notes

1. **Handler Order Matters:** When adding new handlers, carefully choose `getOrder()` value to position it correctly in the chain. Lower numbers = higher priority.

2. **State Store Cleanup:** Always call `stateStore.clear(userId)` after completing a flow to prevent memory leaks.

3. **Callback Data Prefixes:** Use consistent prefixes for inline button callbacks:
   - `onb:` for onboarding
   - `workout:strength:` for strength workouts
   - `workout:cardio:` for cardio workouts
   - `menu:` for menu navigation
   - `profile:` for profile editing
   - `lifestyle:` for lifestyle flow
   - `nutrient:` for nutrient actions (explain, verbose toggle)

4. **Multi-line Input Parsing:** When accepting multi-line input, validate ALL lines before creating database records. See `StrengthWorkoutAddFlowHandler.handleMultipleSetsInput()` for reference implementation.

5. **Nutrient Formulas:** Each formula is independent. Add new nutrients by creating a new `@Component` class implementing `ExplainableNutrientFormula` in the `formulas/` package. All formulas must use `NutrientLimits` to clamp results within MIN/MAX bounds. The `NutrientContext` object provides 25+ fields including: sex, ageYears, weightKg, heightCm, trainingLevel, workoutType, durationMin, distanceKm, cardioRpe, ss, tss, ffm, boneMass, sweatRate, smokePacksPerDay, vegan, pregnant, sunExposureMinutes, and derived booleans (male, athlete). Utility methods available: `getSsOrZero()`, `getTssOrZero()`, `getFfmOrDefault()`, `getBoneMassOrDefault()`, `getSweatRateOrDefault()`, `isMale()`, `isAthlete()`, `isPregnant()`, `isVegan()`, `getSmokePacksOrZero()`.

6. **Database Migrations:** Always create a new Flyway migration file (V{n}__description.sql) for schema changes. Never modify existing migrations.

7. **User Lookup:** Use `UserService.ensureUserByTelegramId()` to get/create user records - never query directly by telegram_id without this helper.

8. **Workout Draft Pattern:** Workouts start as `DRAFT` status during creation, become `ACTIVE` on save, or `DELETED` on discard. Never hard-delete workouts.
