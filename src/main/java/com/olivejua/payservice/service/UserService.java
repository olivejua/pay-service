package com.olivejua.payservice.service;

import com.olivejua.payservice.database.entity.UserEntity;
import com.olivejua.payservice.database.repository.UserJpaRepository;
import com.olivejua.payservice.domain.User;
import com.olivejua.payservice.error.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 유저가 존재하지 않거나 탈퇴한 유저라면 예외를 던진다.
 */

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserJpaRepository userRepository;

    public User getActiveUser(Long userId) {
        return userRepository.findById(userId)
                .map(UserEntity::toModel)
                .filter(User::hasActiveStatus)
                .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND_OR_WITHDRAWN", "User does not exist or is in a withdrawn state."));
    }

    public void validateIfUserIsActive(Long userId) {
        boolean existsActiveUser = userRepository.findById(userId).stream()
                .map(UserEntity::toModel)
                .anyMatch(User::hasActiveStatus);

        if (!existsActiveUser) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND_OR_WITHDRAWN", "User does not exist or is in a withdrawn state.");
        }
    }

    @Transactional
    public void addCurrentBalance(Long userId, long amount) {
        User user = getActiveUser(userId);

        if (amount > 0) {
            UserEntity entity = UserEntity.from(user.addCurrentBalance(amount));
            userRepository.save(entity);
        }
    }

    @Transactional
    public void subtractCurrentBalance(Long userId, long amount) {
        User user = getActiveUser(userId);

        if (amount > 0) {
            UserEntity entity = UserEntity.from(user.subtractCurrentBalance(amount));
            userRepository.save(entity);
        }
    }
}
