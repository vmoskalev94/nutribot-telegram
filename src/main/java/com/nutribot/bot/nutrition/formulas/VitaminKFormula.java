package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Витамин K
 * <p>
 * Формула (PDF):
 * K = clamp(120 + bone_mass×1 + (warfarin ? 100 : 0) + EXTRA,
 *           MIN=90, MAX=1000)
 * <p>
 * Единицы: мкг
 * Варфарин блокирует K. Синергия с D3 и Ca.
 * <p>
 * Особенности:
 * - warfarin = false (MVP)
 */
@Component
public class VitaminKFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "K";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double boneMass = ctx.getBoneMassOrDefault(3.0);
        boolean warfarin = MvpConstants.WARFARIN;

        // Базовая формула по PDF
        double value = 120.0
                + (boneMass * 1.0);

        // Модификатор варфарина
        if (warfarin) {
            value += 100.0;
        }

        // Дополнительный расход
        value += MvpConstants.EXTRA_K;

        // Clamp в диапазон [MIN, MAX]
        return NutrientLimits.clamp(value, NutrientLimits.K_MIN, NutrientLimits.K_MAX);
    }

    @Override
    public String template() {
        return "K = clamp(120 + bone_mass×1 + warfarin_bonus + extra, 90, 1000)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double boneMass = ctx.getBoneMassOrDefault(3.0);
        boolean warfarin = MvpConstants.WARFARIN;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("bone_mass", boneMass);
        vars.put("warfarin", warfarin);
        vars.put("warfarin_bonus", warfarin ? 100.0 : 0.0);
        vars.put("extra", MvpConstants.EXTRA_K);
        vars.put("min", NutrientLimits.K_MIN);
        vars.put("max", NutrientLimits.K_MAX);
        return vars;
    }
}
