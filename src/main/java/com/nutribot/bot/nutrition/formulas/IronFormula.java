package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import com.nutribot.bot.workout.WorkoutType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Железо (Fe)
 * <p>
 * Формула:
 * Fe = (FFM × 0.1) + (blood_donation × 50) + TSS × 0.5 + (женщина ? menstrual_loss × 0.5 : 0) + 0.8 (доп. расход)
 * <p>
 * Группа 2: для силовых используем SS + TSS, для кардио только TSS.
 * <p>
 * Особенности:
 * - blood_donation = 0 (MVP)
 * - menstrual_loss = 30 мл/цикл (MVP)
 */
@Component
public class IronFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "FE";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double ffm = ctx.getFfmOrDefault(50.0);
        double bloodDonation = MvpConstants.BLOOD_DONATION;
        boolean isFemale = !ctx.isMale();
        double menstrualLoss = MvpConstants.MENSTRUAL_LOSS;

        // Группа 2: силовая → SS + TSS, кардио → только TSS
        double ss = 0.0;
        double tss = ctx.getTssOrZero();
        if (ctx.getWorkoutType() == WorkoutType.STRENGTH) {
            ss = ctx.getSsOrZero();
        }

        double value = (ffm * 0.1)
                + (bloodDonation * 50.0)
                + ((ss + tss) * 0.5);

        // Учёт менструальных потерь для женщин
        if (isFemale) {
            value += menstrualLoss * 0.5;
        }

        // Дополнительный расход
        value += MvpConstants.EXTRA_FE;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "Fe = (FFM × 0.1) + (blood_donation × 50) + ((SS + TSS) × 0.5) + menstrual_bonus + 0.8";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double ffm = ctx.getFfmOrDefault(50.0);
        double bloodDonation = MvpConstants.BLOOD_DONATION;
        boolean isFemale = !ctx.isMale();
        double menstrualLoss = MvpConstants.MENSTRUAL_LOSS;

        double ss = ctx.getWorkoutType() == WorkoutType.STRENGTH ? ctx.getSsOrZero() : 0.0;
        double tss = ctx.getTssOrZero();

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("FFM", ffm);
        vars.put("blood_donation", bloodDonation);
        vars.put("SS", ss);
        vars.put("TSS", tss);
        vars.put("is_female", isFemale);
        vars.put("menstrual_loss", menstrualLoss);
        vars.put("menstrual_bonus", isFemale ? menstrualLoss * 0.5 : 0.0);
        vars.put("extra", MvpConstants.EXTRA_FE);
        return vars;
    }
}
