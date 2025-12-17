package fr.ensitech.biblio2.integration;

import fr.ensitech.biblio2.dto.AuthenticationResponse;
import fr.ensitech.biblio2.entity.SecurityQuestion;
import fr.ensitech.biblio2.entity.User;
import fr.ensitech.biblio2.repository.IPasswordHistoryRepository;
import fr.ensitech.biblio2.repository.IUserRepository;
import fr.ensitech.biblio2.service.SecurityAnswerService;
import fr.ensitech.biblio2.service.UserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceIntegrationTest {

  @Autowired
  private UserService userService;

  @Autowired
  private IUserRepository userRepository;

  @Autowired
  private IPasswordHistoryRepository passwordHistoryRepository;

  @Autowired
  private SecurityAnswerService securityAnswerService;

  @MockitoBean
  private JavaMailSender mailSender;

  private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @BeforeEach
  void setUp() {
    passwordHistoryRepository.deleteAll();
    userRepository.deleteAll();
    reset(mailSender);
  }

  // ==================== Tests de création d'utilisateur ====================

  @Test
  @DisplayName("Création d'un utilisateur avec succès")
  void shouldCreateUserSuccessfully() throws Exception {
    //GIVEN
    User user = createValidUser("jean.dupont@example.com", "Jean", "DUPONT");

    //WHEN
    userService.createUser(user);

    //THEN
    User savedUser = userRepository.findByEmail("jean.dupont@example.com");
    assertThat(savedUser).isNotNull();
    assertThat(savedUser.getEmail()).isEqualTo("jean.dupont@example.com");
    assertThat(savedUser.getFirstName()).isEqualTo("Jean");
    assertThat(savedUser.getLastName()).isEqualTo("DUPONT");
    assertThat(savedUser.isActive()).isFalse();
    assertThat(savedUser.getPasswordUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Création d'utilisateur hash le mot de passe")
  void shouldHashPasswordOnUserCreation() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    String plainPassword = user.getPassword();

    //WHEN
    userService.createUser(user);

    //THEN
    User savedUser = userRepository.findByEmail("test@example.com");
    assertThat(savedUser.getPassword()).isNotEqualTo(plainPassword);
    assertThat(passwordEncoder.matches(plainPassword, savedUser.getPassword())).isTrue();
  }

  @Test
  @DisplayName("Création d'utilisateur hash la réponse de sécurité")
  void shouldHashSecurityAnswerOnUserCreation() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    String plainAnswer = "Paris";
    user.setSecurityAnswerHash(plainAnswer);

    //WHEN
    userService.createUser(user);

    //THEN
    User savedUser = userRepository.findByEmail("test@example.com");
    assertThat(savedUser.getSecurityAnswerHash()).isNotEqualTo(plainAnswer);
    assertThat(securityAnswerService.verifySecurityAnswer(plainAnswer, savedUser.getSecurityAnswerHash())).isTrue();
  }

  @Test
  @DisplayName("Création d'utilisateur ajoute le mot de passe à l'historique")
  void shouldAddPasswordToHistoryOnUserCreation() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");

    //WHEN
    userService.createUser(user);

    //THEN
    User savedUser = userRepository.findByEmail("test@example.com");
    long historyCount = passwordHistoryRepository.countByUser(savedUser);
    assertThat(historyCount).isEqualTo(1);
  }

  @Test
  @DisplayName("Création d'utilisateur avec email existant doit lever une exception")
  void shouldThrowExceptionWhenCreatingUserWithExistingEmail() throws Exception {
    //GIVEN
    User user1 = createValidUser("duplicate@example.com", "User1", "TEST");
    userService.createUser(user1);

    User user2 = createValidUser("duplicate@example.com", "User2", "TEST");

    //WHEN & THEN
    assertThatThrownBy(() -> userService.createUser(user2))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("User already exists");
  }

  @Test
  @DisplayName("Création d'utilisateur sans question de sécurité doit lever une exception")
  void shouldThrowExceptionWhenCreatingUserWithoutSecurityQuestion() {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    user.setSecurityQuestion(null);

    //WHEN & THEN
    assertThatThrownBy(() -> userService.createUser(user))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Question de sécurité obligatoire");
  }

  @Test
  @DisplayName("Création d'utilisateur sans réponse de sécurité doit lever une exception")
  void shouldThrowExceptionWhenCreatingUserWithoutSecurityAnswer() {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    user.setSecurityAnswerHash(null);

    //WHEN & THEN
    assertThatThrownBy(() -> userService.createUser(user))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Réponse de sécurité obligatoire");
  }

  @Test
  @DisplayName("Création d'utilisateur avec réponse de sécurité vide doit lever une exception")
  void shouldThrowExceptionWhenCreatingUserWithEmptySecurityAnswer() {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    user.setSecurityAnswerHash("");

    //WHEN & THEN
    assertThatThrownBy(() -> userService.createUser(user))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Réponse de sécurité obligatoire");
  }

  @Test
  @DisplayName("Création d'utilisateur avec réponse de sécurité trop longue doit lever une exception")
  void shouldThrowExceptionWhenSecurityAnswerTooLong() {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    user.setSecurityAnswerHash("Cette réponse est beaucoup trop longue pour être acceptée");

    //WHEN & THEN
    assertThatThrownBy(() -> userService.createUser(user))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("La réponse de sécurité ne peut pas dépasser 32 caractères");
  }

  // ==================== Tests d'activation d'utilisateur ====================

  @Test
  @DisplayName("Activation d'un utilisateur")
  void shouldActivateUser() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    userService.createUser(user);
    User savedUser = userRepository.findByEmail("test@example.com");
    reset(mailSender);

    //WHEN
    User activatedUser = userService.activeUser(savedUser.getId());

    //THEN
    assertThat(activatedUser.isActive()).isTrue();
    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("Activation d'utilisateur envoie un email de confirmation")
  void shouldSendActivationConfirmationEmail() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Sophie", "MARTIN");
    userService.createUser(user);
    User savedUser = userRepository.findByEmail("test@example.com");
    reset(mailSender);

    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    //WHEN
    userService.activeUser(savedUser.getId());

    //THEN
    verify(mailSender, times(1)).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();
    assertThat(sentMessage.getTo()).containsExactly("test@example.com");
    assertThat(sentMessage.getSubject()).isEqualTo("Votre compte est maintenant actif - Biblio");
  }

  @Test
  @DisplayName("Activation d'utilisateur inexistant doit lever une exception")
  void shouldThrowExceptionWhenActivatingNonExistentUser() {
    //WHEN & THEN
    assertThatThrownBy(() -> userService.activeUser(9999L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("User not found");

    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  // ==================== Tests d'authentification ====================

  @Test
  @DisplayName("Authentification réussie sans question de sécurité")
  void shouldAuthenticateUserSuccessfully() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    String plainPassword = user.getPassword();
    userService.createUser(user);
    User savedUser = userRepository.findByEmail("test@example.com");
    userService.activeUser(savedUser.getId());

    // Retirer la question de sécurité
    savedUser.setSecurityQuestion(null);
    savedUser.setSecurityAnswerHash(null);
    userRepository.save(savedUser);

    //WHEN
    AuthenticationResponse response = userService.authenticatedUser("test@example.com", plainPassword);

    //THEN
    assertThat(response).isNotNull();
    assertThat(response.isRequiresSecurityQuestion()).isFalse();
    assertThat(response.getMessage()).isEqualTo("Connexion réussie");
    assertThat(response.getUserId()).isEqualTo(savedUser.getId());
  }

  @Test
  @DisplayName("Authentification avec question de sécurité requise")
  void shouldRequireSecurityQuestionOnAuthentication() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    String plainPassword = user.getPassword();
    userService.createUser(user);
    User savedUser = userRepository.findByEmail("test@example.com");
    userService.activeUser(savedUser.getId());

    //WHEN
    AuthenticationResponse response = userService.authenticatedUser("test@example.com", plainPassword);

    //THEN
    assertThat(response).isNotNull();
    assertThat(response.isRequiresSecurityQuestion()).isTrue();
    assertThat(response.getSecurityQuestion()).isEqualTo(SecurityQuestion.CHILDHOOD_CITY.getQuestionText());
    assertThat(response.getMessage()).isEqualTo("Question de sécurité requise");
    assertThat(response.getUserId()).isEqualTo(savedUser.getId());
  }

  @Test
  @DisplayName("Authentification avec mot de passe expiré")
  void shouldDetectExpiredPasswordOnAuthentication() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    String plainPassword = user.getPassword();
    userService.createUser(user);
    User savedUser = userRepository.findByEmail("test@example.com");
    userService.activeUser(savedUser.getId());

    // Expirer le mot de passe
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.WEEK_OF_YEAR, -13);
    savedUser.setPasswordUpdatedAt(cal.getTime());
    userRepository.save(savedUser);

    //WHEN
    AuthenticationResponse response = userService.authenticatedUser("test@example.com", plainPassword);

    //THEN
    assertThat(response).isNotNull();
    assertThat(response.isRequiresSecurityQuestion()).isTrue();
    assertThat(response.getMessage()).contains("Votre mot de passe a expiré");
  }

  @Test
  @DisplayName("Authentification avec email inexistant doit lever une exception")
  void shouldThrowExceptionWhenAuthenticatingWithInvalidEmail() {
    //WHEN & THEN
    assertThatThrownBy(() -> userService.authenticatedUser("inexistant@example.com", "password"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Invalid credentials");
  }

  @Test
  @DisplayName("Authentification avec mot de passe incorrect doit lever une exception")
  void shouldThrowExceptionWhenAuthenticatingWithInvalidPassword() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    userService.createUser(user);
    User savedUser = userRepository.findByEmail("test@example.com");
    userService.activeUser(savedUser.getId());

    //WHEN & THEN
    assertThatThrownBy(() -> userService.authenticatedUser("test@example.com", "wrongpassword"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Invalid credentials");
  }

  @Test
  @DisplayName("Authentification avec compte non activé doit lever une exception")
  void shouldThrowExceptionWhenAuthenticatingInactiveAccount() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    String plainPassword = user.getPassword();
    userService.createUser(user);

    //WHEN & THEN
    assertThatThrownBy(() -> userService.authenticatedUser("test@example.com", plainPassword))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Account not activated");
  }

  // ==================== Tests de vérification de réponse de sécurité ====================

  @Test
  @DisplayName("Vérification de réponse de sécurité correcte")
  void shouldVerifyCorrectSecurityAnswer() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    user.setSecurityAnswerHash("Paris");
    userService.createUser(user);
    User savedUser = userRepository.findByEmail("test@example.com");

    //WHEN
    boolean isValid = userService.verifySecurityAnswer(savedUser.getId(), "Paris");

    //THEN
    assertThat(isValid).isTrue();
  }

  @Test
  @DisplayName("Vérification de réponse de sécurité incorrecte")
  void shouldRejectIncorrectSecurityAnswer() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    user.setSecurityAnswerHash("Paris");
    userService.createUser(user);
    User savedUser = userRepository.findByEmail("test@example.com");

    //WHEN
    boolean isValid = userService.verifySecurityAnswer(savedUser.getId(), "Lyon");

    //THEN
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("Vérification de réponse de sécurité pour utilisateur inexistant")
  void shouldThrowExceptionWhenVerifyingAnswerForNonExistentUser() {
    //WHEN & THEN
    assertThatThrownBy(() -> userService.verifySecurityAnswer(9999L, "Paris"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Utilisateur non trouvé");
  }

  @Test
  @DisplayName("Vérification de réponse de sécurité sans question configurée")
  void shouldThrowExceptionWhenVerifyingAnswerWithoutConfiguredQuestion() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    userService.createUser(user);
    User savedUser = userRepository.findByEmail("test@example.com");
    savedUser.setSecurityAnswerHash(null);
    userRepository.save(savedUser);

    //WHEN & THEN
    assertThatThrownBy(() -> userService.verifySecurityAnswer(savedUser.getId(), "Paris"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Aucune question de sécurité configurée");
  }

  // ==================== Tests de suppression d'utilisateur ====================

  @Test
  @DisplayName("Suppression d'un utilisateur désactive le compte")
  void shouldDeactivateUserOnDeletion() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    userService.createUser(user);
    User savedUser = userRepository.findByEmail("test@example.com");
    userService.activeUser(savedUser.getId());
    reset(mailSender);

    //WHEN
    User deletedUser = userService.deleteUser(savedUser.getId());

    //THEN
    assertThat(deletedUser.isActive()).isFalse();
    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("Suppression d'utilisateur envoie un email de confirmation")
  void shouldSendUnsubscribeConfirmationEmail() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Sophie", "MARTIN");
    userService.createUser(user);
    User savedUser = userRepository.findByEmail("test@example.com");
    reset(mailSender);

    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    //WHEN
    userService.deleteUser(savedUser.getId());

    //THEN
    verify(mailSender, times(1)).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();
    assertThat(sentMessage.getTo()).containsExactly("test@example.com");
    assertThat(sentMessage.getSubject()).isEqualTo("Confirmation de désinscription - Biblio");
  }

  @Test
  @DisplayName("Suppression d'utilisateur inexistant doit lever une exception")
  void shouldThrowExceptionWhenDeletingNonExistentUser() {
    //WHEN & THEN
    assertThatThrownBy(() -> userService.deleteUser(9999L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("User not found");

    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  // ==================== Tests d'envoi d'email d'activation ====================

  @Test
  @DisplayName("Envoi d'email d'activation")
  void shouldSendActivationEmail() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    userService.createUser(user);
    reset(mailSender);

    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    //WHEN
    userService.sendActivationMail("test@example.com");

    //THEN
    verify(mailSender, times(1)).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();
    assertThat(sentMessage.getTo()).containsExactly("test@example.com");
    assertThat(sentMessage.getSubject()).isEqualTo("Activation de votre compte - Biblio");
  }

  @Test
  @DisplayName("Envoi d'email d'activation pour utilisateur inexistant")
  void shouldThrowExceptionWhenSendingActivationEmailToNonExistentUser() {
    //WHEN & THEN
    assertThatThrownBy(() -> userService.sendActivationMail("inexistant@example.com"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("User not found");

    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  // ==================== Tests de récupération d'utilisateur ====================

  @Test
  @DisplayName("Récupération d'utilisateur par ID")
  void shouldGetUserById() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Jean", "DUPONT");
    userService.createUser(user);
    User savedUser = userRepository.findByEmail("test@example.com");

    //WHEN
    User foundUser = userService.getUserById(savedUser.getId());

    //THEN
    assertThat(foundUser).isNotNull();
    assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
    assertThat(foundUser.getFirstName()).isEqualTo("Jean");
  }

  @Test
  @DisplayName("Récupération d'utilisateur inexistant retourne null")
  void shouldReturnNullWhenGettingNonExistentUser() throws Exception {
    //WHEN
    User foundUser = userService.getUserById(9999L);

    //THEN
    assertThat(foundUser).isNull();
  }

  @Test
  @DisplayName("Récupération d'utilisateurs par date de naissance")
  void shouldGetUsersByBirthdate() throws Exception {
    //GIVEN
    Calendar cal = Calendar.getInstance();
    cal.set(1990, Calendar.JANUARY, 15);
    Date birthdate1 = cal.getTime();

    cal.set(1995, Calendar.JUNE, 20);
    Date birthdate2 = cal.getTime();

    cal.set(2000, Calendar.DECEMBER, 10);
    Date birthdate3 = cal.getTime();

    User user1 = createValidUser("user1@example.com", "User1", "TEST");
    user1.setBirthdate(birthdate1);
    userService.createUser(user1);

    User user2 = createValidUser("user2@example.com", "User2", "TEST");
    user2.setBirthdate(birthdate2);
    userService.createUser(user2);

    User user3 = createValidUser("user3@example.com", "User3", "TEST");
    user3.setBirthdate(birthdate3);
    userService.createUser(user3);

    cal.set(1992, Calendar.JANUARY, 1);
    Date dateInf = cal.getTime();
    cal.set(1998, Calendar.DECEMBER, 31);
    Date dateSup = cal.getTime();

    //WHEN
    List<User> users = userService.getUsersByBirthdate(dateInf, dateSup);

    //THEN
    assertThat(users).hasSize(1);
    assertThat(users.get(0).getEmail()).isEqualTo("user2@example.com");
  }

  // ==================== Tests de mise à jour de profil ====================

  @Test
  @DisplayName("Mise à jour du prénom d'un utilisateur")
  void shouldUpdateUserFirstName() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Jean", "DUPONT");
    userService.createUser(user);
    User savedUser = userRepository.findByEmail("test@example.com");

    User updatedInfo = new User();
    updatedInfo.setFirstName("Pierre");

    //WHEN
    userService.updateUserProfile(savedUser.getId(), updatedInfo);

    //THEN
    User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
    assertThat(updatedUser.getFirstName()).isEqualTo("Pierre");
    assertThat(updatedUser.getLastName()).isEqualTo("DUPONT");
  }

  @Test
  @DisplayName("Mise à jour du nom d'un utilisateur")
  void shouldUpdateUserLastName() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Jean", "DUPONT");
    userService.createUser(user);
    User savedUser = userRepository.findByEmail("test@example.com");

    User updatedInfo = new User();
    updatedInfo.setLastName("MARTIN");

    //WHEN
    userService.updateUserProfile(savedUser.getId(), updatedInfo);

    //THEN
    User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
    assertThat(updatedUser.getFirstName()).isEqualTo("Jean");
    assertThat(updatedUser.getLastName()).isEqualTo("MARTIN");
  }

  @Test
  @DisplayName("Mise à jour de la date de naissance d'un utilisateur")
  void shouldUpdateUserBirthdate() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Jean", "DUPONT");
    userService.createUser(user);
    User savedUser = userRepository.findByEmail("test@example.com");

    Calendar cal = Calendar.getInstance();
    cal.set(1990, Calendar.MARCH, 15);
    Date newBirthdate = cal.getTime();

    User updatedInfo = new User();
    updatedInfo.setBirthdate(newBirthdate);

    //WHEN
    userService.updateUserProfile(savedUser.getId(), updatedInfo);

    //THEN
    User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
    assertThat(updatedUser.getBirthdate()).isEqualTo(newBirthdate);
  }

  @Test
  @DisplayName("Mise à jour de profil pour utilisateur inexistant")
  void shouldThrowExceptionWhenUpdatingNonExistentUserProfile() {
    //GIVEN
    User updatedInfo = new User();
    updatedInfo.setFirstName("Test");

    //WHEN & THEN
    assertThatThrownBy(() -> userService.updateUserProfile(9999L, updatedInfo))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Utilisateur non trouvé");
  }

  @Test
  @DisplayName("Mise à jour de profil ignore les valeurs null")
  void shouldIgnoreNullValuesWhenUpdatingProfile() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Jean", "DUPONT");
    userService.createUser(user);
    User savedUser = userRepository.findByEmail("test@example.com");

    User updatedInfo = new User();
    updatedInfo.setFirstName(null);
    updatedInfo.setLastName(null);
    updatedInfo.setBirthdate(null);

    //WHEN
    userService.updateUserProfile(savedUser.getId(), updatedInfo);

    //THEN
    User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
    assertThat(updatedUser.getFirstName()).isEqualTo("Jean");
    assertThat(updatedUser.getLastName()).isEqualTo("DUPONT");
  }

  // ==================== Tests de mise à jour de mot de passe ====================

  @Test
  @DisplayName("Mise à jour du mot de passe avec succès")
  void shouldUpdatePasswordSuccessfully() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    String oldPassword = user.getPassword();
    userService.createUser(user);
    User savedUser = userRepository.findByEmail("test@example.com");
    reset(mailSender);

    //WHEN
    userService.updateUserPassword(savedUser.getId(), oldPassword, "NewPassword123!");

    //THEN
    User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
    assertThat(passwordEncoder.matches("NewPassword123!", updatedUser.getPassword())).isTrue();
    assertThat(passwordEncoder.matches(oldPassword, updatedUser.getPassword())).isFalse();
    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("Mise à jour du mot de passe met à jour la date")
  void shouldUpdatePasswordDateOnPasswordChange() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    String oldPassword = user.getPassword();
    userService.createUser(user);
    User savedUser = userRepository.findByEmail("test@example.com");

    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_YEAR, -10);
    savedUser.setPasswordUpdatedAt(cal.getTime());
    userRepository.save(savedUser);

    Date oldPasswordDate = savedUser.getPasswordUpdatedAt();

    //WHEN
    userService.updateUserPassword(savedUser.getId(), oldPassword, "NewPassword123!");

    //THEN
    User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
    assertThat(updatedUser.getPasswordUpdatedAt()).isAfter(oldPasswordDate);
  }

  @Test
  @DisplayName("Mise à jour du mot de passe avec ancien mot de passe incorrect")
  void shouldThrowExceptionWhenOldPasswordIncorrect() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    userService.createUser(user);
    User savedUser = userRepository.findByEmail("test@example.com");

    //WHEN & THEN
    assertThatThrownBy(() -> userService.updateUserPassword(savedUser.getId(), "WrongPassword", "NewPassword123!"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("L'ancien mot de passe est incorrect");

    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("Mise à jour du mot de passe identique à l'ancien")
  void shouldThrowExceptionWhenNewPasswordSameAsOld() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    String password = user.getPassword();
    userService.createUser(user);
    User savedUser = userRepository.findByEmail("test@example.com");

    //WHEN & THEN
    assertThatThrownBy(() -> userService.updateUserPassword(savedUser.getId(), password, password))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Le nouveau mot de passe doit être différent de l'ancien");

    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("Mise à jour du mot de passe présent dans l'historique")
  void shouldThrowExceptionWhenNewPasswordInHistory() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    String password1 = user.getPassword();
    userService.createUser(user);
    User savedUser = userRepository.findByEmail("test@example.com");

    userService.updateUserPassword(savedUser.getId(), password1, "Password2!");
    reset(mailSender);

    //WHEN & THEN
    assertThatThrownBy(() -> userService.updateUserPassword(savedUser.getId(), "Password2!", password1))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Le nouveau mot de passe ne peut pas être l'un de vos 5 derniers mots de passe");

    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  // ==================== Tests de renouvellement de mot de passe ====================

  @Test
  @DisplayName("Renouvellement du mot de passe avec succès")
  void shouldRenewPasswordSuccessfully() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    String oldPassword = user.getPassword();
    userService.createUser(user);
    reset(mailSender);

    //WHEN
    userService.renewPassword("test@example.com", oldPassword, "NewPassword123!");

    //THEN
    User updatedUser = userRepository.findByEmail("test@example.com");
    assertThat(passwordEncoder.matches("NewPassword123!", updatedUser.getPassword())).isTrue();
    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("Renouvellement du mot de passe avec mot de passe trop court")
  void shouldThrowExceptionWhenNewPasswordTooShort() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    String oldPassword = user.getPassword();
    userService.createUser(user);

    //WHEN & THEN
    assertThatThrownBy(() -> userService.renewPassword("test@example.com", oldPassword, "12345"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Le nouveau mot de passe doit contenir au moins 6 caractères");

    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("Renouvellement du mot de passe pour utilisateur inexistant")
  void shouldThrowExceptionWhenRenewingPasswordForNonExistentUser() {
    //WHEN & THEN
    assertThatThrownBy(() -> userService.renewPassword("inexistant@example.com", "oldPass", "newPass123"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Utilisateur non trouvé");
  }

  @Test
  @DisplayName("Renouvellement du mot de passe avec ancien mot de passe incorrect")
  void shouldThrowExceptionWhenRenewingWithIncorrectOldPassword() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    userService.createUser(user);

    //WHEN & THEN
    assertThatThrownBy(() -> userService.renewPassword("test@example.com", "WrongPassword", "NewPassword123!"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("L'ancien mot de passe est incorrect");
  }

  @Test
  @DisplayName("Renouvellement du mot de passe identique à l'ancien")
  void shouldThrowExceptionWhenRenewingWithSamePassword() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    String password = user.getPassword();
    userService.createUser(user);

    //WHEN & THEN
    assertThatThrownBy(() -> userService.renewPassword("test@example.com", password, password))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Le nouveau mot de passe doit être différent de l'ancien");
  }

  @Test
  @DisplayName("Renouvellement du mot de passe présent dans l'historique")
  void shouldThrowExceptionWhenRenewingWithPasswordInHistory() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    String password1 = user.getPassword();
    userService.createUser(user);

    userService.renewPassword("test@example.com", password1, "Password2!");

    //WHEN & THEN
    assertThatThrownBy(() -> userService.renewPassword("test@example.com", "Password2!", password1))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Le nouveau mot de passe ne peut pas être l'un de vos 5 derniers mots de passe");
  }

  // ==================== Tests de vérification d'expiration de mot de passe ====================

  @Test
  @DisplayName("Vérification qu'un mot de passe récent n'est pas expiré")
  void shouldReturnFalseForNonExpiredPassword() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    userService.createUser(user);

    //WHEN
    boolean isExpired = userService.isPasswordExpired("test@example.com");

    //THEN
    assertThat(isExpired).isFalse();
  }

  @Test
  @DisplayName("Vérification qu'un mot de passe expiré est détecté")
  void shouldReturnTrueForExpiredPassword() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    userService.createUser(user);
    User savedUser = userRepository.findByEmail("test@example.com");

    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.WEEK_OF_YEAR, -13);
    savedUser.setPasswordUpdatedAt(cal.getTime());
    userRepository.save(savedUser);

    //WHEN
    boolean isExpired = userService.isPasswordExpired("test@example.com");

    //THEN
    assertThat(isExpired).isTrue();
  }

  @Test
  @DisplayName("Vérification d'expiration pour utilisateur inexistant")
  void shouldThrowExceptionWhenCheckingExpirationForNonExistentUser() {
    //WHEN & THEN
    assertThatThrownBy(() -> userService.isPasswordExpired("inexistant@example.com"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Utilisateur non trouvé");
  }

  @Test
  @DisplayName("Calcul des jours avant expiration")
  void shouldCalculateDaysUntilExpiration() throws Exception {
    //GIVEN
    User user = createValidUser("test@example.com", "Test", "USER");
    userService.createUser(user);

    //WHEN
    long daysRemaining = userService.getDaysUntilPasswordExpiration("test@example.com");

    //THEN
    assertThat(daysRemaining).isGreaterThan(80);
    assertThat(daysRemaining).isLessThanOrEqualTo(84);
  }

  @Test
  @DisplayName("Calcul des jours avant expiration pour utilisateur inexistant")
  void shouldThrowExceptionWhenCalculatingExpirationForNonExistentUser() {
    //WHEN & THEN
    assertThatThrownBy(() -> userService.getDaysUntilPasswordExpiration("inexistant@example.com"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Utilisateur non trouvé");
  }

  // ==================== Méthode utilitaire ====================

  private User createValidUser(String email, String firstName, String lastName) {
    User user = new User();
    user.setEmail(email);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setPassword("Password123!");
    user.setRole("U");
    user.setSecurityQuestion(SecurityQuestion.CHILDHOOD_CITY);
    user.setSecurityAnswerHash("Paris");
    return user;
  }
}
