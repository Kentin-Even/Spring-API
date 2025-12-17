package fr.ensitech.biblio2.service;

import fr.ensitech.biblio2.dto.AuthenticationResponse;
import fr.ensitech.biblio2.entity.SecurityQuestion;
import fr.ensitech.biblio2.entity.User;
import fr.ensitech.biblio2.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests complémentaires UserService - Coverage 100%")
class UserServiceTest {

  @Mock
  private IUserRepository userRepository;

  @Mock
  private EmailService emailService;

  @Mock
  private SecurityAnswerService securityAnswerService;

  @Mock
  private PasswordRotationService passwordRotationService;

  @InjectMocks
  private UserService userService;

  private User testUser;
  private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setFirstName("John");
    testUser.setLastName("Doe");
    testUser.setEmail("john.doe@test.com");
    testUser.setPassword(passwordEncoder.encode("Password123!"));
    testUser.setRole("U");
    testUser.setBirthdate(new Date());
    testUser.setActive(true);
    testUser.setSecurityQuestion(SecurityQuestion.CHILDHOOD_CITY);
    testUser.setSecurityAnswerHash("hashedAnswer");
    testUser.setPasswordUpdatedAt(new Date());
  }

  @Nested
  @DisplayName("getUserById - Récupération utilisateur par ID")
  class GetUserById {

    @Test
    @DisplayName("Devrait retourner l'utilisateur quand il existe")
    void shouldReturnUserWhenExists() throws Exception {
      // Given
      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

      // When
      User result = userService.getUserById(1L);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getEmail()).isEqualTo("john.doe@test.com");

      verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Devrait retourner null quand l'utilisateur n'existe pas")
    void shouldReturnNullWhenUserNotExists() throws Exception {
      // Given
      when(userRepository.findById(999L)).thenReturn(Optional.empty());

      // When
      User result = userService.getUserById(999L);

      // Then
      assertThat(result).isNull();

      verify(userRepository, times(1)).findById(999L);
    }
  }

  @Nested
  @DisplayName("getUsersByBirthdate - Recherche par date de naissance")
  class GetUsersByBirthdate {

    @Test
    @DisplayName("Devrait retourner les utilisateurs dans la plage de dates")
    void shouldReturnUsersInDateRange() throws Exception {
      // Given
      Date dateInf = new Date(2000, 0, 1);
      Date dateSup = new Date(2010, 11, 31);

      User user1 = new User();
      user1.setId(1L);
      user1.setBirthdate(new Date(2005, 5, 15));

      User user2 = new User();
      user2.setId(2L);
      user2.setBirthdate(new Date(2008, 3, 20));

      List<User> expectedUsers = Arrays.asList(user1, user2);

      when(userRepository.findByBirthdateBetween(dateInf, dateSup)).thenReturn(expectedUsers);

      // When
      List<User> result = userService.getUsersByBirthdate(dateInf, dateSup);

      // Then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(2);
      assertThat(result).containsExactly(user1, user2);

      verify(userRepository, times(1)).findByBirthdateBetween(dateInf, dateSup);
    }

    @Test
    @DisplayName("Devrait retourner une liste vide si aucun utilisateur dans la plage")
    void shouldReturnEmptyListWhenNoUsersInRange() throws Exception {
      // Given
      Date dateInf = new Date(1980, 0, 1);
      Date dateSup = new Date(1990, 11, 31);

      when(userRepository.findByBirthdateBetween(dateInf, dateSup)).thenReturn(Arrays.asList());

      // When
      List<User> result = userService.getUsersByBirthdate(dateInf, dateSup);

      // Then
      assertThat(result).isNotNull();
      assertThat(result).isEmpty();

      verify(userRepository, times(1)).findByBirthdateBetween(dateInf, dateSup);
    }
  }

  @Nested
  @DisplayName("verifySecurityAnswer - Vérification réponse de sécurité")
  class VerifySecurityAnswer {

    @Test
    @DisplayName("Devrait retourner true quand la réponse est correcte")
    void shouldReturnTrueWhenAnswerIsCorrect() throws Exception {
      // Given
      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
      when(securityAnswerService.verifySecurityAnswer("Paris", "hashedAnswer")).thenReturn(true);

      // When
      boolean result = userService.verifySecurityAnswer(1L, "Paris");

      // Then
      assertThat(result).isTrue();

      verify(userRepository, times(1)).findById(1L);
      verify(securityAnswerService, times(1)).verifySecurityAnswer("Paris", "hashedAnswer");
    }

    @Test
    @DisplayName("Devrait retourner false quand la réponse est incorrecte")
    void shouldReturnFalseWhenAnswerIsIncorrect() throws Exception {
      // Given
      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
      when(securityAnswerService.verifySecurityAnswer("London", "hashedAnswer")).thenReturn(false);

      // When
      boolean result = userService.verifySecurityAnswer(1L, "London");

      // Then
      assertThat(result).isFalse();

      verify(userRepository, times(1)).findById(1L);
      verify(securityAnswerService, times(1)).verifySecurityAnswer("London", "hashedAnswer");
    }

    @Test
    @DisplayName("Devrait lever une exception si l'utilisateur n'existe pas")
    void shouldThrowExceptionWhenUserNotFound() {
      // Given
      when(userRepository.findById(999L)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> userService.verifySecurityAnswer(999L, "answer"))
              .isInstanceOf(Exception.class)
              .hasMessageContaining("non trouvé");

      verify(userRepository, times(1)).findById(999L);
      verify(securityAnswerService, never()).verifySecurityAnswer(anyString(), anyString());
    }

    @Test
    @DisplayName("Devrait lever une exception si aucune question de sécurité configurée")
    void shouldThrowExceptionWhenNoSecurityQuestionConfigured() {
      // Given
      testUser.setSecurityAnswerHash(null);
      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

      // When & Then
      assertThatThrownBy(() -> userService.verifySecurityAnswer(1L, "answer"))
              .isInstanceOf(Exception.class)
              .hasMessageContaining("Aucune question de sécurité");

      verify(userRepository, times(1)).findById(1L);
      verify(securityAnswerService, never()).verifySecurityAnswer(anyString(), anyString());
    }
  }

  @Nested
  @DisplayName("sendActivationMail - Envoi email d'activation")
  class SendActivationMail {

    @Test
    @DisplayName("Devrait envoyer l'email d'activation")
    void shouldSendActivationEmail() throws Exception {
      // Given
      when(userRepository.findByEmail("john.doe@test.com")).thenReturn(testUser);
      doNothing().when(emailService).sendActivationEmail(anyString(), anyLong());

      // When
      userService.sendActivationMail("john.doe@test.com");

      // Then
      verify(userRepository, times(1)).findByEmail("john.doe@test.com");
      verify(emailService, times(1)).sendActivationEmail("john.doe@test.com", 1L);
    }

    @Test
    @DisplayName("Devrait lever une exception si l'utilisateur n'existe pas")
    void shouldThrowExceptionWhenUserNotFound() {
      // Given
      when(userRepository.findByEmail("unknown@test.com")).thenReturn(null);

      // When & Then
      assertThatThrownBy(() -> userService.sendActivationMail("unknown@test.com"))
              .isInstanceOf(Exception.class)
              .hasMessageContaining("not found");

      verify(userRepository, times(1)).findByEmail("unknown@test.com");
      verify(emailService, never()).sendActivationEmail(anyString(), anyLong());
    }
  }

  @Nested
  @DisplayName("updateUserProfile - Mise à jour du profil")
  class UpdateUserProfile {

    @Test
    @DisplayName("Devrait mettre à jour tous les champs du profil")
    void shouldUpdateAllProfileFields() throws Exception {
      // Given
      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
      when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

      User updatedUser = new User();
      updatedUser.setFirstName("Jane");
      updatedUser.setLastName("Smith");
      updatedUser.setBirthdate(new Date(1995, 5, 15));

      // When
      userService.updateUserProfile(1L, updatedUser);

      // Then
      assertThat(testUser.getFirstName()).isEqualTo("Jane");
      assertThat(testUser.getLastName()).isEqualTo("Smith");
      assertThat(testUser.getBirthdate()).isEqualTo(updatedUser.getBirthdate());

      verify(userRepository, times(1)).findById(1L);
      verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Devrait mettre à jour seulement le prénom")
    void shouldUpdateOnlyFirstName() throws Exception {
      // Given
      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
      when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

      User updatedUser = new User();
      updatedUser.setFirstName("Jane");
      // Pas de lastName ni birthdate

      String originalLastName = testUser.getLastName();
      Date originalBirthdate = testUser.getBirthdate();

      // When
      userService.updateUserProfile(1L, updatedUser);

      // Then
      assertThat(testUser.getFirstName()).isEqualTo("Jane");
      assertThat(testUser.getLastName()).isEqualTo(originalLastName); // Inchangé
      assertThat(testUser.getBirthdate()).isEqualTo(originalBirthdate); // Inchangé

      verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Devrait ignorer les champs vides ou null")
    void shouldIgnoreEmptyOrNullFields() throws Exception {
      // Given
      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
      when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

      User updatedUser = new User();
      updatedUser.setFirstName(""); // Vide
      updatedUser.setLastName(null); // Null

      String originalFirstName = testUser.getFirstName();
      String originalLastName = testUser.getLastName();

      // When
      userService.updateUserProfile(1L, updatedUser);

      // Then
      assertThat(testUser.getFirstName()).isEqualTo(originalFirstName); // Inchangé
      assertThat(testUser.getLastName()).isEqualTo(originalLastName); // Inchangé

      verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Devrait lever une exception si l'utilisateur n'existe pas")
    void shouldThrowExceptionWhenUserNotFound() {
      // Given
      when(userRepository.findById(999L)).thenReturn(Optional.empty());

      User updatedUser = new User();
      updatedUser.setFirstName("Jane");

      // When & Then
      assertThatThrownBy(() -> userService.updateUserProfile(999L, updatedUser))
              .isInstanceOf(Exception.class)
              .hasMessageContaining("non trouvé");

      verify(userRepository, times(1)).findById(999L);
      verify(userRepository, never()).save(any(User.class));
    }
  }

  @Nested
  @DisplayName("renewPassword - Renouvellement mot de passe")
  class RenewPassword {

    @Test
    @DisplayName("Devrait renouveler le mot de passe avec succès")
    void shouldRenewPasswordSuccessfully() throws Exception {
      // Given
      when(userRepository.findByEmail("john.doe@test.com")).thenReturn(testUser);
      when(passwordRotationService.isPasswordInHistory(any(User.class), anyString())).thenReturn(false);
      when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
      doNothing().when(passwordRotationService).addPasswordToHistory(any(User.class), anyString());
      doNothing().when(emailService).sendPasswordChangedEmail(anyString(), anyString(), anyString());

      // When
      userService.renewPassword("john.doe@test.com", "Password123!", "NewPassword789!");

      // Then
      verify(userRepository, times(1)).findByEmail("john.doe@test.com");
      verify(passwordRotationService, times(1)).isPasswordInHistory(any(User.class), eq("NewPassword789!"));
      verify(passwordRotationService, times(1)).addPasswordToHistory(any(User.class), anyString());
      verify(userRepository, times(1)).save(testUser);
      verify(emailService, times(1)).sendPasswordChangedEmail("john.doe@test.com", "John", "Doe");
    }

    @Test
    @DisplayName("Devrait lever une exception si l'utilisateur n'existe pas")
    void shouldThrowExceptionWhenUserNotFound() {
      // Given
      when(userRepository.findByEmail("unknown@test.com")).thenReturn(null);

      // When & Then
      assertThatThrownBy(() -> userService.renewPassword("unknown@test.com", "old", "new"))
              .isInstanceOf(Exception.class)
              .hasMessageContaining("non trouvé");

      verify(userRepository, times(1)).findByEmail("unknown@test.com");
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Devrait lever une exception si l'ancien mot de passe est incorrect")
    void shouldThrowExceptionWhenOldPasswordIncorrect() {
      // Given
      when(userRepository.findByEmail("john.doe@test.com")).thenReturn(testUser);

      // When & Then
      assertThatThrownBy(() -> userService.renewPassword("john.doe@test.com", "WrongPassword", "NewPassword789!"))
              .isInstanceOf(Exception.class)
              .hasMessageContaining("incorrect");

      verify(userRepository, times(1)).findByEmail("john.doe@test.com");
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Devrait lever une exception si le nouveau mot de passe est identique à l'ancien")
    void shouldThrowExceptionWhenNewPasswordSameAsOld() {
      // Given
      when(userRepository.findByEmail("john.doe@test.com")).thenReturn(testUser);

      // When & Then
      assertThatThrownBy(() -> userService.renewPassword("john.doe@test.com", "Password123!", "Password123!"))
              .isInstanceOf(Exception.class)
              .hasMessageContaining("différent");

      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Devrait lever une exception si le mot de passe est dans l'historique")
    void shouldThrowExceptionWhenPasswordInHistory() {
      // Given
      when(userRepository.findByEmail("john.doe@test.com")).thenReturn(testUser);
      when(passwordRotationService.isPasswordInHistory(testUser, "OldPassword123!")).thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> userService.renewPassword("john.doe@test.com", "Password123!", "OldPassword123!"))
              .isInstanceOf(Exception.class)
              .hasMessageContaining("5 derniers");

      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Devrait lever une exception si le mot de passe est trop court")
    void shouldThrowExceptionWhenPasswordTooShort() {
      // Given
      when(userRepository.findByEmail("john.doe@test.com")).thenReturn(testUser);
      when(passwordRotationService.isPasswordInHistory(any(User.class), anyString())).thenReturn(false);

      // When & Then
      assertThatThrownBy(() -> userService.renewPassword("john.doe@test.com", "Password123!", "12345"))
              .isInstanceOf(Exception.class)
              .hasMessageContaining("au moins 6 caractères");

      verify(userRepository, never()).save(any(User.class));
    }
  }

  @Nested
  @DisplayName("isPasswordExpired - Vérification expiration mot de passe")
  class IsPasswordExpired {

    @Test
    @DisplayName("Devrait retourner true si le mot de passe est expiré")
    void shouldReturnTrueWhenPasswordExpired() throws Exception {
      // Given
      when(userRepository.findByEmail("john.doe@test.com")).thenReturn(testUser);
      when(passwordRotationService.isPasswordExpired(testUser)).thenReturn(true);

      // When
      boolean result = userService.isPasswordExpired("john.doe@test.com");

      // Then
      assertThat(result).isTrue();

      verify(userRepository, times(1)).findByEmail("john.doe@test.com");
      verify(passwordRotationService, times(1)).isPasswordExpired(testUser);
    }

    @Test
    @DisplayName("Devrait retourner false si le mot de passe n'est pas expiré")
    void shouldReturnFalseWhenPasswordNotExpired() throws Exception {
      // Given
      when(userRepository.findByEmail("john.doe@test.com")).thenReturn(testUser);
      when(passwordRotationService.isPasswordExpired(testUser)).thenReturn(false);

      // When
      boolean result = userService.isPasswordExpired("john.doe@test.com");

      // Then
      assertThat(result).isFalse();

      verify(userRepository, times(1)).findByEmail("john.doe@test.com");
      verify(passwordRotationService, times(1)).isPasswordExpired(testUser);
    }

    @Test
    @DisplayName("Devrait lever une exception si l'utilisateur n'existe pas")
    void shouldThrowExceptionWhenUserNotFound() {
      // Given
      when(userRepository.findByEmail("unknown@test.com")).thenReturn(null);

      // When & Then
      assertThatThrownBy(() -> userService.isPasswordExpired("unknown@test.com"))
              .isInstanceOf(Exception.class)
              .hasMessageContaining("non trouvé");

      verify(userRepository, times(1)).findByEmail("unknown@test.com");
      verify(passwordRotationService, never()).isPasswordExpired(any(User.class));
    }
  }

  @Nested
  @DisplayName("getDaysUntilPasswordExpiration - Jours avant expiration")
  class GetDaysUntilPasswordExpiration {

    @Test
    @DisplayName("Devrait retourner le nombre de jours restants")
    void shouldReturnDaysRemaining() throws Exception {
      // Given
      when(userRepository.findByEmail("john.doe@test.com")).thenReturn(testUser);
      when(passwordRotationService.getDaysUntilExpiration(testUser)).thenReturn(15L);

      // When
      long result = userService.getDaysUntilPasswordExpiration("john.doe@test.com");

      // Then
      assertThat(result).isEqualTo(15L);

      verify(userRepository, times(1)).findByEmail("john.doe@test.com");
      verify(passwordRotationService, times(1)).getDaysUntilExpiration(testUser);
    }

    @Test
    @DisplayName("Devrait retourner 0 si le mot de passe est déjà expiré")
    void shouldReturnZeroWhenPasswordExpired() throws Exception {
      // Given
      when(userRepository.findByEmail("john.doe@test.com")).thenReturn(testUser);
      when(passwordRotationService.getDaysUntilExpiration(testUser)).thenReturn(0L);

      // When
      long result = userService.getDaysUntilPasswordExpiration("john.doe@test.com");

      // Then
      assertThat(result).isEqualTo(0L);

      verify(userRepository, times(1)).findByEmail("john.doe@test.com");
      verify(passwordRotationService, times(1)).getDaysUntilExpiration(testUser);
    }

    @Test
    @DisplayName("Devrait lever une exception si l'utilisateur n'existe pas")
    void shouldThrowExceptionWhenUserNotFound() {
      // Given
      when(userRepository.findByEmail("unknown@test.com")).thenReturn(null);

      // When & Then
      assertThatThrownBy(() -> userService.getDaysUntilPasswordExpiration("unknown@test.com"))
              .isInstanceOf(Exception.class)
              .hasMessageContaining("non trouvé");

      verify(userRepository, times(1)).findByEmail("unknown@test.com");
      verify(passwordRotationService, never()).getDaysUntilExpiration(any(User.class));
    }
  }

  @Nested
  @DisplayName("createUser - Scénarios complémentaires")
  class CreateUserScenariosComplementaires {

    @Test
    @DisplayName("Devrait lever une exception si la réponse de sécurité est vide")
    void shouldThrowExceptionWhenSecurityAnswerEmpty() {
      // Given
      User newUser = new User();
      newUser.setEmail("new@test.com");
      newUser.setPassword("Password123!");
      newUser.setSecurityQuestion(SecurityQuestion.FIRST_PET);
      newUser.setSecurityAnswerHash(""); // Réponse vide

      when(userRepository.findByEmail("new@test.com")).thenReturn(null);

      // When & Then
      assertThatThrownBy(() -> userService.createUser(newUser))
              .isInstanceOf(Exception.class)
              .hasMessageContaining("obligatoire");

      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Devrait lever une exception si la réponse de sécurité est null")
    void shouldThrowExceptionWhenSecurityAnswerNull() {
      // Given
      User newUser = new User();
      newUser.setEmail("new@test.com");
      newUser.setPassword("Password123!");
      newUser.setSecurityQuestion(SecurityQuestion.FIRST_PET);
      newUser.setSecurityAnswerHash(null); // Réponse null

      when(userRepository.findByEmail("new@test.com")).thenReturn(null);

      // When & Then
      assertThatThrownBy(() -> userService.createUser(newUser))
              .isInstanceOf(Exception.class)
              .hasMessageContaining("obligatoire");

      verify(userRepository, never()).save(any(User.class));
    }
  }

  @Nested
  @DisplayName("updateUserPassword - Scénario nouveau mot de passe identique")
  class UpdateUserPasswordSamePassword {

    @Test
    @DisplayName("Devrait lever une exception si le nouveau mot de passe est identique à l'ancien")
    void shouldThrowExceptionWhenNewPasswordSameAsOld() {
      // Given
      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

      // When & Then
      assertThatThrownBy(() -> userService.updateUserPassword(1L, "Password123!", "Password123!"))
              .isInstanceOf(Exception.class)
              .hasMessageContaining("différent");

      verify(userRepository, never()).save(any(User.class));
      verify(emailService, never()).sendPasswordChangedEmail(anyString(), anyString(), anyString());
    }
  }

  @Nested
  @DisplayName("authenticatedUser - Scénario sans question de sécurité")
  class AuthenticatedUserWithoutSecurityQuestion {

    @Test
    @DisplayName("Devrait authentifier sans demander de question de sécurité si non configurée")
    void shouldAuthenticateWithoutSecurityQuestionWhenNotConfigured() throws Exception {
      // Given
      testUser.setSecurityQuestion(null);
      testUser.setSecurityAnswerHash(null);

      when(userRepository.findByEmail("john.doe@test.com")).thenReturn(testUser);
      when(passwordRotationService.isPasswordExpired(testUser)).thenReturn(false);

      // When
      AuthenticationResponse response = userService.authenticatedUser(
              "john.doe@test.com",
              "Password123!"
      );

      // Then
      assertThat(response.isRequiresSecurityQuestion()).isFalse();
      assertThat(response.getSecurityQuestion()).isNull();
      assertThat(response.getMessage()).contains("réussie");
      assertThat(response.getUserId()).isEqualTo(1L);

      verify(userRepository, times(1)).findByEmail("john.doe@test.com");
      verify(passwordRotationService, times(1)).isPasswordExpired(testUser);
    }

    @Test
    @DisplayName("Devrait authentifier si seulement la question est configurée mais pas la réponse")
    void shouldAuthenticateWhenOnlyQuestionConfiguredButNoAnswer() throws Exception {
      // Given
      testUser.setSecurityQuestion(SecurityQuestion.CHILDHOOD_CITY);
      testUser.setSecurityAnswerHash(null); // Pas de réponse

      when(userRepository.findByEmail("john.doe@test.com")).thenReturn(testUser);
      when(passwordRotationService.isPasswordExpired(testUser)).thenReturn(false);

      // When
      AuthenticationResponse response = userService.authenticatedUser(
              "john.doe@test.com",
              "Password123!"
      );

      // Then
      assertThat(response.isRequiresSecurityQuestion()).isFalse();
      assertThat(response.getMessage()).contains("réussie");

      verify(userRepository, times(1)).findByEmail("john.doe@test.com");
    }
  }
}