package com.nutribot.bot.nutrition;

/**
 * MVP-константы для расчёта нутриентов.
 * <p>
 * Все значения, которые в будущем будут получаться из лабораторных данных,
 * устройств или ввода пользователя, пока захардкожены здесь.
 */
public final class MvpConstants {

    private MvpConstants() {
        // Utility class
    }

    // ==================== Географические ====================

    /**
     * Широта (Москва). Используется для расчёта витамина D3.
     * > 40° означает низкую инсоляцию.
     */
    public static final double LATITUDE = 55.75222;

    /**
     * AQI (Air Quality Index) — индекс качества воздуха.
     * Используется для расчёта витамина C.
     */
    public static final double AQI = 41.0;

    /**
     * Уровень загрязнения воздуха (шкала 1-5).
     * Используется для расчёта витамина E.
     */
    public static final double POLLUTION_LEVEL = 2.0;

    // ==================== Лабораторные маркеры ====================

    /**
     * CRP (С-реактивный белок) в мг/л.
     * 2.5 — умеренное воспаление после тяжёлой тренировки.
     */
    public static final double CRP = 2.5;

    /**
     * Omega-3 Index в %.
     * 5% — средний показатель по РФ.
     */
    public static final double OMEGA3_INDEX = 5.0;

    /**
     * Гомоцистеин в мкмоль/л.
     * 8 — среднее значение для здорового взрослого.
     */
    public static final double HOMOCYSTEINE = 8.0;

    /**
     * TSH (тиреотропный гормон) в мМЕ/л.
     * Используется для расчёта селена.
     */
    public static final double TSH = 2.0;

    // ==================== Физиологические ====================

    /**
     * Средний пульс (HR_avg) для кардио тренировок.
     * В будущем будет получаться с устройств.
     */
    public static final double HR_AVG = 70.0;

    /**
     * Донации крови за 90 дней.
     * На MVP = 0.
     */
    public static final double BLOOD_DONATION = 0.0;

    /**
     * Объём щитовидной железы в мл.
     * Используется для расчёта йода.
     */
    public static final double THYROID_VOLUME = 1.0;

    /**
     * Менструальные потери в мл/цикл.
     * Используется для расчёта железа у женщин.
     */
    public static final double MENSTRUAL_LOSS = 30.0;

    // ==================== Поведенческие ====================

    /**
     * Количество пачек сигарет в день (для некурящих).
     */
    public static final double SMOKE_PACKS_DEFAULT = 0.0;

    /**
     * Годы веганства (для не-веганов).
     */
    public static final double VEGAN_YEARS_DEFAULT = 0.0;

    /**
     * Продолжительность сна в часах (для расчёта магния).
     * Пока не запрашиваем у пользователя.
     */
    public static final double SLEEP_DURATION_DEFAULT = 7.0;

    // ==================== Медицинские флаги ====================

    /**
     * Приём варфарина (антикоагулянты).
     */
    public static final boolean WARFARIN = false;

    /**
     * Мутация MTHFR.
     */
    public static final boolean MTHFR_MUTATION = false;

    // ==================== Дополнительный расход ====================

    /**
     * Фиксированная надбавка за тренировку по каждому нутриенту.
     */
    public static final double EXTRA_MG = 45.0;
    public static final double EXTRA_D3 = 200.0;
    public static final double EXTRA_FE = 0.8;
    public static final double EXTRA_ZN = 2.5;
    public static final double EXTRA_OMEGA3 = 300.0;
    public static final double EXTRA_B6 = 0.4;
    public static final double EXTRA_CA = 15.0;
    public static final double EXTRA_C = 35.0;
    public static final double EXTRA_B12 = 0.3;
    public static final double EXTRA_SE = 12.0;
    public static final double EXTRA_CU = 0.1;
    public static final double EXTRA_E = 3.0;
    public static final double EXTRA_I = 15.0;
    public static final double EXTRA_K = 20.0;
    public static final double EXTRA_B9 = 40.0;

    // ==================== Таблицы по группам ====================

    /**
     * Потребление белка в г/сутки.
     * Рассчитывается как вес × 1.5 (типичная норма для спортсменов).
     *
     * @param weightKg вес в кг
     * @return потребление белка в г/сутки
     */
    public static double getProteinIntake(double weightKg) {
        return weightKg * 1.5;
    }

    /**
     * Церулоплазмин в мг/дл по группам.
     */
    public static double getCeruloplasmin(boolean isMale, boolean isPregnant, boolean isAthlete, boolean isVegan) {
        if (isPregnant) return 50.0;
        if (isVegan) return 28.0;
        if (isAthlete) return 38.0;
        return isMale ? 31.0 : 35.0;
    }

    /**
     * Ферритин в нг/мл по группам.
     */
    public static double getFerritin(boolean isMale, Integer age, boolean isPregnant, boolean isVegan) {
        if (isPregnant) return 25.0;
        if (isVegan) return 35.0;
        if (isMale) return 80.0;

        // Женщины
        int effectiveAge = (age != null) ? age : 30;
        return (effectiveAge > 50) ? 75.0 : 45.0;
    }

    /**
     * HRV по возрасту и полу (для расчёта магния).
     * Возвращает среднее значение для возрастной группы.
     */
    public static double getHrv(Integer age, boolean isMale) {
        int effectiveAge = (age != null) ? age : 30;

        // Упрощённая таблица HRV
        if (effectiveAge <= 26) return isMale ? 61.0 : 57.0;
        if (effectiveAge <= 31) return isMale ? 56.0 : 53.0;
        if (effectiveAge <= 36) return isMale ? 49.0 : 47.0;
        if (effectiveAge <= 41) return isMale ? 43.0 : 42.0;
        if (effectiveAge <= 46) return isMale ? 37.0 : 37.0;
        if (effectiveAge <= 51) return isMale ? 34.0 : 34.0;
        if (effectiveAge <= 56) return isMale ? 32.0 : 33.0;
        return isMale ? 31.0 : 31.0; // 60+
    }

    /**
     * Heavy metal exposure (для расчёта селена).
     * Курильщики = 2, остальные = 1.
     */
    public static double getHeavyMetalExposure(double smokePacksPerDay) {
        return smokePacksPerDay > 0 ? 2.0 : 1.0;
    }
}
