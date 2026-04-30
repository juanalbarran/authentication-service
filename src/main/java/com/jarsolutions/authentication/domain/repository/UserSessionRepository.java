package com.jarsolutions.authentication.domain.repository;

import com.jarsolutions.authentication.domain.entity.UserSession;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
  Optional<UserSession> findByToken(String refreshToken);
}
