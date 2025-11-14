package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.NutrientContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Фолат (B9)
 * <p>
 * Формула из спецификации:
 * B9 = (dna_replication_rate * 0.01)
 * + (mthfr_mutation * 200)
 * + TSS_total * 0.2
 * <p>
 * MVP:
 * - dna_replication_rate = 3
 * - mthfr_mutation = 1.3
 */
@Component
public class FolateB9Formula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "B9";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double tss = orDefault(ctx.getTssTotal(), 0.0);

        double dnaReplicationRate = 3.0;
        double mthfrMutation = 1.3;

        double value = dnaReplicationRate * 0.01
                + mthfrMutation * 200.0
                + tss * 0.2;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "B9 = (dna_replication_rate * 0.01) + (mthfr_mutation * 200) + (tss_total * 0.2)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double tss = orDefault(ctx.getTssTotal(), 0.0);
        double dnaReplicationRate = 3.0;
        double mthfrMutation = 1.3;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("dna_replication_rate", dnaReplicationRate);
        vars.put("mthfr_mutation", mthfrMutation);
        vars.put("tss_total", tss);
        return vars;
    }

    private double orDefault(Number n, double def) {
        return n == null ? def : n.doubleValue();
    }
}
