package com.nutribot.bot.telegram.menu;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NutrientsMenuButtons {

    public static final String FILL_PROFILE = "Заполнить расширенный профиль";
    public static final String LAST_WORKOUT = "Расчёт нутриентов последней тренировки";
    public static final String VERBOSE_MODE  = "Подробный режим расчёта";
    public static final String STATS = "Статистика по нутриентам (скоро)";
    public static final String BACK_TO_MAIN = "⬅️Главное меню";

    // Расширенный профиль
    public static final String SMOKING = "Курение";
    public static final String VEGAN = "Веганство";
    public static final String PREGNANCY = "Беременность";
    public static final String GEO = "Геолокация";
    public static final String BACK_TO_NUTRIENTS = "⬅️ Назад в «Нутриенты»";
}

