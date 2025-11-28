package com.nutribot.bot.nutrition;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Сервис, который из результата расчёта строит "человеческое" объяснение.
 * <p>
 * Используется в "подробном режиме" и по кнопке «Пояснить расчёт» (шаг 5.2).
 */
@Service
@RequiredArgsConstructor
public class NutrientExplanationService {

    private final List<ExplainableNutrientFormula> formulas;

    /**
     * Удобный вход: сразу на основе результата калькулятора.
     */
    public String buildExplanation(NutrientCalculationResult result) {
        return buildExplanation(result.context(), result.recommendations());
    }

    /**
     * Основной метод: объяснение по контексту и списку рекомендаций.
     */
    public String buildExplanation(NutrientContext ctx, List<NutrientRecommendation> recommendations) {
        StringBuilder sb = new StringBuilder();

        sb.append("Подробное объяснение расчёта нутриентов 💊\n\n");
        appendInputs(sb, ctx);

        if (recommendations == null || recommendations.isEmpty()) {
            sb.append("\nПока нет ни одной активной рекомендации по нутриентам.\n")
                    .append("Возможно, формулы ещё не настроены или нагрузка слишком мала.");
            return sb.toString();
        }

        // Индекс формул по коду нутриента
        Map<String, ExplainableNutrientFormula> formulaByCode =
                formulas == null ? Map.of() :
                        formulas.stream().collect(Collectors.toMap(
                                ExplainableNutrientFormula::code,
                                f -> f,
                                (a, b) -> a, // в случае дублей берём первую
                                LinkedHashMap::new
                        ));

        sb.append("\nРазбор по каждому нутриенту:\n");

        for (NutrientRecommendation r : recommendations) {
            sb.append("\n")
                    .append(r.name())
                    .append(" (").append(r.code()).append(")\n");

            ExplainableNutrientFormula f = formulaByCode.get(r.code());
            if (f == null) {
                sb.append("Формула для этого нутриента пока не описана.\n")
                        .append("Итоговое значение: ")
                        .append(formatAmount(r.value()))
                        .append(" ").append(r.unit())
                        .append("\n");
                continue;
            }

            sb.append("Формула: ")
                    .append(f.template())
                    .append("\n");

            Map<String, Object> vars = f.vars(ctx);
            if (vars != null && !vars.isEmpty()) {
                sb.append("Подставляем значения:\n");
                for (Map.Entry<String, Object> e : vars.entrySet()) {
                    sb.append(" • ")
                            .append(e.getKey())
                            .append(" = ")
                            .append(formatVarValue(e.getValue()))
                            .append("\n");
                }
            }

            sb.append("Итог: ")
                    .append(formatAmount(r.value()))
                    .append(" ").append(r.unit())
                    .append("\n");
        }

        return sb.toString();
    }

    private void appendInputs(StringBuilder sb, NutrientContext ctx) {
        sb.append("Какие данные мы учли:\n");

        // Профиль
        boolean hasProfile = false;
        if (ctx.getSex() != null) {
            sb.append(" • Пол: ").append(ctx.getSex()).append("\n");
            hasProfile = true;
        }
        if (ctx.getAgeYears() != null) {
            sb.append(" • Возраст: ").append(ctx.getAgeYears()).append(" лет\n");
            hasProfile = true;
        }
        if (ctx.getWeightKg() != null) {
            sb.append(" • Вес: ").append(formatAmount(ctx.getWeightKg())).append(" кг\n");
            hasProfile = true;
        }
        if (ctx.getHeightCm() != null) {
            sb.append(" • Рост: ").append(ctx.getHeightCm()).append(" см\n");
            hasProfile = true;
        }
        if (ctx.getTrainingLevel() != null) {
            sb.append(" • Уровень подготовки: ").append(ctx.getTrainingLevel()).append("\n");
            hasProfile = true;
        }
        if (!hasProfile) {
            sb.append(" • базовый профиль не заполнен (использованы дефолтные значения)\n");
        }

        // Тренировка
        boolean hasWorkout = false;
        if (ctx.getWorkoutType() != null) {
            sb.append(" • Тип тренировки: ").append(ctx.getWorkoutType()).append("\n");
            hasWorkout = true;
        }
        if (ctx.getDurationMin() != null) {
            sb.append(" • Длительность: ").append(ctx.getDurationMin()).append(" мин\n");
            hasWorkout = true;
        }
        if (ctx.getDistanceKm() != null) {
            sb.append(" • Дистанция: ").append(formatAmount(ctx.getDistanceKm())).append(" км\n");
            hasWorkout = true;
        }
        if (ctx.getCardioRpe() != null) {
            sb.append(" • RPE: ").append(ctx.getCardioRpe()).append("\n");
            hasWorkout = true;
        }
//        if (ctx.getTssTotal() != null) {
//            sb.append(" • TSS (суммарная нагрузка): ").append(formatAmount(ctx.getTssTotal())).append("\n");
//            hasWorkout = true;
//        }
        if (ctx.getTss() != null) {
            sb.append(" • TSS: ").append(formatAmount(ctx.getTss())).append("\n");
            hasWorkout = true;
        }
        if (ctx.getSs() != null) {
            sb.append(" • SS: ").append(formatAmount(ctx.getSs())).append("\n");
            hasWorkout = true;
        }
        if (!hasWorkout) {
            sb.append(" • данные по тренировке не указаны (TSS = 0)\n");
        }

        // Lifestyle
        boolean hasLifestyle = false;
        if (ctx.getSmokePacksPerDay() != null) {
            double p = ctx.getSmokePacksPerDay();
            String label = p == 0.0 ? "не курит"
                    : (p <= 0.25 ? "редко"
                    : (p <= 1.0 ? "до 1 пачки в день"
                    : "больше пачки в день"));
            sb.append(" • Курение: ").append(label)
                    .append(" (").append(formatAmount(p)).append(" пачек/день)\n");
            hasLifestyle = true;
        }
        if (ctx.getVegan() != null && ctx.getVegan()) {
            sb.append(" • Веган/вегетарианец: да\n");
            hasLifestyle = true;
        }
        if (ctx.getPregnant() != null && ctx.getPregnant()) {
            sb.append(" • Беременность: да\n");
            hasLifestyle = true;
        }
        if (ctx.getSunExposureMinutes() != null) {
            sb.append(" • Солнце: ")
                    .append(formatAmount(ctx.getSunExposureMinutes()))
                    .append(" мин/день\n");
            hasLifestyle = true;
        }
        if (!hasLifestyle) {
            sb.append(" • Дополнительные факторы образа жизни: не указаны (использованы дефолты)\n");
        }
    }

    private String formatAmount(double value) {
        if (value < 10) {
            return String.format("%.1f", value);
        }
        return String.format("%.0f", value);
    }

    private String formatVarValue(Object v) {
        if (v instanceof Number n) {
            return formatAmount(n.doubleValue());
        }
        return String.valueOf(v);
    }
}
