package com.nutribot.bot.nutrition;

/**
 * Калькулятор показателей состава тела: FFM (безжировая масса) и bone_mass (костная масса).
 * <p>
 * Используется в формулах расчёта нутриентов.
 */
public final class BodyCompositionCalculator {

    private BodyCompositionCalculator() {
        // Utility class
    }

    /**
     * Рассчитывает FFM (Fat-Free Mass / безжировая масса тела).
     * <p>
     * Формулы:
     * - Мужчины: FFM = 0.407 × weight + 0.267 × height - 19.2
     * - Женщины: FFM = 0.252 × weight + 0.473 × height - 48.3
     * <p>
     * Модификаторы:
     * - Для атлетов (PRO): FFM × 1.15
     * - Защита: min 30 кг, max 95% от веса
     *
     * @param weightKg      вес в кг
     * @param heightCm      рост в см
     * @param isMale        true для мужчин
     * @param isAthlete     true для атлетов (training_level = PRO)
     * @return FFM в кг
     */
    public static double calculateFfm(Double weightKg, Integer heightCm, boolean isMale, boolean isAthlete) {
        double weight = (weightKg != null && weightKg > 0) ? weightKg : 70.0;
        int height = (heightCm != null && heightCm > 0) ? heightCm : 175;

        double ffm;
        if (isMale) {
            ffm = 0.407 * weight + 0.267 * height - 19.2;
        } else {
            ffm = 0.252 * weight + 0.473 * height - 48.3;
        }

        // Модификатор для атлетов: +15%
        if (isAthlete) {
            ffm *= 1.15;
        }

        // Защита от нереалистичных значений
        ffm = Math.max(ffm, 30.0);          // Минимум 30 кг
        ffm = Math.min(ffm, weight * 0.95); // Максимум 95% от веса

        return Math.round(ffm * 10.0) / 10.0; // Округляем до 1 знака
    }

    /**
     * Рассчитывает bone_mass (костная масса) для формул кальция и витамина K.
     * <p>
     * Формулы:
     * - Мужчины: bone_mass = 0.21 × weight + 0.18 × height - 12.2
     * - Женщины: bone_mass = 0.14 × weight + 0.22 × height - 5.4
     *
     * @param weightKg вес в кг
     * @param heightCm рост в см
     * @param isMale   true для мужчин
     * @return bone_mass в кг
     */
    public static double calculateBoneMass(Double weightKg, Integer heightCm, boolean isMale) {
        double weight = (weightKg != null && weightKg > 0) ? weightKg : 70.0;
        int height = (heightCm != null && heightCm > 0) ? heightCm : 175;

        double boneMass;
        if (isMale) {
            boneMass = 0.21 * weight + 0.18 * height - 12.2;
        } else {
            boneMass = 0.14 * weight + 0.22 * height - 5.4;
        }

        // Защита от отрицательных значений
        return Math.max(boneMass, 1.0);
    }

    /**
     * Рассчитывает sweat_rate для формулы цинка.
     * <p>
     * sweat_rate = base_sweat × 0.2
     * где base_sweat: мужчины = 1.0, женщины = 0.75
     *
     * @param isMale true для мужчин
     * @return sweat_rate
     */
    public static double calculateSweatRate(boolean isMale) {
        double baseSweat = isMale ? 1.0 : 0.75;
        return baseSweat * 0.2;
    }

    /**
     * Определяет, является ли пользователь атлетом по уровню подготовки.
     *
     * @param trainingLevel уровень подготовки (BEGINNER, AMATEUR, PRO)
     * @return true если PRO
     */
    public static boolean isAthlete(String trainingLevel) {
        return "PRO".equalsIgnoreCase(trainingLevel);
    }

    /**
     * Определяет пол по строковому значению.
     *
     * @param sex строка пола (M/F или MALE/FEMALE)
     * @return true если мужчина
     */
    public static boolean isMale(String sex) {
        if (sex == null) {
            return true; // Дефолт — мужчина
        }
        String s = sex.trim().toUpperCase();
        return s.startsWith("M") || s.equals("MALE") || s.equals("М");
    }
}
