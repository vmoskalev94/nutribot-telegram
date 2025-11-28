package com.nutribot.bot.nutrition;

import com.nutribot.bot.workout.WorkoutType;
import lombok.Builder;
import lombok.Value;

/**
 * Контекст для расчёта нутриентов:
 * - данные пользователя,
 * - параметры конкретной тренировки,
 * - lifestyle-факторы,
 * - рассчитанные метрики нагрузки (SS, TSS),
 * - производные показатели (FFM, bone_mass).
 * <p>
 * Все поля, которые нужны формулам, собираются здесь,
 * чтобы каждая формула не ходила за данными самостоятельно.
 */
@Value
@Builder(toBuilder = true)
public class NutrientContext {

    Long userId;
    Long workoutId;

    // ==================== Профиль пользователя ====================

    String sex;              // "M"/"F"
    Integer ageYears;
    Double weightKg;
    Integer heightCm;
    String trainingLevel;    // "BEGINNER" / "AMATEUR" / "PRO"

    // ==================== Параметры тренировки ====================

    WorkoutType workoutType;
    Integer durationMin;     // для кардио
    Double distanceKm;       // для кардио
    Integer cardioRpe;       // RPE кардио (1–10)

    // ==================== Метрики нагрузки ====================

    /**
     * SS (Strength Score) — для силовых тренировок.
     * Для кардио = 0.
     */
    Double ss;

    /**
     * TSS (Training Stress Score) — для кардио тренировок.
     * Для силовых = 0 (в группе Mg, Zn, B6, Ω3, C).
     * Для остальных нутриентов может использоваться и для силовых.
     */
    Double tss;

    /**
     * @deprecated Используй ss и tss отдельно.
     * Оставлено для обратной совместимости.
     */
    @Deprecated
    Double tssTotal;

    // ==================== Производные показатели тела ====================

    /**
     * FFM (Fat-Free Mass) — безжировая масса тела в кг.
     * Рассчитывается из веса, роста, пола и уровня подготовки.
     */
    Double ffm;

    /**
     * Костная масса в кг.
     * Используется для расчёта кальция и витамина K.
     */
    Double boneMass;

    /**
     * Sweat rate — скорость потоотделения.
     * Используется для расчёта цинка.
     */
    Double sweatRate;

    // ==================== Lifestyle ====================

    Double smokePacksPerDay;
    Boolean vegan;
    Boolean pregnant;

    Double sunExposureMinutes;
    // ==================== Вспомогательные флаги ====================

    /**
     * Является ли пользователь мужчиной.
     * Вычисляется из sex для удобства формул.
     */
    Boolean male;

    /**
     * Является ли пользователь атлетом (PRO уровень).
     * Вычисляется из trainingLevel для удобства формул.
     */
    Boolean athlete;

    // ==================== Утилитные методы ====================

    /**
     * Возвращает SS или 0 если null.
     */
    public double getSsOrZero() {
        return ss != null ? ss : 0.0;
    }

    /**
     * Возвращает TSS или 0 если null.
     */
    public double getTssOrZero() {
        return tss != null ? tss : 0.0;
    }

    /**
     * Возвращает FFM или дефолтное значение.
     */
    public double getFfmOrDefault(double defaultValue) {
        return ffm != null ? ffm : defaultValue;
    }

    /**
     * Возвращает bone_mass или дефолтное значение.
     */
    public double getBoneMassOrDefault(double defaultValue) {
        return boneMass != null ? boneMass : defaultValue;
    }

    /**
     * Возвращает sweat_rate или дефолтное значение.
     */
    public double getSweatRateOrDefault(double defaultValue) {
        return sweatRate != null ? sweatRate : defaultValue;
    }

    /**
     * Проверяет, мужчина ли пользователь.
     */
    public boolean isMale() {
        return male != null ? male : true;
    }

    /**
     * Проверяет, является ли пользователь атлетом.
     */
    public boolean isAthlete() {
        return athlete != null && athlete;
    }

    /**
     * Проверяет, беременна ли пользователь.
     */
    public boolean isPregnant() {
        return pregnant != null && pregnant;
    }

    /**
     * Проверяет, веган ли пользователь.
     */
    public boolean isVegan() {
        return vegan != null && vegan;
    }

    /**
     * Возвращает количество пачек сигарет в день или 0.
     */
    public double getSmokePacksOrZero() {
        return smokePacksPerDay != null ? smokePacksPerDay : 0.0;
    }

    /**
     * Возвращает возраст или дефолтное значение.
     */
    public int getAgeOrDefault(int defaultValue) {
        return ageYears != null ? ageYears : defaultValue;
    }

    /**
     * Возвращает вес или дефолтное значение.
     */
    public double getWeightOrDefault(double defaultValue) {
        return weightKg != null ? weightKg : defaultValue;
    }
}

