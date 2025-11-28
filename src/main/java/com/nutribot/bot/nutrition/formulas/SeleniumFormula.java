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
 * Формула:
 * Se = (TSH × 0.8) + (heavy_metal_exposure × 15) + (SS + TSS) × 0.5 + 12 (доп. расход)
 * <p>
 * Группа 2: для силовых используем SS + TSS, для кардио только TSS.
 * <p>
 * Особенности:
 * - TSH = 2.0 мМЕ/л (MVP)
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

        // Группа 2: силовая → SS + TSS, кардио → только TSS
        double ss = 0.0;
        double tss = ctx.getTssOrZero();
        if (ctx.getWorkoutType() == WorkoutType.STRENGTH) {
            ss = ctx.getSsOrZero();
        }

        double value = (tsh * 0.8)
                + (heavyMetalExposure * 15.0)
                + ((ss + tss) * 0.5);

        // Дополнительный расход
        value += MvpConstants.EXTRA_SE;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "Se = (TSH × 0.8) + (heavy_metal_exposure × 15) + ((SS + TSS) × 0.5) + 12";
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
        vars.put("heavy_metal_exposure", heavyMetalExposure);
        vars.put("SS", ss);
        vars.put("TSS", tss);
        vars.put("extra", MvpConstants.EXTRA_SE);
        return vars;
    }
}
