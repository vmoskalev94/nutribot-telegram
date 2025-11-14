package com.nutribot.bot.user;

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
@Table("user_lifestyle")
public class UserLifestyle {

    @Id
    private Long id;   // surrogate PK

    @Column("user_id")
    private Long userId;

    @Column("smoke_packs_per_day")
    private Double smokePacksPerDay;

    @Column("vegan")
    private Boolean vegan;

    @Column("pregnant")
    private Boolean pregnant;
}
