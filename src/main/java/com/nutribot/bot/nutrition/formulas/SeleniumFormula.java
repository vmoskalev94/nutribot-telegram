package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import com.nutribot.bot.workout.WorkoutType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Селен (Se)
 * <p>
 * Формула (PDF):
 * Se = clamp(55 + (TSH > 3.0 ? 20 : 0) + heavy_metal_exposure×10 + (SS+TSS)×0.0001 + EXTRA,
 *            MIN=55, MAX=300)
 * <p>
 * Единицы: мкг
 * UL = 400 мкг. Токсичен при длительном превышении.
 * <p>
 * Особенности:
 * - heavy_metal_exposure: не курит = 1, курит = 2
 */
@Component
public class SeleniumFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "SE";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double tsh = MvpConstants.TSH;
        double smokePacks = ctx.getSmokePacksOrZero();
        double heavyMetalExposure = MvpConstants.getHeavyMetalExposure(smokePacks);

        // SS и TSS
        double ss = 0.0;
        double tss = ctx.getTssOrZero();
        if (ctx.getWorkoutType() == WorkoutType.STRENGTH) {
            ss = ctx.getSsOrZero();
        }

        // Базовая формула по PDF
        double value = 55.0;

        // Модификаторы
        if (tsh > 3.0) {
            value += 20.0;
        }
        value += heavyMetalExposure * 10.0;
        value += (ss + tss) * 0.0001;

        // Дополнительный расход
        value += MvpConstants.EXTRA_SE;

        // Clamp в диапазон [MIN, MAX]
        return NutrientLimits.clamp(value, NutrientLimits.SE_MIN, NutrientLimits.SE_MAX);
    }

    @Override
    public String template() {
        return "Se = clamp(55 + tsh_bonus + heavy_metal×10 + (SS+TSS)×0.0001 + extra, 55, 300)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double tsh = MvpConstants.TSH;
        double smokePacks = ctx.getSmokePacksOrZero();
        double heavyMetalExposure = MvpConstants.getHeavyMetalExposure(smokePacks);

        double ss = ctx.getWorkoutType() == WorkoutType.STRENGTH ? ctx.getSsOrZero() : 0.0;
        double tss = ctx.getTssOrZero();

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("TSH", tsh);
        vars.put("tsh_bonus", tsh > 3.0 ? 20.0 : 0.0);
        vars.put("heavy_metal_exposure", heavyMetalExposure);
        vars.put("SS", ss);
        vars.put("TSS", tss);
        vars.put("extra", MvpConstants.EXTRA_SE);
        vars.put("min", NutrientLimits.SE_MIN);
        vars.put("max", NutrientLimits.SE_MAX);
        return vars;
    }
}
