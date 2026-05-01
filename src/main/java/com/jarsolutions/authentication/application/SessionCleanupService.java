package com.jarsolutions.authentication.application;

import com.jarsolutions.authentication.domain.repository.UserSessionRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class SessionCleanupService {
  private static final Logger log = LoggerFactory.getLogger(SessionCleanupService.class);
  private final UserSessionRepository userSessionRepository;

  public SessionCleanupService(UserSessionRepository userSessionRepository) {
    this.userSessionRepository = userSessionRepository;
  }

  @Scheduled(cron = "0 0 3 * * *")
  public void cleanupExpiredSession() {
    log.info("Starting background cleanup job for expired tokens...");

    Instant now = Instant.now();
    userSessionRepository.deleteAllByExpiryDateBefore(now);

    log.info("Finished background cleanup job.");
  }
}
