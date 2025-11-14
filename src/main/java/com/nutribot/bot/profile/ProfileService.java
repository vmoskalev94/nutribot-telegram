package com.nutribot.bot.profile;

import com.nutribot.bot.user.User;
import com.nutribot.bot.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис работы с профилем (пока только чтение).
 * Дальше добавим сюда методы редактирования.
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileView getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

        return UserProfileView.from(user);
    }
}
