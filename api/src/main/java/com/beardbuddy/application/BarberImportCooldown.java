package com.beardbuddy.application;

import com.beardbuddy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * One barber import per user per cooldown window. The timestamp lives in the database (not in
 * memory), so it survives restarts and holds across several API replicas.
 */
@Component
public class BarberImportCooldown {

    private final UserRepository userRepository;
    private final Duration cooldown;

    public BarberImportCooldown(
            UserRepository userRepository,
            @Value("${beardbuddy.barber-import.cooldown-seconds}") long cooldownSeconds
    ) {
        this.userRepository = userRepository;
        this.cooldown = Duration.ofSeconds(Math.max(0, cooldownSeconds));
    }

    /**
     * Reserves an import for this user, or throws if the previous one is too recent.
     *
     * @return when this user may import again
     */
    @Transactional
    public Instant claim(String userId) {
        Instant now = Instant.now();
        int claimed = userRepository.claimBarberImport(userId, now, now.minus(cooldown));
        if (claimed == 1) {
            return now.plus(cooldown);
        }

        Instant nextAllowed = userRepository.findLastBarberImportAt(userId)
                .map(last -> last.plus(cooldown))
                .orElse(now.plus(cooldown));
        long waitSeconds = Duration.between(now, nextAllowed).toSeconds() + 1;
        throw new TooManyRequestsException(
                "You can import barbers again in " + formatWait(waitSeconds), waitSeconds);
    }

    private static String formatWait(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }
        long minutes = seconds / 60;
        long rest = seconds % 60;
        return rest == 0 ? minutes + "m" : minutes + "m " + rest + "s";
    }
}
