package com.nutribot.bot.telegram.menu;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Текст кнопок внутри раздела «Профиль».
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProfileMenuButtons {

    public static final String VIEW = "Просмотр";
    public static final String EDIT = "Редактировать";
    public static final String BACK_TO_MAIN = "⬅️Главное меню";

    // кнопки меню "Профиль → Редактировать"
    public static final String EDIT_NAME = "Имя";
    public static final String EDIT_AGE = "Возраст";
    public static final String EDIT_HEIGHT = "Рост";
    public static final String EDIT_WEIGHT = "Вес";
    public static final String EDIT_CITY = "Город";
    public static final String EDIT_PHONE = "Телефон";
    public static final String EDIT_TRAINING_LEVEL = "Уровень подготовки";
    public static final String EDIT_BACK = "Назад";
}
