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
 * Формула:
 * Mg = 300 + (FFM × 0.5) + SS × 0.03 + TSS × 0.8 × 8
 *      + (sleep_duration < 7 ? 20 : 0)
 *      + (HRV < 50 ? 15 : 0)
 *      + 45 (доп. расход)
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
            // TSS = 0 для силовых (Mg в группе нейромышечных)
        } else if (ctx.getWorkoutType() == WorkoutType.CARDIO) {
            tss = ctx.getTssOrZero();
            // SS = 0 для кардио
        }

        // MVP-константы
        double sleepDuration = MvpConstants.SLEEP_DURATION_DEFAULT;
        double hrv = MvpConstants.getHrv(ctx.getAgeYears(), ctx.isMale());

        // Базовая формула
        double value = 300.0
                + (ffm * 0.5)
                + (ss * 0.03)
                + (tss * 0.8 * 8.0);

        // Модификаторы
        if (sleepDuration < 7) {
            value += 20.0;
        }
        if (hrv < 50) {
            value += 15.0;
        }

        // Дополнительный расход
        value += MvpConstants.EXTRA_MG;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "Mg = 300 + (FFM × 0.5) + (SS × 0.03) + (TSS × 0.8 × 8) + sleep_bonus + hrv_bonus + 45";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double ffm = ctx.getFfmOrDefault(50.0);
        double ss = ctx.getWorkoutType() == WorkoutType.STRENGTH ? ctx.getSsOrZero() : 0.0;
        double tss = ctx.getWorkoutType() == WorkoutType.CARDIO ? ctx.getTssOrZero() : 0.0;
        double hrv = MvpConstants.getHrv(ctx.getAgeYears(), ctx.isMale());

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("FFM", ffm);
        vars.put("SS", ss);
        vars.put("TSS", tss);
        vars.put("sleep_duration", MvpConstants.SLEEP_DURATION_DEFAULT);
        vars.put("HRV", hrv);
        vars.put("extra", MvpConstants.EXTRA_MG);
        return vars;
    }
}
