package com.nutribot.bot.nutrition.formulas;

/**
 * Лимиты (MIN/MAX) для всех нутриентов.
 * <p>
 * MIN — минимальная рекомендуемая доза.
 * MAX — верхний допустимый уровень (UL, Upper Limit).
 * <p>
 * Формула расчёта: clamp(calculated, MIN, MAX)
 */
public final class NutrientLimits {

    private NutrientLimits() {
        // Utility class
    }

    // ==================== Магний (Mg) ====================
    /** Минимум: 300 мг */
    public static final double MG_MIN = 300.0;
    /** Максимум (UL): 500 мг */
    public static final double MG_MAX = 500.0;

    // ==================== Цинк (Zn) ====================
    /** Минимум: 8 мг */
    public static final double ZN_MIN = 8.0;
    /** Максимум (UL): 30 мг */
    public static final double ZN_MAX = 30.0;

    // ==================== Витамин B6 ====================
    /** Минимум: 1.3 мг */
    public static final double B6_MIN = 1.3;
    /** Максимум (UL): 20 мг (токсичен при >100 мг/сут) */
    public static final double B6_MAX = 20.0;

    // ==================== Витамин D3 ====================
    /** Минимум: 600 МЕ */
    public static final double D3_MIN = 600.0;
    /** Максимум (UL): 4000 МЕ */
    public static final double D3_MAX = 4000.0;

    // ==================== Железо (Fe) ====================
    /** Минимум: 6 мг */
    public static final double FE_MIN = 6.0;
    /** Максимум (UL): 20 мг */
    public static final double FE_MAX = 20.0;

    // ==================== Омега-3 (Ω3) ====================
    /** Минимум: 250 мг */
    public static final double OMEGA3_MIN = 250.0;
    /** Максимум (UL): 3000 мг */
    public static final double OMEGA3_MAX = 3000.0;

    // ==================== Витамин C ====================
    /** Минимум: 90 мг */
    public static final double C_MIN = 90.0;
    /** Максимум (UL): 2000 мг */
    public static final double C_MAX = 2000.0;

    // ==================== Кальций (Ca) ====================
    /** Минимум: 800 мг */
    public static final double CA_MIN = 800.0;
    /** Максимум (UL): 2500 мг */
    public static final double CA_MAX = 2500.0;

    // ==================== Витамин B12 ====================
    /** Минимум: 2.4 мкг */
    public static final double B12_MIN = 2.4;
    /** Максимум (UL): 1000 мкг (безопасен даже при высоких дозах) */
    public static final double B12_MAX = 1000.0;

    // ==================== Селен (Se) ====================
    /** Минимум: 55 мкг */
    public static final double SE_MIN = 55.0;
    /** Максимум (UL): 300 мкг (реальный UL = 400, но используем 300 для безопасности) */
    public static final double SE_MAX = 300.0;

    // ==================== Медь (Cu) ====================
    /** Минимум: 0.9 мг */
    public static final double CU_MIN = 0.9;
    /** Максимум (UL): 10 мг */
    public static final double CU_MAX = 10.0;

    // ==================== Витамин E ====================
    /** Минимум: 15 мг */
    public static final double E_MIN = 15.0;
    /** Максимум (UL): 1000 мг */
    public static final double E_MAX = 1000.0;

    // ==================== Йод (I) ====================
    /** Минимум: 150 мкг */
    public static final double I_MIN = 150.0;
    /** Максимум (UL): 1100 мкг */
    public static final double I_MAX = 1100.0;

    // ==================== Витамин K ====================
    /** Минимум: 90 мкг */
    public static final double K_MIN = 90.0;
    /** Максимум (UL): 1000 мкг */
    public static final double K_MAX = 1000.0;

    // ==================== Фолат (B9) ====================
    /** Минимум: 400 мкг */
    public static final double B9_MIN = 400.0;
    /** Максимум (UL): 1000 мкг */
    public static final double B9_MAX = 1000.0;

    // ==================== Утилитный метод ====================

    /**
     * Ограничивает значение в диапазоне [min, max].
     *
     * @param value исходное значение
     * @param min   минимум
     * @param max   максимум
     * @return значение в диапазоне [min, max]
     */
    public static double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }
}
