package com.nutribot.bot.nutrition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("nutrient_definition")
public class NutrientDefinition {

    @Id
    private Long id;

    @Column("code")
    private String code;

    @Column("name")
    private String name;

    @Column("unit")
    private String unit;
}
