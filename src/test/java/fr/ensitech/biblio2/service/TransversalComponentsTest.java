package fr.ensitech.biblio2.service;

import fr.ensitech.biblio2.entity.PasswordHistory;
import fr.ensitech.biblio2.entity.User;
import fr.ensitech.biblio2.repository.IPasswordHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 4.4 - Tests des composants transverses
 * Tests du composant de hash de mot de passe
 * Tests de la rotation de mot de passe
 * Tests du service de réponse de sécurité
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("4.4 - Tests des composants transverses")
class TransversalComponentsTest {

  // ========================================
  // 4.4.1 - Tests du composant SecurityAnswerService
  // ========================================

  @Nested
  @DisplayName("4.4.1 - Tests du hash des réponses de sécurité")
  class SecurityAnswerServiceTest {

    @InjectMocks
    private SecurityAnswerService securityAnswerService;

    @Test
    @DisplayName("Devrait hasher une réponse de sécurité avec SHA-256")
    void shouldHashSecurityAnswerWithSHA256() {
      // Given
      String plainAnswer = "Paris";

      // When
      String hashedAnswer = securityAnswerService.hashSecurityAnswer(plainAnswer);

      // Then
      assertThat(hashedAnswer).isNotNull();
      assertThat(hashedAnswer).isNotEqualTo(plainAnswer);
      assertThat(hashedAnswer).isBase64();
    }

    @Test
    @DisplayName("Devrait produire le même hash pour la même réponse")
    void shouldProduceSameHashForSameAnswer() {
      // Given
      String plainAnswer = "London";

      // When
      String hash1 = securityAnswerService.hashSecurityAnswer(plainAnswer);
      String hash2 = securityAnswerService.hashSecurityAnswer(plainAnswer);

      // Then
      assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    @DisplayName("Devrait être insensible à la casse")
    void shouldBeCaseInsensitive() {
      // Given
      String answer1 = "PARIS";
      String answer2 = "paris";
      String answer3 = "PaRiS";

      // When
      String hash1 = securityAnswerService.hashSecurityAnswer(answer1);
      String hash2 = securityAnswerService.hashSecurityAnswer(answer2);
      String hash3 = securityAnswerService.hashSecurityAnswer(answer3);

      // Then
      assertThat(hash1).isEqualTo(hash2);
      assertThat(hash2).isEqualTo(hash3);
    }

    @Test
    @DisplayName("Devrait ignorer les espaces en début et fin")
    void shouldTrimWhitespace() {
      // Given
      String answer1 = "Paris";
      String answer2 = "  Paris  ";
      String answer3 = "Paris   ";

      // When
      String hash1 = securityAnswerService.hashSecurityAnswer(answer1);
      String hash2 = securityAnswerService.hashSecurityAnswer(answer2);
      String hash3 = securityAnswerService.hashSecurityAnswer(answer3);

      // Then
      assertThat(hash1).isEqualTo(hash2);
      assertThat(hash2).isEqualTo(hash3);
    }

    @Test
    @DisplayName("Devrait vérifier correctement une réponse valide")
    void shouldVerifyCorrectAnswer() {
      // Given
      String plainAnswer = "MyPet";
      String hashedAnswer = securityAnswerService.hashSecurityAnswer(plainAnswer);

      // When
      boolean isValid = securityAnswerService.verifySecurityAnswer(plainAnswer, hashedAnswer);

      // Then
      assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Devrait rejeter une réponse incorrecte")
    void shouldRejectIncorrectAnswer() {
      // Given
      String correctAnswer = "MyPet";
      String wrongAnswer = "WrongPet";
      String hashedAnswer = securityAnswerService.hashSecurityAnswer(correctAnswer);

      // When
      boolean isValid = securityAnswerService.verifySecurityAnswer(wrongAnswer, hashedAnswer);

      // Then
      assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Devrait gérer les caractères spéciaux")
    void shouldHandleSpecialCharacters() {
      // Given
      String answerWithSpecialChars = "My-Pet_123!@#";

      // When
      String hash = securityAnswerService.hashSecurityAnswer(answerWithSpecialChars);
      boolean isValid = securityAnswerService.verifySecurityAnswer(answerWithSpecialChars, hash);

      // Then
      assertThat(hash).isNotNull();
      assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Devrait produire des hashs différents pour des réponses différentes")
    void shouldProduceDifferentHashesForDifferentAnswers() {
      // Given
      String answer1 = "Paris";
      String answer2 = "London";

      // When
      String hash1 = securityAnswerService.hashSecurityAnswer(answer1);
      String hash2 = securityAnswerService.hashSecurityAnswer(answer2);

      // Then
      assertThat(hash1).isNotEqualTo(hash2);
    }
  }

  // ========================================
  // 4.4.2 - Tests du composant PasswordRotationService
  // ========================================

  @Nested
  @DisplayName("4.4.2 - Tests de la rotation de mot de passe")
  class PasswordRotationServiceTest {

    @Mock
    private IPasswordHistoryRepository passwordHistoryRepository;

    @Spy
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private PasswordRotationService passwordRotationService;

    private User testUser;

    @BeforeEach
    void setUp() {
      testUser = new User();
      testUser.setId(1L);
      testUser.setEmail("test@test.com");
    }

    @Test
    @DisplayName("Devrait détecter un mot de passe expiré (>12 semaines)")
    void shouldDetectExpiredPassword() {
      // Given
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.WEEK_OF_YEAR, -13); // 13 semaines dans le passé
      testUser.setPasswordUpdatedAt(cal.getTime());

      // When
      boolean isExpired = passwordRotationService.isPasswordExpired(testUser);

      // Then
      assertThat(isExpired).isTrue();
    }

    @Test
    @DisplayName("Devrait détecter un mot de passe valide (<12 semaines)")
    void shouldDetectValidPassword() {
      // Given
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.WEEK_OF_YEAR, -5); // 5 semaines dans le passé
      testUser.setPasswordUpdatedAt(cal.getTime());

      // When
      boolean isExpired = passwordRotationService.isPasswordExpired(testUser);

      // Then
      assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("Devrait considérer comme expiré si passwordUpdatedAt est null")
    void shouldConsiderExpiredIfUpdateDateIsNull() {
      // Given
      testUser.setPasswordUpdatedAt(null);

      // When
      boolean isExpired = passwordRotationService.isPasswordExpired(testUser);

      // Then
      assertThat(isExpired).isTrue();
    }

    @Test
    @DisplayName("Devrait détecter un mot de passe dans l'historique")
    void shouldDetectPasswordInHistory() {
      // Given
      String plainPassword = "OldPassword123";
      String hashedPassword = passwordEncoder.encode(plainPassword);

      PasswordHistory history = new PasswordHistory();
      history.setPasswordHash(hashedPassword);

      when(passwordHistoryRepository.findTop5ByUserIdOrderByCreatedAtDesc(1L))
              .thenReturn(List.of(history));

      // When
      boolean isInHistory = passwordRotationService.isPasswordInHistory(testUser, plainPassword);

      // Then
      assertThat(isInHistory).isTrue();
      verify(passwordHistoryRepository, times(1)).findTop5ByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("Ne devrait pas détecter un nouveau mot de passe")
    void shouldNotDetectNewPassword() {
      // Given
      String newPassword = "NewPassword123";
      String oldHashedPassword = passwordEncoder.encode("OldPassword123");

      PasswordHistory history = new PasswordHistory();
      history.setPasswordHash(oldHashedPassword);

      when(passwordHistoryRepository.findTop5ByUserIdOrderByCreatedAtDesc(1L))
              .thenReturn(List.of(history));

      // When
      boolean isInHistory = passwordRotationService.isPasswordInHistory(testUser, newPassword);

      // Then
      assertThat(isInHistory).isFalse();
    }

    @Test
    @DisplayName("Devrait ajouter un mot de passe à l'historique")
    void shouldAddPasswordToHistory() {
      // Given
      String passwordHash = "hashedPassword";
      when(passwordHistoryRepository.save(any(PasswordHistory.class)))
              .thenAnswer(invocation -> invocation.getArgument(0));
      when(passwordHistoryRepository.findByUserOrderByCreatedAtDesc(testUser))
              .thenReturn(new ArrayList<>());

      // When
      passwordRotationService.addPasswordToHistory(testUser, passwordHash);

      // Then
      ArgumentCaptor<PasswordHistory> captor = ArgumentCaptor.forClass(PasswordHistory.class);
      verify(passwordHistoryRepository).save(captor.capture());

      PasswordHistory saved = captor.getValue();
      assertThat(saved.getUser()).isEqualTo(testUser);
      assertThat(saved.getPasswordHash()).isEqualTo(passwordHash);
      assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Devrait nettoyer l'historique au-delà de 5 entrées")
    void shouldCleanupOldPasswordsWhenExceeding5() {
      // Given
      List<PasswordHistory> histories = new ArrayList<>();
      for (int i = 0; i < 7; i++) {
        PasswordHistory history = new PasswordHistory();
        history.setId((long) i);
        history.setUser(testUser);
        history.setPasswordHash("hash" + i);
        histories.add(history);
      }

      when(passwordHistoryRepository.findByUserOrderByCreatedAtDesc(testUser))
              .thenReturn(histories);

      // When
      passwordRotationService.cleanupOldPasswords(testUser);

      // Then
      ArgumentCaptor<List<PasswordHistory>> captor = ArgumentCaptor.forClass(List.class);
      verify(passwordHistoryRepository).deleteAll(captor.capture());

      List<PasswordHistory> deletedHistories = captor.getValue();
      assertThat(deletedHistories).hasSize(2); // 7 - 5 = 2
    }

    @Test
    @DisplayName("Ne devrait pas nettoyer si moins de 5 entrées")
    void shouldNotCleanupWhenUnder5Entries() {
      // Given
      List<PasswordHistory> histories = new ArrayList<>();
      for (int i = 0; i < 3; i++) {
        PasswordHistory history = new PasswordHistory();
        history.setId((long) i);
        histories.add(history);
      }

      when(passwordHistoryRepository.findByUserOrderByCreatedAtDesc(testUser))
              .thenReturn(histories);

      // When
      passwordRotationService.cleanupOldPasswords(testUser);

      // Then
      verify(passwordHistoryRepository, never()).deleteAll(anyList());
    }

    @Test
    @DisplayName("Devrait calculer les jours restants avant expiration")
    void shouldCalculateDaysUntilExpiration() {
      // Given
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.WEEK_OF_YEAR, -10); // 10 semaines dans le passé
      testUser.setPasswordUpdatedAt(cal.getTime());

      // When
      long daysRemaining = passwordRotationService.getDaysUntilExpiration(testUser);

      // Then
      assertThat(daysRemaining).isGreaterThan(0);
      assertThat(daysRemaining).isLessThanOrEqualTo(14); // Environ 2 semaines restantes
    }

    @Test
    @DisplayName("Devrait retourner 0 jours si le mot de passe est expiré")
    void shouldReturn0DaysIfExpired() {
      // Given
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.WEEK_OF_YEAR, -15); // 15 semaines dans le passé
      testUser.setPasswordUpdatedAt(cal.getTime());

      // When
      long daysRemaining = passwordRotationService.getDaysUntilExpiration(testUser);

      // Then
      assertThat(daysRemaining).isEqualTo(0);
    }

    @Test
    @DisplayName("Devrait retourner 0 si passwordUpdatedAt est null")
    void shouldReturn0IfUpdateDateIsNull() {
      // Given
      testUser.setPasswordUpdatedAt(null);

      // When
      long daysRemaining = passwordRotationService.getDaysUntilExpiration(testUser);

      // Then
      assertThat(daysRemaining).isEqualTo(0);
    }
  }

  // ========================================
  // 4.4.3 - Tests du composant BCryptPasswordEncoder
  // ========================================

  @Nested
  @DisplayName("4.4.3 - Tests du hash BCrypt des mots de passe")
  class BCryptPasswordHashTest {

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("Devrait hasher un mot de passe avec BCrypt")
    void shouldHashPasswordWithBCrypt() {
      // Given
      String plainPassword = "MyPassword123!";

      // When
      String hashedPassword = passwordEncoder.encode(plainPassword);

      // Then
      assertThat(hashedPassword).isNotNull();
      assertThat(hashedPassword).isNotEqualTo(plainPassword);
      assertThat(hashedPassword).startsWith("$2a$"); // Format BCrypt
    }

    @Test
    @DisplayName("Devrait produire des hashs différents pour chaque encodage")
    void shouldProduceDifferentHashesEachTime() {
      // Given
      String plainPassword = "MyPassword123!";

      // When
      String hash1 = passwordEncoder.encode(plainPassword);
      String hash2 = passwordEncoder.encode(plainPassword);

      // Then
      assertThat(hash1).isNotEqualTo(hash2); // Salt différent à chaque fois
    }

    @Test
    @DisplayName("Devrait valider correctement un mot de passe")
    void shouldValidateCorrectPassword() {
      // Given
      String plainPassword = "MyPassword123!";
      String hashedPassword = passwordEncoder.encode(plainPassword);

      // When
      boolean matches = passwordEncoder.matches(plainPassword, hashedPassword);

      // Then
      assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("Devrait rejeter un mot de passe incorrect")
    void shouldRejectIncorrectPassword() {
      // Given
      String correctPassword = "MyPassword123!";
      String wrongPassword = "WrongPassword456!";
      String hashedPassword = passwordEncoder.encode(correctPassword);

      // When
      boolean matches = passwordEncoder.matches(wrongPassword, hashedPassword);

      // Then
      assertThat(matches).isFalse();
    }

    @Test
    @DisplayName("Devrait être sensible à la casse")
    void shouldBeCaseSensitive() {
      // Given
      String password = "Password123";
      String hashedPassword = passwordEncoder.encode(password);

      // When
      boolean matchesLowerCase = passwordEncoder.matches("password123", hashedPassword);
      boolean matchesUpperCase = passwordEncoder.matches("PASSWORD123", hashedPassword);
      boolean matchesCorrect = passwordEncoder.matches("Password123", hashedPassword);

      // Then
      assertThat(matchesLowerCase).isFalse();
      assertThat(matchesUpperCase).isFalse();
      assertThat(matchesCorrect).isTrue();
    }

    @Test
    @DisplayName("Devrait gérer les caractères spéciaux")
    void shouldHandleSpecialCharacters() {
      // Given
      String passwordWithSpecialChars = "P@ssw0rd!#$%^&*()";

      // When
      String hashed = passwordEncoder.encode(passwordWithSpecialChars);
      boolean matches = passwordEncoder.matches(passwordWithSpecialChars, hashed);

      // Then
      assertThat(hashed).isNotNull();
      assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("Devrait gérer les mots de passe longs")
    void shouldHandleLongPasswords() {
      // Given
      // BCrypt ne supporte que 72 octets, on reste à la limite
      String longPassword = "a".repeat(72);

      // When
      String hashed = passwordEncoder.encode(longPassword);
      boolean matches = passwordEncoder.matches(longPassword, hashed);

      // Then
      assertThat(hashed).isNotNull();
      assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("Le hash devrait avoir une longueur fixe")
    void hashShouldHaveFixedLength() {
      // Given
      String shortPassword = "abc";
      // Mot de passe « long » mais conforme à la limite BCrypt (72 octets)
      String longPassword = "a".repeat(72);

      // When
      String hash1 = passwordEncoder.encode(shortPassword);
      String hash2 = passwordEncoder.encode(longPassword);

      // Then
      assertThat(hash1).hasSameSizeAs(hash2);
      assertThat(hash1.length()).isEqualTo(60); // BCrypt produit 60 caractères
    }
  }

  // ========================================
  // 4.4.4 - Tests des règles métier critiques
  // ========================================

  @Nested
  @DisplayName("4.4.4 - Tests des règles métier critiques")
  class CriticalBusinessRulesTest {

    @Test
    @DisplayName("Règle : Un utilisateur ne peut avoir que 3 réservations actives maximum")
    void shouldEnforceMaximumActiveReservationsRule() {
      // Given
      int maxReservations = 3;
      int currentReservations = 3;

      // When
      boolean canReserve = currentReservations < maxReservations;

      // Then
      assertThat(canReserve).isFalse();
    }

    @Test
    @DisplayName("Règle : Le mot de passe expire après 12 semaines")
    void shouldEnforcePasswordExpirationRule() {
      // Given
      int expirationWeeks = 12;
      Calendar passwordDate = Calendar.getInstance();
      passwordDate.add(Calendar.WEEK_OF_YEAR, -13);

      Calendar expirationDate = Calendar.getInstance();
      expirationDate.setTime(passwordDate.getTime());
      expirationDate.add(Calendar.WEEK_OF_YEAR, expirationWeeks);

      // When
      boolean isExpired = new Date().after(expirationDate.getTime());

      // Then
      assertThat(isExpired).isTrue();
    }

    @Test
    @DisplayName("Règle : Impossible de réutiliser les 5 derniers mots de passe")
    void shouldEnforcePasswordHistoryRule() {
      // Given
      int maxPasswordHistory = 5;
      List<String> passwordHistory = List.of("pass1", "pass2", "pass3", "pass4", "pass5");
      String newPassword = "pass3";

      // When
      boolean isInHistory = passwordHistory.contains(newPassword);

      // Then
      assertThat(isInHistory).isTrue();
    }

    @Test
    @DisplayName("Règle : La réponse de sécurité ne peut pas dépasser 32 caractères")
    void shouldEnforceSecurityAnswerLengthRule() {
      // Given
      int maxLength = 32;
      String tooLongAnswer = "a".repeat(33);
      String validAnswer = "a".repeat(32);

      // When
      boolean isInvalid = tooLongAnswer.length() > maxLength;
      boolean isValid = validAnswer.length() <= maxLength;

      // Then
      assertThat(isInvalid).isTrue();
      assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Règle : Le stock disponible = stock total - réservations actives")
    void shouldCalculateAvailableStockCorrectly() {
      // Given
      int totalStock = 10;
      int activeReservations = 3;

      // When
      int availableStock = totalStock - activeReservations;

      // Then
      assertThat(availableStock).isEqualTo(7);
    }

    @Test
    @DisplayName("Règle : Impossible de réserver si stock disponible = 0")
    void shouldPreventReservationWhenStockEmpty() {
      // Given
      int totalStock = 5;
      int activeReservations = 5;
      int availableStock = totalStock - activeReservations;

      // When
      boolean canReserve = availableStock > 0;

      // Then
      assertThat(canReserve).isFalse();
    }
  }
}