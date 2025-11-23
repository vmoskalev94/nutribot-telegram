# NutriBot — Telegram-бот для спортсменов

Backend для Telegram-бота, помогающего отслеживать тренировки и получать персонализированные рекомендации по микронутриентам.

## Ключевые возможности

- 🏋️ **Учёт тренировок**: силовые и кардио с детальной статистикой
  - Силовые: упражнения, подходы (вес, повторы, RIR)
  - **Быстрый ввод**: можно вводить все подходы сразу, разделяя переносами строк
  - Кардио: тип активности, длительность, дистанция, интенсивность (RPE)
- 📊 **Training Stress Score (TSS)**: автоматический расчёт тренировочного стресса
- 💊 **Персонализированные рекомендации по 13 нутриентам**:
  - Витамины: B6, B12, C, D3, E, K, Фолат (B9)
  - Минералы: Кальций, Железо, Йод, Магний, Селен, Цинк, Омега-3
  - Учитывают: возраст, пол, вес, рост, уровень подготовки, образ жизни, конкретную тренировку
- 👤 **Полноценный профиль**: антропометрия, lifestyle-факторы (курение, веганство, беременность)
- 🔬 **Научные формулы**: каждый нутриент рассчитывается по проверенным методикам
- 🎯 **Обязательный онбординг**: персонализация с первого запуска

## Бизнес-модель

### Целевая аудитория
Активные люди и спортсмены, которые:
- Регулярно тренируются (силовые или кардио)
- Хотят оптимизировать питание под свои нагрузки
- Нуждаются в персонализированных рекомендациях по витаминам и минералам

### Ценностное предложение

**Проблема**: Спортсмены не знают, какие витамины и минералы им нужны с учётом их индивидуальных характеристик, образа жизни и тренировочных нагрузок.

**Решение**: Бот собирает данные о пользователе и его тренировках, рассчитывает Training Stress Score (TSS) и на основе научных формул выдаёт персонализированные рекомендации по 13 ключевым нутриентам.

### Монетизация (потенциал)
- Freemium модель: базовый функционал бесплатно, расширенная аналитика по подписке
- Партнёрские программы с магазинами спортивного питания
- Персональные консультации нутрициологов через бота
- Premium рекомендации с учётом анализов крови

---

## Основные флоу

### 1. Онбординг (Первичная регистрация)

#### Бизнес-логика
При первом запуске бота пользователь **обязан** пройти онбординг — без этого доступ к функциям бота закрыт. Это критично, т.к. все рекомендации строятся на антропометрических данных и уровне подготовки.

**Шаги онбординга:**
1. **Приветствие** — объяснение возможностей бота
2. **Имя** — можно взять из Telegram или ввести вручную
3. **Пол** (М/Ж) — влияет на расчёт мышечной массы и нутриентов
4. **Возраст** (10-100 лет) — критичен для возрастных норм витаминов
5. **Рост** (120-230 см) — для расчёта индекса массы тела
6. **Вес** (35-250 кг) — для расчёта базовых потребностей
7. **Уровень подготовки**:
   - Beginner — нерегулярные тренировки
   - Amateur — 3-4 раза в неделю
   - Pro — высокие объёмы, спорт как важная часть жизни
8. **Геолокация/город** — для учёта климата и длительности дня (влияет на витамин D)
9. **Телефон** — для восстановления доступа и будущих уведомлений
10. **Завершение** — доступ к главному меню

#### Техническая реализация

**Хендлер**: `OnboardingFlowHandler` (order: 15)

**Хранилище состояния**:
- Таблица: `user_onboarding_state`
- Поля: `user_id`, `current_step` (enum)

**Механика**:
1. `StartCommandHandler` проверяет флаг `onboarding_completed` в таблице `app_user`
2. Если не пройден — создаётся запись в `user_onboarding_state` с шагом `A1_GREETING`
3. `OnboardingFlowHandler` перехватывает все апдейты (кроме `/start`, `/cancel`, `/help`)
4. Метод `canHandle()` проверяет наличие активного состояния онбординга
5. Метод `handleStepInternal()` роутит по шагам через switch-case
6. Каждый шаг:
   - Валидирует ввод пользователя
   - Сохраняет данные через `UserService` в таблицу `app_user`
   - Обновляет `current_step` в `user_onboarding_state`
   - Отправляет следующий вопрос

**Особенности**:
- Inline-кнопки для бинарного выбора (пол, уровень подготовки)
- Reply-клавиатуры для шаринга геолокации и контакта
- Валидация с понятными сообщениями об ошибках
- После завершения: `onboarding_completed = true`, очистка `user_onboarding_state`

**Файлы**:
- `src/main/java/com/nutribot/bot/onboarding/OnboardingFlowHandler.java`
- `src/main/java/com/nutribot/bot/onboarding/OnboardingService.java`
- `src/main/java/com/nutribot/bot/onboarding/OnboardingStep.java` (enum шагов)

---

### 2. Добавление силовой тренировки

#### Бизнес-логика
Пользователь фиксирует силовую тренировку, добавляя упражнения и подходы. Это нужно для:
- Расчёта тренировочного стресса (TSS) — чем тяжелее подходы, тем выше нагрузка
- Персонализации рекомендаций по нутриентам (магний, цинк, витамины группы B для восстановления)

**Шаги**:
1. Пользователь выбирает «Тренировки» → «Добавить» → «Силовая»
2. Вводит **название упражнения** (например, "Жим лёжа")
3. Вводит **подходы** в формате: `вес,повторы[,RIR]`
   - **Одиночный ввод**: `80,10,2` = 80 кг, 10 повторений, RIR 2 (запас 2 повторения)
   - **Множественный ввод** (каждый подход с новой строки):
     ```
     80,10,2
     80,9,1
     75,10,2
     ```
   - RIR (Reps In Reserve) — сколько повторений осталось до отказа
4. Может добавить несколько подходов для упражнения (по одному или сразу все)
5. Может добавить новое упражнение или завершить тренировку
6. Видит итоговую сводку и выбирает: **Сохранить** или **Удалить**

#### Техническая реализация

**Хендлер**: `StrengthWorkoutAddFlowHandler` (order: 45)

**Хранилище состояния**:
- In-memory store: `WorkoutCreationStateStore`
- Поля: `workoutId`, `type` (STRENGTH), `currentExerciseId`

**Механика**:
1. `WorkoutsMenuHandler` по кнопке "Добавить → Силовая" создаёт черновик через `WorkoutService.createWorkout()`
   - Таблица `workout`: статус = `DRAFT`
2. Вызывает `StrengthWorkoutAddFlowHandler.startNewStrengthWorkout()`
3. Handler сохраняет состояние в `WorkoutCreationStateStore`
4. Метод `canHandle()` проверяет:
   - Есть ли активное состояние для userId
   - Тип тренировки = STRENGTH
   - Текст не команда (не начинается с `/`)
5. Метод `handle()` роутит на `handleText()` или `handleCallback()`

**Обработка текста**:
- Если `currentExerciseId == null` → ждём название упражнения
  - Валидация: 2-100 символов, хотя бы одна буква
  - Создаём запись в таблице `strength_exercise` через `WorkoutService.addStrengthExercise()`
- Если `currentExerciseId != null` → ждём подход(ы)
  - **Определение режима**: проверяется наличие переносов строк (`\n`)
  - **Одиночный ввод** (`handleSingleSetInput()`):
    - Парсинг через regex: `split("[\\s,.;/\\-]+")`
    - Валидация: вес >= 0, повторы > 0, RIR 0-8
    - Создаём 1 запись в таблице `strength_set`
  - **Множественный ввод** (`handleMultipleSetsInput()`):
    - Разбиваем по переносам строк: `split("\\r?\\n")`
    - Парсим каждую строку отдельно (пустые строки пропускаются)
    - Если хотя бы одна строка невалидна → показываем ошибку с номерами проблемных строк
    - Если все валидны → создаём все подходы разом в цикле
    - Возвращаем сводку: "Записал 3 подхода для «Жим лёжа»"

**Inline-кнопки**:
- `workout:strength:new_exercise` — сбрасывает `currentExerciseId` → ждём новое упражнение
- `workout:strength:finish` — показывает сводку тренировки
- `workout:strength:save` — переводит статус в `ACTIVE`, очищает state
- `workout:strength:delete` — статус в `DELETED` (soft-delete), очищает state

**Таблицы**:
- `workout` (id, user_id, type, status, started_at)
- `strength_exercise` (id, workout_id, name, order_index)
- `strength_set` (id, exercise_id, weight, reps, rir, order_index)

**Файлы**:
- `src/main/java/com/nutribot/bot/workout/StrengthWorkoutAddFlowHandler.java`
- `src/main/java/com/nutribot/bot/workout/WorkoutService.java`

---

### 3. Добавление кардио-тренировки

#### Бизнес-логика
Пользователь фиксирует кардио-тренировку для расчёта нагрузки и рекомендаций по нутриентам (витамин C, железо, магний для выносливости).

**Шаги**:
1. Выбор «Тренировки» → «Добавить» → «Кардио»
2. Ввод **вида активности** (Бег, Велосипед, Гребля, Ходьба)
3. Ввод **длительности** в минутах (5-600)
4. Ввод **дистанции** в километрах (0-500, может быть 0 если неважна)
5. Выбор **интенсивности**:
   - Кнопки: Low / Moderate / High
   - Или ввод RPE (1-10) — субъективная оценка нагрузки
6. Просмотр сводки и выбор: **Сохранить** или **Удалить**

#### Техническая реализация

**Хендлер**: `CardioWorkoutAddFlowHandler` (order: 46)

**Хранилище состояния**:
- In-memory: `WorkoutCreationStateStore` (базовый workoutId)
- In-memory: `CardioCreationStateStore` (специфичный стейт с шагами)

**Поля CardioCreationState**:
- `step` (enum: ACTIVITY_TYPE, DURATION, DISTANCE, INTENSITY, SUMMARY)
- `activityType`, `durationMin`, `distanceKm`, `intensity`, `rpe`

**Механика**:
1. Создание черновика аналогично силовой
2. Handler переключается между шагами через `CardioCreationStep`
3. Каждый шаг валидирует ввод и сохраняет в state
4. На шаге `INTENSITY`:
   - Inline-кнопки: `workout:cardio:intensity_low/moderate/high`
   - Или текстовый ввод RPE (1-10) → маппинг:
     - 1-3 → LOW
     - 4-7 → MODERATE
     - 8-10 → HIGH
5. После выбора интенсивности → сохранение через `WorkoutService.fillCardioDetails()`
   - Таблица: `cardio_workout_details` (one-to-one с workout)
6. Показ сводки и кнопки save/delete

**Таблицы**:
- `workout` (id, user_id, type=CARDIO, status, started_at)
- `cardio_workout_details` (workout_id PK, activity_type, duration_min, distance_km, intensity, rpe)

**Файлы**:
- `src/main/java/com/nutribot/bot/workout/CardioWorkoutAddFlowHandler.java`
- `src/main/java/com/nutribot/bot/workout/CardioCreationStep.java` (enum)

---

### 4. Расчёт рекомендаций по нутриентам

#### Бизнес-логика
Это **ключевая фича бота** — персонализированный расчёт витаминов и минералов на основе:
- Профиля пользователя (возраст, пол, вес, рост)
- Уровня подготовки
- Конкретной тренировки (тип, длительность, интенсивность)
- Образа жизни (курение, веганство, беременность)
- TSS (Training Stress Score) — показатель тренировочного стресса

**Что рассчитывается** (13 нутриентов):
- Витамины: B6, B12, C, D3, E, K, Фолат (B9)
- Минералы: Кальций, Железо, Йод, Магний, Селен, Цинк
- Омега-3

**Пример**: Если пользователь — мужчина 30 лет, 80 кг, профессиональный уровень, сделал тяжёлую кардио-тренировку (TSS 120), курит и живёт в регионе с малым количеством солнца → бот увеличит рекомендации по витамину C (антиоксидант), магнию (восстановление), витамину D3 (дефицит солнца).

#### Техническая реализация

**Entry point**: `NutrientsMenuHandler` или кнопка "Рассчитать нутриенты" после сохранения тренировки

**Сервис**: `NutrientCalculatorService.calculateWithContext(userId, workoutId)`

**Алгоритм**:
1. **Сборка контекста** (`buildContext()`):
   - Загружаем `User` из БД
   - Загружаем `WorkoutDetails` (включая strength_exercises/cardio_details)
   - Расчёт TSS через `TssService`:
     - Для силовой: `StrengthTssCalculator` — учитывает вес, повторы, RIR
     - Для кардио: `CardioTssCalculator` — учитывает длительность, интенсивность, RPE
   - Загружаем `UserLifestyle` (курение, веганство, беременность)
   - Собираем `NutrientContext` с ~20 полями

2. **Расчёт рекомендаций** (`calculateForContext()`):
   - Итерируемся по всем формулам: `List<ExplainableNutrientFormula>`
   - Каждая формула — Spring Bean, имплементирующий интерфейс:
     ```java
     public interface ExplainableNutrientFormula {
         String code();  // "VITAMIN_D3"
         double calculate(NutrientContext ctx);
         String explain(NutrientContext ctx); // для debug-режима
     }
     ```
   - Примеры формул:
     - `VitaminD3Formula`: базовая норма + коррекция на вес + бонус за TSS + штраф за недостаток солнца
     - `MagnesiumFormula`: норма по полу/возрасту + 30% за силовую тренировку + бонус за высокий TSS
     - `IronFormula`: норма выше для женщин + коррекция на веганство + бонус за кардио

3. **Получение метаданных**:
   - Кэшируем определения нутриентов из таблицы `nutrient_definition` (name, unit, description)

4. **Возврат результата**:
   - `List<NutrientRecommendation>`: code, name, value, unit
   - Если включен `nutrient_verbose` → дополнительно отправляем объяснение расчёта через `NutrientExplanationService`

**Формулы**:
Каждая в отдельном классе, например `VitaminD3Formula.java`:
```java
@Component
public class VitaminD3Formula implements ExplainableNutrientFormula {
    public String code() { return "VITAMIN_D3"; }

    public double calculate(NutrientContext ctx) {
        double base = 600; // IU базовая норма
        if (ctx.getAgeYears() > 70) base = 800;

        // коррекция на вес
        double weightFactor = ctx.getWeightKg() / 70.0;
        base *= weightFactor;

        // штраф за недостаток солнца
        if (ctx.getSunExposureMinutes() < 15) base *= 1.5;

        // бонус за тренировочный стресс
        base += ctx.getTssTotal() * 2;

        return base;
    }

    public String explain(NutrientContext ctx) {
        // Пошаговое объяснение формулы для debug
    }
}
```

**Таблицы**:
- `nutrient_definition` (code PK, name, unit, description)
- `user_lifestyle` (user_id PK, smoke_packs_per_day, vegan, pregnant)

**Файлы**:
- `src/main/java/com/nutribot/bot/nutrition/NutrientCalculatorService.java`
- `src/main/java/com/nutribot/bot/nutrition/formulas/*` (13 классов формул)
- `src/main/java/com/nutribot/bot/workout/tss/TssService.java`

---

### 5. Просмотр и редактирование профиля

#### Бизнес-логика
Пользователь может:
- Просмотреть свой профиль (имя, пол, возраст, вес, рост, уровень подготовки)
- Отредактировать любое поле (например, обновить вес после прогресса)
- Настроить lifestyle-факторы (курение, веганство, беременность)
- Включить/выключить verbose-режим для объяснений расчётов нутриентов

Это критично, т.к. рекомендации динамичны и зависят от актуальных данных.

#### Техническая реализация

**Entry point**: `ProfileMenuHandler` → кнопка "Профиль"

**Просмотр**:
- `ProfileService.buildProfileView(userId)` собирает:
  - Данные из `app_user`
  - Lifestyle из `user_lifestyle`
- Форматирует красивое текстовое представление
- Inline-кнопки: "Редактировать", "Настройки lifestyle", "Назад"

**Редактирование**:
- Хендлер: `ProfileEditHandler` (order: 25)
- In-memory state: `ProfileEditStateStore`
- Поля: `field` (enum: NAME, AGE, WEIGHT, HEIGHT, TRAINING_LEVEL)
- Флоу:
  1. Пользователь выбирает поле для редактирования
  2. State сохраняет, какое поле редактируется
  3. `ProfileEditHandler.canHandle()` перехватывает следующий апдейт
  4. Валидирует ввод и обновляет через `UserService.updateXXX()`
  5. Очищает state, показывает обновлённый профиль

**Lifestyle-настройки**:
- Отдельная таблица `user_lifestyle` (one-to-one с user)
- Inline-кнопки для переключения флагов: vegan, pregnant
- Текстовый ввод для `smoke_packs_per_day`

**Файлы**:
- `src/main/java/com/nutribot/bot/telegram/handlers/ProfileMenuHandler.java`
- `src/main/java/com/nutribot/bot/telegram/handlers/ProfileEditHandler.java`
- `src/main/java/com/nutribot/bot/profile/ProfileService.java`

---

## Архитектура

### Технологический стек
- **Backend**: Spring Boot 3.5.7, Java 21
- **БД**: PostgreSQL с Flyway миграциями
- **ORM**: Spring Data JDBC (не JPA — легче и быстрее)
- **Telegram API**: `java-telegram-bot-api` (библиотека pengrad)
- **Build**: Gradle

### Диспетчеризация апдейтов

**Паттерн**: Chain of Responsibility

**Компоненты**:
1. `TelegramUpdatesListenerRegistrar` — регистрирует listener для Long Polling
2. `UpdateDispatcher` — главный роутер:
   - Конвертирует `Update` в `UpdateContext` (chatId, userId, text, message, callback)
   - Итерируется по всем `BotUpdateHandler` (sorted by order)
   - Вызывает `canHandle()` для каждого
   - Первый подходящий handler обрабатывает апдейт
3. `BotUpdateHandler` (интерфейс):
   ```java
   boolean canHandle(UpdateContext ctx);
   void handle(UpdateContext ctx);
   int getOrder(); // приоритет (меньше = раньше)
   ```

**Пример порядка**:
- 5: `StartCommandHandler` (перехватывает `/start`)
- 10: `CancelCommandHandler` (перехватывает `/cancel`)
- 15: `OnboardingFlowHandler` (если онбординг активен)
- 25: `ProfileEditHandler` (если редактируется профиль)
- 30: `MainMenuInlineHandler` (inline-кнопки главного меню)
- 40: `WorkoutsMenuHandler` (меню тренировок)
- 45: `StrengthWorkoutAddFlowHandler` (если создаётся силовая)
- 46: `CardioWorkoutAddFlowHandler` (если создаётся кардио)
- 100: `FallbackEchoHandler` (fallback — эхо непонятных сообщений)

### Управление состоянием

**Проблема**: Telegram не хранит состояние диалога.

**Решение**: Гибридный подход

1. **БД** (для долгосрочного состояния):
   - `user_onboarding_state` (текущий шаг онбординга)
   - Таблицы с флагами типа `onboarding_completed`

2. **In-Memory** (для краткосрочного состояния):
   - `WorkoutCreationStateStore` — создание тренировки
   - `CardioCreationStateStore` — кардио-флоу
   - `ProfileEditStateStore` — редактирование профиля
   - Реализация: `ConcurrentHashMap<Long, State>` (userId → state)
   - Очищается после завершения флоу или по таймауту (если добавить)

**Trade-off**: In-memory быстрее, но теряется при перезапуске. Для MVP приемлемо, т.к. флоу короткие.

### Структура БД

**Основные таблицы**:
```
app_user (id, telegram_id, name, sex, age, height_cm, weight_kg,
          training_level, onboarding_completed, created_at)

user_onboarding_state (id, user_id, current_step)

user_lifestyle (user_id PK, smoke_packs_per_day, vegan, pregnant)

workout (id, user_id, type, status, started_at)
  ├─ strength_exercise (id, workout_id, name, order_index)
  │   └─ strength_set (id, exercise_id, weight, reps, rir, order_index)
  └─ cardio_workout_details (workout_id PK, activity_type, duration_min,
                              distance_km, intensity, rpe)

nutrient_definition (code PK, name, unit, description)
```

### Dependency Injection

**Паттерн**: Constructor Injection

Все сервисы и handlers — Spring Beans с `@Component`/`@Service`:
```java
@Component
@RequiredArgsConstructor  // Lombok генерирует конструктор
public class OnboardingFlowHandler implements BotUpdateHandler {
    private final OnboardingService onboardingService;
    private final UserService userService;
    private final TelegramClient tg;
    // ...
}
```

Spring автоматически инжектит зависимости через конструктор.

### Конфигурация

**Файл**: `application.yaml`

**Переменные окружения**:
- `TELEGRAM_BOT_TOKEN` — токен бота (required)
- `DB_HOST`, `DB_PORT`, `DB_NAME` — PostgreSQL
- `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `TELEGRAM_NUTRIENT_EXPLAIN` — фича-флаг для verbose-режима (default: false)

**Профили**:
- `local` — дефолтный, для разработки
- Production профиль можно добавить с другими настройками

---

## Запуск проекта

### Требования
- Java 21
- PostgreSQL
- Telegram Bot Token (получить у @BotFather)

### Локальный запуск

1. Создать БД:
```bash
createdb nutribot
```

2. Настроить переменные окружения:
```bash
export TELEGRAM_BOT_TOKEN="your_token_here"
export SPRING_DATASOURCE_USERNAME="nutribot"
export SPRING_DATASOURCE_PASSWORD="nutribot"
```

3. Запустить приложение:
```bash
./gradlew bootRun
```

Flyway автоматически накатит миграции при старте.

### Docker (если есть Dockerfile)

```bash
docker-compose up
```

---

## Последние обновления

### v0.0.1 (текущая версия)

**Улучшение UX для силовых тренировок** (2025-01)
- ✅ Добавлена возможность множественного ввода подходов
  - Теперь можно вводить все подходы упражнения одним сообщением, разделяя переносами строк
  - Пример: вместо 3 отдельных сообщений можно отправить:
    ```
    80,10,2
    80,9,1
    75,10,2
    ```
  - Бот валидирует все строки сразу и показывает детальные ошибки, если что-то не так
  - Старый способ (по одному подходу) продолжает работать
  - Значительно ускоряет ввод тренировок

---

## Будущие улучшения

### Функциональные
- **Аналитика тренировок**: графики прогресса, статистика по неделям
- **Планы питания**: генерация меню с учётом рекомендаций по нутриентам
- **Интеграция с носимыми устройствами**: синхронизация тренировок с Apple Health / Google Fit
- **Социальные фичи**: соревнования, лиги, шеринг достижений
- **AI-рекомендации**: GPT-4 для персональных советов по питанию

### Технические
- **Redis для state**: перенести in-memory state в Redis для масштабирования
- **WebHook вместо Long Polling**: снизить задержки и нагрузку
- **Кэширование**: Redis для кэша расчётов нутриентов
- **Мониторинг**: Prometheus + Grafana для метрик
- **CI/CD**: автоматический деплой через GitHub Actions
- **Тесты**: покрытие unit и integration тестами (сейчас минимально)

---

## Контакты и вклад

Проект в активной разработке. Если нашли баг или хотите предложить улучшение — welcome to issues/PRs!
