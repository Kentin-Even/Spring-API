package fr.ensitech.biblio2.integration;

import fr.ensitech.biblio2.entity.PasswordHistory;
import fr.ensitech.biblio2.entity.User;
import fr.ensitech.biblio2.repository.IPasswordHistoryRepository;
import fr.ensitech.biblio2.repository.IUserRepository;
import fr.ensitech.biblio2.service.PasswordRotationService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PasswordRotationServiceIntegrationTest {

  @Autowired
  private PasswordRotationService passwordRotationService;

  @Autowired
  private IUserRepository userRepository;

  @Autowired
  private IPasswordHistoryRepository passwordHistoryRepository;

  private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  private User testUser;

  @BeforeEach
  void setUp() {
    passwordHistoryRepository.deleteAll();
    userRepository.deleteAll();

    testUser = new User();
    testUser.setFirstName("Jean");
    testUser.setLastName("PAUL");
    testUser.setEmail("jean.paul@example.com");
    testUser.setPassword(passwordEncoder.encode("Password123!"));
    testUser.setRole("U");
    testUser.setActive(true);
    testUser.setPasswordUpdatedAt(new Date());

    testUser = userRepository.save(testUser);
  }

  @Test
  @DisplayName("Vérification qu'un mot de passe récent n'est pas expiré")
  void shouldReturnFalseForRecentPassword() {
    //GIVEN
    testUser.setPasswordUpdatedAt(new Date());
    userRepository.save(testUser);

    //WHEN
    boolean isExpired = passwordRotationService.isPasswordExpired(testUser);

    //THEN
    assertThat(isExpired).isFalse();
  }

  @Test
  @DisplayName("Vérification qu'un mot de passe de plus de 12 semaines est expiré")
  void shouldReturnTrueForExpiredPassword() {
    //GIVEN
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.WEEK_OF_YEAR, -13); // 13 semaines dans le passé
    testUser.setPasswordUpdatedAt(cal.getTime());
    userRepository.save(testUser);

    //WHEN
    boolean isExpired = passwordRotationService.isPasswordExpired(testUser);

    //THEN
    assertThat(isExpired).isTrue();
  }

  @Test
  @DisplayName("Vérification qu'un mot de passe null est considéré comme expiré")
  void shouldReturnTrueForNullPasswordDate() {
    //GIVEN
    testUser.setPasswordUpdatedAt(null);
    userRepository.save(testUser);

    //WHEN
    boolean isExpired = passwordRotationService.isPasswordExpired(testUser);

    //THEN
    assertThat(isExpired).isTrue();
  }

  @Test
  @DisplayName("Vérification qu'un mot de passe exactement à 12 semaines n'est pas expiré")
  void shouldReturnFalseForPasswordAtExactly12Weeks() {
    //GIVEN
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.WEEK_OF_YEAR, -12);
    cal.add(Calendar.HOUR, 1); // Légèrement avant l'expiration
    testUser.setPasswordUpdatedAt(cal.getTime());
    userRepository.save(testUser);

    //WHEN
    boolean isExpired = passwordRotationService.isPasswordExpired(testUser);

    //THEN
    assertThat(isExpired).isFalse();
  }

  @Test
  @DisplayName("Ajout d'un mot de passe à l'historique")
  void shouldAddPasswordToHistory() {
    //GIVEN
    String passwordHash = passwordEncoder.encode("NewPassword123!");

    //WHEN
    passwordRotationService.addPasswordToHistory(testUser, passwordHash);

    //THEN
    List<PasswordHistory> history = passwordHistoryRepository.findTop5ByUserIdOrderByCreatedAtDesc(testUser.getId());
    assertThat(history).hasSize(1);
    assertThat(history.get(0).getPasswordHash()).isEqualTo(passwordHash);
    assertThat(history.get(0).getUser().getId()).isEqualTo(testUser.getId());
    assertThat(history.get(0).getCreatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Vérification qu'un mot de passe n'est pas dans l'historique")
  void shouldReturnFalseWhenPasswordNotInHistory() {
    //GIVEN
    String oldPasswordHash = passwordEncoder.encode("OldPassword123!");
    passwordRotationService.addPasswordToHistory(testUser, oldPasswordHash);

    //WHEN
    boolean isInHistory = passwordRotationService.isPasswordInHistory(testUser, "NewPassword456!");

    //THEN
    assertThat(isInHistory).isFalse();
  }

  @Test
  @DisplayName("Vérification qu'un mot de passe est dans l'historique")
  void shouldReturnTrueWhenPasswordInHistory() {
    //GIVEN
    String plainPassword = "OldPassword123!";
    String passwordHash = passwordEncoder.encode(plainPassword);
    passwordRotationService.addPasswordToHistory(testUser, passwordHash);

    //WHEN
    boolean isInHistory = passwordRotationService.isPasswordInHistory(testUser, plainPassword);

    //THEN
    assertThat(isInHistory).isTrue();
  }

  @Test
  @DisplayName("Vérification de plusieurs mots de passe dans l'historique")
  void shouldCheckMultiplePasswordsInHistory() {
    //GIVEN
    String password1 = "Password1!";
    String password2 = "Password2!";
    String password3 = "Password3!";

    passwordRotationService.addPasswordToHistory(testUser, passwordEncoder.encode(password1));
    passwordRotationService.addPasswordToHistory(testUser, passwordEncoder.encode(password2));
    passwordRotationService.addPasswordToHistory(testUser, passwordEncoder.encode(password3));

    //WHEN & THEN
    assertThat(passwordRotationService.isPasswordInHistory(testUser, password1)).isTrue();
    assertThat(passwordRotationService.isPasswordInHistory(testUser, password2)).isTrue();
    assertThat(passwordRotationService.isPasswordInHistory(testUser, password3)).isTrue();
    assertThat(passwordRotationService.isPasswordInHistory(testUser, "Password4!")).isFalse();
  }

  @Test
  @DisplayName("Nettoyage automatique de l'historique au-delà de 5 mots de passe")
  void shouldCleanupOldPasswordsWhenExceeding5() {
    //GIVEN
    for (int i = 1; i <= 7; i++) {
      String passwordHash = passwordEncoder.encode("Password" + i + "!");
      passwordRotationService.addPasswordToHistory(testUser, passwordHash);
    }

    //WHEN
    List<PasswordHistory> history = passwordHistoryRepository.findByUserOrderByCreatedAtDesc(testUser);

    //THEN
    assertThat(history).hasSize(5);
  }

  @Test
  @DisplayName("Vérification que les 5 mots de passe les plus récents sont conservés")
  void shouldKeepMostRecent5Passwords() {
    //GIVEN
    for (int i = 1; i <= 7; i++) {
      String passwordHash = passwordEncoder.encode("Password" + i + "!");
      passwordRotationService.addPasswordToHistory(testUser, passwordHash);
      
      // Petit délai pour garantir l'ordre chronologique
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    //WHEN
    List<PasswordHistory> history = passwordHistoryRepository.findTop5ByUserIdOrderByCreatedAtDesc(testUser.getId());

    //THEN
    assertThat(history).hasSize(5);
    // Les mots de passe 3, 4, 5, 6, 7 doivent être dans l'historique
    assertThat(passwordRotationService.isPasswordInHistory(testUser, "Password7!")).isTrue();
    assertThat(passwordRotationService.isPasswordInHistory(testUser, "Password6!")).isTrue();
    assertThat(passwordRotationService.isPasswordInHistory(testUser, "Password5!")).isTrue();
    assertThat(passwordRotationService.isPasswordInHistory(testUser, "Password4!")).isTrue();
    assertThat(passwordRotationService.isPasswordInHistory(testUser, "Password3!")).isTrue();
    // Les mots de passe 1 et 2 doivent avoir été supprimés
    assertThat(passwordRotationService.isPasswordInHistory(testUser, "Password1!")).isFalse();
    assertThat(passwordRotationService.isPasswordInHistory(testUser, "Password2!")).isFalse();
  }

  @Test
  @DisplayName("Nettoyage manuel de l'historique")
  void shouldManuallyCleanupOldPasswords() {
    //GIVEN
    for (int i = 1; i <= 7; i++) {
      PasswordHistory ph = new PasswordHistory();
      ph.setUser(testUser);
      ph.setPasswordHash(passwordEncoder.encode("Password" + i + "!"));
      ph.setCreatedAt(new Date());
      passwordHistoryRepository.save(ph);
    }

    //WHEN
    passwordRotationService.cleanupOldPasswords(testUser);

    //THEN
    List<PasswordHistory> history = passwordHistoryRepository.findByUserOrderByCreatedAtDesc(testUser);
    assertThat(history).hasSize(5);
  }

  @Test
  @DisplayName("Calcul des jours avant expiration pour un mot de passe récent")
  void shouldCalculateDaysUntilExpirationForRecentPassword() {
    //GIVEN
    testUser.setPasswordUpdatedAt(new Date());
    userRepository.save(testUser);

    //WHEN
    long daysRemaining = passwordRotationService.getDaysUntilExpiration(testUser);

    //THEN
    assertThat(daysRemaining).isGreaterThan(80); // ~84 jours (12 semaines)
    assertThat(daysRemaining).isLessThanOrEqualTo(84);
  }

  @Test
  @DisplayName("Calcul des jours avant expiration pour un mot de passe à mi-parcours")
  void shouldCalculateDaysUntilExpirationForMidwayPassword() {
    //GIVEN
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.WEEK_OF_YEAR, -6); // 6 semaines dans le passé
    testUser.setPasswordUpdatedAt(cal.getTime());
    userRepository.save(testUser);

    //WHEN
    long daysRemaining = passwordRotationService.getDaysUntilExpiration(testUser);

    //THEN
    assertThat(daysRemaining).isGreaterThan(38); // ~42 jours (6 semaines restantes)
    assertThat(daysRemaining).isLessThanOrEqualTo(42);
  }

  @Test
  @DisplayName("Calcul des jours avant expiration pour un mot de passe expiré")
  void shouldReturnZeroForExpiredPassword() {
    //GIVEN
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.WEEK_OF_YEAR, -15); // 15 semaines dans le passé
    testUser.setPasswordUpdatedAt(cal.getTime());
    userRepository.save(testUser);

    //WHEN
    long daysRemaining = passwordRotationService.getDaysUntilExpiration(testUser);

    //THEN
    assertThat(daysRemaining).isEqualTo(0);
  }

  @Test
  @DisplayName("Calcul des jours avant expiration pour un mot de passe null")
  void shouldReturnZeroForNullPasswordDate() {
    //GIVEN
    testUser.setPasswordUpdatedAt(null);
    userRepository.save(testUser);

    //WHEN
    long daysRemaining = passwordRotationService.getDaysUntilExpiration(testUser);

    //THEN
    assertThat(daysRemaining).isEqualTo(0);
  }

  @Test
  @DisplayName("Vérification que l'historique est bien lié à l'utilisateur")
  void shouldLinkPasswordHistoryToUser() {
    //GIVEN
    String passwordHash = passwordEncoder.encode("TestPassword123!");
    passwordRotationService.addPasswordToHistory(testUser, passwordHash);

    //WHEN
    List<PasswordHistory> history = passwordHistoryRepository.findByUserOrderByCreatedAtDesc(testUser);

    //THEN
    assertThat(history).hasSize(1);
    assertThat(history.get(0).getUser().getId()).isEqualTo(testUser.getId());
    assertThat(history.get(0).getUser().getEmail()).isEqualTo(testUser.getEmail());
  }

  @Test
  @DisplayName("Vérification que plusieurs utilisateurs ont des historiques séparés")
  void shouldSeparatePasswordHistoriesForDifferentUsers() {
    //GIVEN
    User user2 = new User();
    user2.setFirstName("Marie");
    user2.setLastName("MARTIN");
    user2.setEmail("marie.martin@example.com");
    user2.setPassword(passwordEncoder.encode("Password123!"));
    user2.setRole("U");
    user2.setActive(true);
    user2.setPasswordUpdatedAt(new Date());
    user2 = userRepository.save(user2);

    String password1 = "User1Password!";
    String password2 = "User2Password!";

    passwordRotationService.addPasswordToHistory(testUser, passwordEncoder.encode(password1));
    passwordRotationService.addPasswordToHistory(user2, passwordEncoder.encode(password2));

    //WHEN
    boolean user1HasPassword1 = passwordRotationService.isPasswordInHistory(testUser, password1);
    boolean user1HasPassword2 = passwordRotationService.isPasswordInHistory(testUser, password2);
    boolean user2HasPassword1 = passwordRotationService.isPasswordInHistory(user2, password1);
    boolean user2HasPassword2 = passwordRotationService.isPasswordInHistory(user2, password2);

    //THEN
    assertThat(user1HasPassword1).isTrue();
    assertThat(user1HasPassword2).isFalse();
    assertThat(user2HasPassword1).isFalse();
    assertThat(user2HasPassword2).isTrue();
  }

  @Test
  @DisplayName("Vérification du comptage des entrées d'historique")
  void shouldCountPasswordHistoryEntries() {
    //GIVEN
    for (int i = 1; i <= 3; i++) {
      String passwordHash = passwordEncoder.encode("Password" + i + "!");
      passwordRotationService.addPasswordToHistory(testUser, passwordHash);
    }

    //WHEN
    long count = passwordHistoryRepository.countByUser(testUser);

    //THEN
    assertThat(count).isEqualTo(3);
  }

  @Test
  @DisplayName("Vérification que l'historique vide retourne false pour tout mot de passe")
  void shouldReturnFalseForEmptyHistory() {
    //WHEN
    boolean isInHistory = passwordRotationService.isPasswordInHistory(testUser, "AnyPassword123!");

    //THEN
    assertThat(isInHistory).isFalse();
  }

  @Test
  @DisplayName("Vérification de la date de création dans l'historique")
  void shouldSetCreatedAtDateWhenAddingToHistory() {
    //GIVEN
    String passwordHash = passwordEncoder.encode("TestPassword123!");
    Date beforeAdd = new Date();

    //WHEN
    passwordRotationService.addPasswordToHistory(testUser, passwordHash);

    //THEN
    List<PasswordHistory> history = passwordHistoryRepository.findTop5ByUserIdOrderByCreatedAtDesc(testUser.getId());
    assertThat(history).hasSize(1);
    assertThat(history.get(0).getCreatedAt()).isNotNull();
    assertThat(history.get(0).getCreatedAt()).isAfterOrEqualTo(beforeAdd);
    assertThat(history.get(0).getCreatedAt()).isBeforeOrEqualTo(new Date());
  }

  @Test
  @DisplayName("Vérification que le BCrypt fonctionne correctement pour la comparaison")
  void shouldCorrectlyCompareBCryptPasswords() {
    //GIVEN
    String plainPassword = "MySecurePassword123!";
    String hashedPassword = passwordEncoder.encode(plainPassword);
    passwordRotationService.addPasswordToHistory(testUser, hashedPassword);

    //WHEN
    boolean matchesCorrect = passwordRotationService.isPasswordInHistory(testUser, plainPassword);
    boolean matchesIncorrect = passwordRotationService.isPasswordInHistory(testUser, "WrongPassword123!");

    //THEN
    assertThat(matchesCorrect).isTrue();
    assertThat(matchesIncorrect).isFalse();
  }

  @Test
  @DisplayName("Vérification du tri chronologique de l'historique")
  void shouldOrderHistoryByCreatedAtDesc() {
    //GIVEN
    for (int i = 1; i <= 3; i++) {
      String passwordHash = passwordEncoder.encode("Password" + i + "!");
      passwordRotationService.addPasswordToHistory(testUser, passwordHash);
      
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    //WHEN
    List<PasswordHistory> history = passwordHistoryRepository.findTop5ByUserIdOrderByCreatedAtDesc(testUser.getId());

    //THEN
    assertThat(history).hasSize(3);
    // Vérifier que les dates sont dans l'ordre décroissant
    for (int i = 0; i < history.size() - 1; i++) {
      assertThat(history.get(i).getCreatedAt())
              .isAfterOrEqualTo(history.get(i + 1).getCreatedAt());
    }
  }
}
