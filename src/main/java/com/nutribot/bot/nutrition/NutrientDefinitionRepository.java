package com.nutribot.bot.nutrition;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface NutrientDefinitionRepository extends CrudRepository<NutrientDefinition, Long> {

    Optional<NutrientDefinition> findByCode(String code);
}
