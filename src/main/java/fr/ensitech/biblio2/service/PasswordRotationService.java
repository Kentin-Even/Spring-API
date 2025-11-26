package fr.ensitech.biblio2.service;

import fr.ensitech.biblio2.entity.PasswordHistory;
import fr.ensitech.biblio2.entity.User;
import fr.ensitech.biblio2.repository.IPasswordHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class PasswordRotationService {

  @Autowired
  private IPasswordHistoryRepository passwordHistoryRepository;

  private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  private static final int PASSWORD_EXPIRATION_WEEKS = 12;
  private static final int PASSWORD_HISTORY_LIMIT = 5;

  public boolean isPasswordExpired(User user) {
    if (user.getPasswordUpdatedAt() == null) {

      return true;
    }

    Calendar expirationDate = Calendar.getInstance();
    expirationDate.setTime(user.getPasswordUpdatedAt());
    expirationDate.add(Calendar.WEEK_OF_YEAR, PASSWORD_EXPIRATION_WEEKS);

    return new Date().after(expirationDate.getTime());
  }

  public boolean isPasswordInHistory(User user, String plainPassword) {
    List<PasswordHistory> history = passwordHistoryRepository.findTop5ByUserIdOrderByCreatedAtDesc(user.getId());

    for (PasswordHistory ph : history) {
      if (passwordEncoder.matches(plainPassword, ph.getPasswordHash())) {
        return true;
      }
    }

    return false;
  }

  @Transactional
  public void addPasswordToHistory(User user, String passwordHash) {
    PasswordHistory history = new PasswordHistory();
    history.setUser(user);
    history.setPasswordHash(passwordHash);
    history.setCreatedAt(new Date());

    passwordHistoryRepository.save(history);

    cleanupOldPasswords(user);
  }

  @Transactional
  public void cleanupOldPasswords(User user) {
    List<PasswordHistory> allHistory = passwordHistoryRepository.findByUserOrderByCreatedAtDesc(user);

    if (allHistory.size() > PASSWORD_HISTORY_LIMIT) {
      List<PasswordHistory> toDelete = allHistory.subList(PASSWORD_HISTORY_LIMIT, allHistory.size());
      passwordHistoryRepository.deleteAll(toDelete);
    }
  }

  public long getDaysUntilExpiration(User user) {
    if (user.getPasswordUpdatedAt() == null) {
      return 0;
    }

    Calendar expirationDate = Calendar.getInstance();
    expirationDate.setTime(user.getPasswordUpdatedAt());
    expirationDate.add(Calendar.WEEK_OF_YEAR, PASSWORD_EXPIRATION_WEEKS);

    long diffInMillis = expirationDate.getTimeInMillis() - new Date().getTime();
    long daysRemaining = diffInMillis / (1000 * 60 * 60 * 24);

    return Math.max(0, daysRemaining);
  }
}