package com.nutribot.bot.nutrition;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "nutribot.features.nutrient-explain")
public class NutrientExplainProperties {

    /**
     * Разрешено ли показывать подробные логи расчёта пользователю.
     * Если false — кнопка "Пояснить расчёт" и "подробный режим" исчезают,
     * но explainable-домен и логи остаются.
     */
    private boolean userVisible = false;
}
