package com.pcms.userservice.scheduler;

import com.pcms.userservice.entity.User;
import com.pcms.userservice.enums.UserStatus;
import com.pcms.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * NSF-10: Account auto-unlock every 5 minutes
 * Unlocks accounts whose 30-min lockout has expired
 */
@Component
public class AccountUnlockScheduler {

    private static final Logger log = LoggerFactory.getLogger(AccountUnlockScheduler.class);

    private final UserRepository userRepository;

    public AccountUnlockScheduler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(fixedRate = 300_000) // 5 min = 300_000 ms
    @Transactional
    public void unlockExpiredAccounts() {
        LocalDateTime now = LocalDateTime.now();
        List<User> locked = userRepository.findByStatus(UserStatus.LOCKED);
        int unlocked = 0;
        for (User user : locked) {
            if (user.getLockedUntil() != null && user.getLockedUntil().isBefore(now)) {
                user.setStatus(UserStatus.ACTIVE);
                user.setLockedUntil(null);
                user.setFailedLoginCount(0);
                userRepository.save(user);
                unlocked++;
            }
        }
        if (unlocked > 0) {
            log.info("NSF-10: Unlocked {} expired account lockouts", unlocked);
        }
    }
}
