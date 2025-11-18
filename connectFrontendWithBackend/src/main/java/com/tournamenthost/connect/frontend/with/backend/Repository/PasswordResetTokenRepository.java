package com.tournamenthost.connect.frontend.with.backend.Repository;

import com.tournamenthost.connect.frontend.with.backend.Model.PasswordResetToken;
import com.tournamenthost.connect.frontend.with.backend.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    void deleteByExpiryDateBefore(LocalDateTime now);

    long countByUserAndCreatedAtAfter(User user, LocalDateTime since);
}
