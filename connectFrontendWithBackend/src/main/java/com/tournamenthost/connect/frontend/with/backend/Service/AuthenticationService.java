package com.tournamenthost.connect.frontend.with.backend.Service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tournamenthost.connect.frontend.with.backend.DTO.LoginUserDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.RegisterUserDTO;
import com.tournamenthost.connect.frontend.with.backend.Model.PasswordResetToken;
import com.tournamenthost.connect.frontend.with.backend.Model.User;
import com.tournamenthost.connect.frontend.with.backend.Repository.PasswordResetTokenRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthenticationService {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final EmailService emailService;

    public AuthenticationService(
        UserRepository userRepository,
        AuthenticationManager authenticationManager,
        PasswordEncoder passwordEncoder,
        PasswordResetTokenRepository passwordResetTokenRepository,
        EmailService emailService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
    }

    public User signup(RegisterUserDTO input) {
        User user = new User();
        user.setUsername(input.getUsername());
        user.setName(input.getName());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        // Set any other fields required by your User model here

        return userRepository.save(user);
    }

    public User authenticate(LoginUserDTO input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getUsername(),
                        input.getPassword()
                )
        );

        return userRepository.findByUsername(input.getUsername())
                .orElseThrow();
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        log.info("Password reset requested for email: {}", email);

        // Find user by username (which is email in this system)
        User user = userRepository.findByUsername(email).orElse(null);

        // If user doesn't exist, still return success (security best practice - don't reveal if email exists)
        if (user == null) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return;
        }

        log.info("User found for email: {}, checking rate limiting...", email);

        // Check rate limiting: max 3 requests per hour
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentRequests = passwordResetTokenRepository.countByUserAndCreatedAtAfter(user, oneHourAgo);

        log.debug("Recent password reset requests in last hour: {}", recentRequests);

        if (recentRequests >= 3) {
            // Rate limit exceeded, but don't reveal this to the user
            log.warn("Rate limit exceeded for email: {} ({} requests in last hour)", email, recentRequests);
            return;
        }

        // Generate secure random token
        String token = UUID.randomUUID().toString();
        log.debug("Generated reset token: {}", token);

        // Create password reset token with 1-hour expiration
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(false);

        passwordResetTokenRepository.save(resetToken);
        log.info("Password reset token saved to database for email: {}", email);

        // Send reset email
        try {
            emailService.sendPasswordResetEmail(email, token);
            log.info("Password reset process completed successfully for email: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", email, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        // Find token
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        // Check if token is expired
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        // Check if token has already been used
        if (resetToken.isUsed()) {
            throw new RuntimeException("Reset token has already been used");
        }

        // Update user password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    public boolean validateResetToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token).orElse(null);

        if (resetToken == null) {
            return false;
        }

        // Check if expired or already used
        return !resetToken.getExpiryDate().isBefore(LocalDateTime.now()) && !resetToken.isUsed();
    }
}