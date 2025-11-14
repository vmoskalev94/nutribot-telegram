package com.nutribot.bot.user;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserLifestyleRepository extends CrudRepository<UserLifestyle, Long> {

    Optional<UserLifestyle> findByUserId(Long userId);
}
