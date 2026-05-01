package com.jarsolutions.authentication.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.jarsolutions.authentication.domain.repository.UserSessionRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SessionCleanupServiceTest {

  @Mock private UserSessionRepository userSessionRepository;
  @InjectMocks private SessionCleanupService sessionCleanupService;

  @Test
  void cleanupExpiredSession_ShouldCallRepositoryToDeleteOldTokens() {
    sessionCleanupService.cleanupExpiredSession();
    verify(userSessionRepository, times(1)).deleteAllByExpiryDateBefore(any(Instant.class));
  }
}
