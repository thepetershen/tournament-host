package com.tournamenthost.connect.frontend.with.backend.Service;

import com.tournamenthost.connect.frontend.with.backend.Repository.PasswordResetTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PasswordResetTokenCleanupService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetTokenCleanupService.class);
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public PasswordResetTokenCleanupService(PasswordResetTokenRepository passwordResetTokenRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    // Run daily at 2 AM to clean up expired tokens
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        passwordResetTokenRepository.deleteByExpiryDateBefore(now);
        log.info("Cleaned up expired password reset tokens at: {}", now);
    }
}
