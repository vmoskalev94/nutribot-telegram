package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import com.nutribot.bot.workout.WorkoutType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Омега-3 (EPA + DHA)
 * <p>
 * Формула:
 * Ω3 = (omega3_index < 8 ? 2000 : 1000) + (CRP × 50) + (SS + TSS) × 0.03 + 300 (доп. расход)
 * <p>
 * Особенности:
 * - Комбинированный SS + TSS (нутриент связан с восстановлением мышц)
 * - omega3_index = 5% (MVP)
 * - CRP = 2.5 мг/л (MVP)
 * - Для силовых: TSS = 0
 * - Для кардио: SS = 0
 */
@Component
public class Omega3Formula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "OMEGA3";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        // MVP-константы
        double omega3Index = MvpConstants.OMEGA3_INDEX;
        double crp = MvpConstants.CRP;

        // SS и TSS в зависимости от типа тренировки
        double ss = 0.0;
        double tss = 0.0;
        if (ctx.getWorkoutType() == WorkoutType.STRENGTH) {
            ss = ctx.getSsOrZero();
            // TSS = 0 для силовых (Ω3 в группе нейромышечных)
        } else if (ctx.getWorkoutType() == WorkoutType.CARDIO) {
            tss = ctx.getTssOrZero();
            // SS = 0 для кардио
        }

        // Базовая формула
        double baseValue = (omega3Index < 8) ? 2000.0 : 1000.0;
        double value = baseValue
                + (crp * 50.0)
                + ((ss + tss) * 0.03);

        // Дополнительный расход
        value += MvpConstants.EXTRA_OMEGA3;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "Ω3 = (omega3_index < 8 ? 2000 : 1000) + (CRP × 50) + ((SS + TSS) × 0.03) + 300";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double omega3Index = MvpConstants.OMEGA3_INDEX;
        double crp = MvpConstants.CRP;
        double ss = ctx.getWorkoutType() == WorkoutType.STRENGTH ? ctx.getSsOrZero() : 0.0;
        double tss = ctx.getWorkoutType() == WorkoutType.CARDIO ? ctx.getTssOrZero() : 0.0;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("omega3_index", omega3Index);
        vars.put("CRP", crp);
        vars.put("SS", ss);
        vars.put("TSS", tss);
        vars.put("extra", MvpConstants.EXTRA_OMEGA3);
        return vars;
    }
}
