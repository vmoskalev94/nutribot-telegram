package com.nutribot.bot.nutrition;

import java.util.Map;

/**
 * Формула для расчёта одного нутриента.
 * <p>
 * Сейчас:
 * - code()      — код нутриента (MG, D3 и т.п.);
 * - calculate() — числовой расчёт;
 * - template()  — строка с формулой;
 * - vars()      — карта переменных и их значений для подстановки.
 */
public interface ExplainableNutrientFormula {

    /**
     * Код нутриента: MG, D3, PROTEIN и т.п.
     */
    String code();

    /**
     * Расчёт рекомендуемого количества нутриента
     * на основе контекста пользователя и тренировки.
     */
    double calculate(NutrientContext ctx);

    /**
     * Человекочитаемый шаблон формулы.
     * <p>
     * Пример:
     * "MG = 300 + (weight_kg * 1.5) + (height_cm_minus_175 * 0.2) + (tss_total * 12)"
     */
    String template();

    /**
     * Значения переменных, которые используются в формуле.
     * Ключи должны соответствовать обозначениям из template().
     */
    Map<String, Object> vars(NutrientContext ctx);
}
