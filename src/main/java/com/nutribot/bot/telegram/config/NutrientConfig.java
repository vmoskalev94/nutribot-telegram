package com.nutribot.bot.telegram.config;

import com.nutribot.bot.nutrition.NutrientExplainProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NutrientExplainProperties.class)
public class NutrientConfig {

}
