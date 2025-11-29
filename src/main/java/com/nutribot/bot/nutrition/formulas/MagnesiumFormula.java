package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import com.nutribot.bot.workout.WorkoutType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Магний (Mg)
 * <p>
 * Формула (PDF):
 * Mg = clamp(300 + FFM×0.5 + SS×0.03 + TSS×0.08
 *            + (sleep < 7 ? 20 : 0)
 *            + (HRV < 50 ? 15 : 0)
 *            + EXTRA,
 *            MIN=300, MAX=500)
 * <p>
 * Единицы: мг
 * Синергия: B6, K. Избыток кальция снижает усвоение.
 * <p>
 * Особенности:
 * - Силовая: SS считаем, TSS = 0
 * - Кардио: TSS считаем, SS = 0
 */
@Component
public class MagnesiumFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "MG";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double ffm = ctx.getFfmOrDefault(50.0);

        // SS и TSS в зависимости от типа тренировки
        double ss = 0.0;
        double tss = 0.0;
        if (ctx.getWorkoutType() == WorkoutType.STRENGTH) {
            ss = ctx.getSsOrZero();
        } else if (ctx.getWorkoutType() == WorkoutType.CARDIO) {
            tss = ctx.getTssOrZero();
        }

        // MVP-константы
        double sleepDuration = MvpConstants.SLEEP_DURATION_DEFAULT;
        double hrv = MvpConstants.getHrv(ctx.getAgeYears(), ctx.isMale());

        // Базовая формула по PDF
        double value = 300.0
                + (ffm * 0.5)
                + (ss * 0.03)
                + (tss * 0.08);

        // Модификаторы
        if (sleepDuration < 7) {
            value += 20.0;
        }
        if (hrv < 50) {
            value += 15.0;
        }

        // Дополнительный расход
        value += MvpConstants.EXTRA_MG;

        // Clamp в диапазон [MIN, MAX]
        return NutrientLimits.clamp(value, NutrientLimits.MG_MIN, NutrientLimits.MG_MAX);
    }

    @Override
    public String template() {
        return "Mg = clamp(300 + FFM×0.5 + SS×0.03 + TSS×0.08 + sleep_bonus + hrv_bonus + extra, 300, 500)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double ffm = ctx.getFfmOrDefault(50.0);
        double ss = ctx.getWorkoutType() == WorkoutType.STRENGTH ? ctx.getSsOrZero() : 0.0;
        double tss = ctx.getWorkoutType() == WorkoutType.CARDIO ? ctx.getTssOrZero() : 0.0;
        double sleepDuration = MvpConstants.SLEEP_DURATION_DEFAULT;
        double hrv = MvpConstants.getHrv(ctx.getAgeYears(), ctx.isMale());

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("FFM", ffm);
        vars.put("SS", ss);
        vars.put("TSS", tss);
        vars.put("sleep_duration", sleepDuration);
        vars.put("sleep_bonus", sleepDuration < 7 ? 20.0 : 0.0);
        vars.put("HRV", hrv);
        vars.put("hrv_bonus", hrv < 50 ? 15.0 : 0.0);
        vars.put("extra", MvpConstants.EXTRA_MG);
        vars.put("min", NutrientLimits.MG_MIN);
        vars.put("max", NutrientLimits.MG_MAX);
        return vars;
    }
}
