package com.nutribot.bot.profile;

import com.nutribot.bot.user.User;
import lombok.Builder;
import lombok.Value;

/**
 * DTO для удобного отображения профиля пользователю.
 */
@Value
@Builder
public class UserProfileView {

    String name;
    String sexLabel;
    String ageLabel;
    String heightWeightLabel;
    String trainingLevelLabel;
    String cityLabel;
    String phoneLabel;
    String geoLabel;

    public static UserProfileView from(User user) {
        String name = user.getName() != null ? user.getName() : "не указано";

        String sexLabel = switch (user.getSex() != null ? user.getSex() : "") {
            case "M" -> "Мужчина";
            case "F" -> "Женщина";
            default -> "не указано";
        };

        String ageLabel = user.getAge() != null
                ? user.getAge() + " лет"
                : "не указано";

        String heightWeightLabel;
        if (user.getHeightCm() != null && user.getWeightKg() != null) {
            heightWeightLabel = user.getHeightCm() + " см / " + user.getWeightKg() + " кг";
        } else if (user.getHeightCm() != null) {
            heightWeightLabel = user.getHeightCm() + " см / вес не указан";
        } else if (user.getWeightKg() != null) {
            heightWeightLabel = "рост не указан / " + user.getWeightKg() + " кг";
        } else {
            heightWeightLabel = "не указано";
        }

        String trainingLevelLabel;
        if (user.getTrainingLevel() == null) {
            trainingLevelLabel = "не указано";
        } else {
            trainingLevelLabel = switch (user.getTrainingLevel()) {
                case "BEGINNER" -> "Beginner — начинаю / тренируюсь нерегулярно";
                case "AMATEUR" -> "Amateur — стабильно 3–4 раза в неделю";
                case "PRO" -> "Pro — высокий объём/интенсивность";
                default -> "не указано";
            };
        }

        String cityLabel = user.getCity() != null ? user.getCity() : "не указан";
        String phoneLabel = user.getPhone() != null ? user.getPhone() : "не указан";

        String geoLabel;
        if (user.getGeoLat() != null && user.getGeoLon() != null) {
            geoLabel = "есть (lat=" + user.getGeoLat() + ", lon=" + user.getGeoLon() + ")";
        } else {
            geoLabel = "нет";
        }

        return UserProfileView.builder()
                .name(name)
                .sexLabel(sexLabel)
                .ageLabel(ageLabel)
                .heightWeightLabel(heightWeightLabel)
                .trainingLevelLabel(trainingLevelLabel)
                .cityLabel(cityLabel)
                .phoneLabel(phoneLabel)
                .geoLabel(geoLabel)
                .build();
    }

    public String toPrettyString() {
        return """
                🧾 Мой профиль
                
                Имя: %s
                Пол: %s
                Возраст: %s
                Рост / вес: %s
                Уровень подготовки: %s
                Город: %s
                Телефон: %s
                Геоданные: %s
                
                Расширенный профиль (курение, питание, беременность и т.п.) можно будет заполнить в разделе «Нутриенты».
                """.formatted(
                name,
                sexLabel,
                ageLabel,
                heightWeightLabel,
                trainingLevelLabel,
                cityLabel,
                phoneLabel,
                geoLabel
        );
    }
}
