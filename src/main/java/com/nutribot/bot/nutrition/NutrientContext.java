package com.nutribot.bot.nutrition;

import com.nutribot.bot.workout.WorkoutType;
import lombok.Builder;
import lombok.Value;

/**
 * Контекст для расчёта нутриентов:
 * - данные пользователя,
 * - параметры конкретной тренировки,
 * - lifestyle-факторы.
 * <p>
 * sex / trainingLevel здесь — строки, чтобы соответствовать текущей модели User.
 */
@Value
@Builder(toBuilder = true)
public class NutrientContext {

    Long userId;
    Long workoutId;

    // Профиль пользователя
    String sex;              // todo "M"/"F" или твои коды (вывести в enum позже)
    Integer ageYears;
    Double weightKg;
    Integer heightCm;
    String trainingLevel;    //todo "BEGINNER" / "AMATEUR" / "PRO" и т.п. (вывести в enum позже)

    // Параметры тренировки
    WorkoutType workoutType;
    Integer durationMin;   // для кардио
    Double distanceKm;     // для кардио
    Integer cardioRpe;     // RPE кардио (1–10)
    Double tssTotal;       // общая трен. нагрузка (пока 0, позже посчитаем честно)

    // Lifestyle (MVP: всё по умолчанию, шаг 4.3 будет это заполнять)
    Double smokePacksPerDay;
    Boolean vegan;
    Boolean pregnant;

    // Доп. контекст
    Double sunExposureMinutes; // мин/день, на MVP: считаем 0 (мало солнца)
}
