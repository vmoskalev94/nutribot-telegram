package com.nutribot.bot.nutrition;

import com.nutribot.bot.user.User;
import com.nutribot.bot.user.UserLifestyle;
import com.nutribot.bot.user.UserLifestyleService;
import com.nutribot.bot.user.UserService;
import com.nutribot.bot.workout.CardioWorkoutDetails;
import com.nutribot.bot.workout.WorkoutDetails;
import com.nutribot.bot.workout.WorkoutService;
import com.nutribot.bot.workout.WorkoutType;
import com.nutribot.bot.workout.tss.TssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Базовый калькулятор нутриентов.
 * <p>
 * Теперь умеет:
 * - собирать NutrientContext по userId + workoutId;
 * - считать рекомендации;
 * - возвращать как только список, так и "контекст + список".
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NutrientCalculatorService {

    private final UserService userService;
    private final WorkoutService workoutService;
    private final NutrientDefinitionRepository definitionRepository;
    private final UserLifestyleService userLifestyleService;
    private final List<ExplainableNutrientFormula> formulas;
    private final NutrientExplanationService explanationService;
    private final TssService tssService;

    /**
     * Старый метод — оставляем для существующего кода.
     */
    @Transactional(readOnly = true)
    public List<NutrientRecommendation> calculateForWorkout(Long userId, Long workoutId) {
        return calculateWithContext(userId, workoutId).recommendations();
    }

    /**
     * Новый метод: возвращает и контекст, и рекомендации.
     * Удобен для explainable-логов.
     */
    @Transactional(readOnly = true)
    public NutrientCalculationResult calculateWithContext(Long userId, Long workoutId) {
        User user = userService.getUserOrThrow(userId);
        WorkoutDetails workoutDetails = workoutService.getWorkoutDetails(userId, workoutId);

        NutrientContext ctx = buildContext(user, workoutDetails);
        List<NutrientRecommendation> recs = calculateForContext(ctx);

        NutrientCalculationResult result = NutrientCalculationResult.builder()
                .context(ctx)
                .recommendations(recs)
                .build();

        if (log.isDebugEnabled()) {
            String explain = explanationService.buildExplanation(result);
            log.debug("Nutrient calculation debug for user={} workout={}:\n{}",
                    userId, workoutId, explain);
        }

        return result;
    }

    /**
     * Внутренний метод расчёта по уже готовому контексту.
     * Используется и в обычном расчёте, и в explainable-логах.
     */
    private List<NutrientRecommendation> calculateForContext(NutrientContext ctx) {
        List<NutrientRecommendation> result = new ArrayList<>();

        // Простая защита: если формул нет, возвращаем пустой список.
        if (formulas == null || formulas.isEmpty()) {
            return result;
        }

        // Кэшим определения по коду, чтобы меньше ходить в БД
        Map<String, NutrientDefinition> defsCache = new HashMap<>();

        for (ExplainableNutrientFormula formula : formulas) {
            double value = formula.calculate(ctx);
            if (Double.isNaN(value) || value <= 0) {
                continue;
            }

            String code = formula.code();
            NutrientDefinition def = defsCache.computeIfAbsent(code, c ->
                    definitionRepository.findByCode(c)
                            .orElseGet(() -> NutrientDefinition.builder()
                                    .code(c)
                                    .name(c)   // fallback: имя = код
                                    .unit("mg") // todo mg?
                                    .build())
            );

            result.add(NutrientRecommendation.builder()
                    .code(def.getCode())
                    .name(def.getName())
                    .unit(def.getUnit())
                    .value(value)
                    .build());
        }

        return result;
    }

    private NutrientContext buildContext(User user, WorkoutDetails details) {
        var workout = details.getWorkout();
        WorkoutType type = workout.getType();

        Integer durationMin = null;
        Double distanceKm = null;
        Integer cardioRpe = null;

        if (type == WorkoutType.CARDIO) {
            CardioWorkoutDetails c = details.getCardioDetails();
            if (c != null) {
                durationMin = c.getDurationMin();
                distanceKm = c.getDistanceKm();
                cardioRpe = c.getRpe();
            }
        }

        // Определяем пол и уровень подготовки
        boolean isMale = BodyCompositionCalculator.isMale(user.getSex());
        boolean isAthlete = BodyCompositionCalculator.isAthlete(user.getTrainingLevel());

        // SS и TSS — новые метрики нагрузки
        double ss = tssService.calculateSs(details);
        double tss = tssService.calculateTss(details, user.getAge());

        // Производные показатели тела
        double ffm = BodyCompositionCalculator.calculateFfm(
                user.getWeightKg(), user.getHeightCm(), isMale, isAthlete);
        double boneMass = BodyCompositionCalculator.calculateBoneMass(
                user.getWeightKg(), user.getHeightCm(), isMale);
        double sweatRate = BodyCompositionCalculator.calculateSweatRate(isMale);

        // Lifestyle
        UserLifestyle lifestyle = userLifestyleService.findByUserId(user.getId()).orElse(null);
        double smokePacksPerDay = lifestyle != null && lifestyle.getSmokePacksPerDay() != null
                ? lifestyle.getSmokePacksPerDay()
                : 0.0;
        boolean vegan = lifestyle != null && Boolean.TRUE.equals(lifestyle.getVegan());
        boolean pregnant = lifestyle != null && Boolean.TRUE.equals(lifestyle.getPregnant());

        return NutrientContext.builder()
                .userId(user.getId())
                .workoutId(workout.getId())
                // Профиль
                .sex(user.getSex())
                .ageYears(user.getAge())
                .weightKg(user.getWeightKg())
                .heightCm(user.getHeightCm())
                .trainingLevel(user.getTrainingLevel())
                // Вспомогательные флаги
                .male(isMale)
                .athlete(isAthlete)
                // Тренировка
                .workoutType(type)
                .durationMin(durationMin)
                .distanceKm(distanceKm)
                .cardioRpe(cardioRpe)
                // Метрики нагрузки
                .ss(ss)
                .tss(tss)
                .tssTotal(ss + tss) // deprecated, для обратной совместимости
                // Показатели тела
                .ffm(ffm)
                .boneMass(boneMass)
                .sweatRate(sweatRate)
                // Lifestyle
                .smokePacksPerDay(smokePacksPerDay)
                .vegan(vegan)
                .pregnant(pregnant)
                .build();
    }
}
