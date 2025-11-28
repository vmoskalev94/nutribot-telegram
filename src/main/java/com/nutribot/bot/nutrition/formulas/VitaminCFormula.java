package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import com.nutribot.bot.workout.WorkoutType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Витамин C
 * <p>
 * Формула:
 * C = (вес × 1.0) + (smoke_packs × 50) + (AQI × 0.1) + (SS + TSS) × 0.005 + 35 (доп. расход)
 * <p>
 * Особенности:
 * - Комбинированный SS + TSS (антиоксидантная защита)
 * - smoke_packs из lifestyle
 * - AQI = 41 (MVP)
 * - Для силовых: TSS = 0
 * - Для кардио: SS = 0
 */
@Component
public class VitaminCFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "C";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double weight = ctx.getWeightOrDefault(70.0);
        double smokePacks = ctx.getSmokePacksOrZero();
        double aqi = MvpConstants.AQI;

        // SS и TSS в зависимости от типа тренировки
        double ss = 0.0;
        double tss = 0.0;
        if (ctx.getWorkoutType() == WorkoutType.STRENGTH) {
            ss = ctx.getSsOrZero();
            // TSS = 0 для силовых (Vit C в группе нейромышечных)
        } else if (ctx.getWorkoutType() == WorkoutType.CARDIO) {
            tss = ctx.getTssOrZero();
            // SS = 0 для кардио
        }

        double value = (weight * 1.0)
                + (smokePacks * 50.0)
                + (aqi * 0.1)
                + ((ss + tss) * 0.005);

        // Дополнительный расход
        value += MvpConstants.EXTRA_C;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "C = (вес × 1.0) + (smoke_packs × 50) + (AQI × 0.1) + ((SS + TSS) × 0.005) + 35";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double weight = ctx.getWeightOrDefault(70.0);
        double smokePacks = ctx.getSmokePacksOrZero();
        double aqi = MvpConstants.AQI;
        double ss = ctx.getWorkoutType() == WorkoutType.STRENGTH ? ctx.getSsOrZero() : 0.0;
        double tss = ctx.getWorkoutType() == WorkoutType.CARDIO ? ctx.getTssOrZero() : 0.0;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("weight", weight);
        vars.put("smoke_packs", smokePacks);
        vars.put("AQI", aqi);
        vars.put("SS", ss);
        vars.put("TSS", tss);
        vars.put("extra", MvpConstants.EXTRA_C);
        return vars;
    }
}
