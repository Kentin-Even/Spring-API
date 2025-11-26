package fr.ensitech.biblio2.service;

import fr.ensitech.biblio2.dto.AuthenticationResponse;
import fr.ensitech.biblio2.entity.User;
import fr.ensitech.biblio2.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements IUserService {

  @Autowired
  private IUserRepository userRepository;

  @Autowired
  private EmailService emailService;

  @Autowired
  private SecurityAnswerService securityAnswerService;

  @Autowired
  private PasswordRotationService passwordRotationService;

  private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Override
  @Transactional
  public void createUser(User user) throws Exception {
    if (userRepository.findByEmail(user.getEmail()) != null) {
      throw new Exception("User already exists");
    }

    if (user.getSecurityQuestion() == null) {
      throw new Exception("Question de sécurité obligatoire");
    }
    if (user.getSecurityAnswerHash() == null || user.getSecurityAnswerHash().isEmpty()) {
      throw new Exception("Réponse de sécurité obligatoire");
    }
    if (user.getSecurityAnswerHash().length() > 32) {
      throw new Exception("La réponse de sécurité ne peut pas dépasser 32 caractères");
    }

    String hashedSecurityAnswer = securityAnswerService.hashSecurityAnswer(user.getSecurityAnswerHash());
    user.setSecurityAnswerHash(hashedSecurityAnswer);

    String hashedPassword = passwordEncoder.encode(user.getPassword());
    user.setPassword(hashedPassword);

    user.setPasswordUpdatedAt(new Date());

    user.setActive(false);
    User savedUser = userRepository.save(user);

    passwordRotationService.addPasswordToHistory(savedUser, hashedPassword);
  }

  @Override
  public User getUserById(long id) throws Exception {
    Optional<User> optional = userRepository.findById(id);
    return optional.orElse(null);
  }

  @Override
  public List<User> getUsersByBirthdate(Date dateInf, Date dateSup) throws Exception {
    return userRepository.findByBirthdateBetween(dateInf, dateSup);
  }

  @Override
  public User activeUser(long id) throws Exception {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new Exception("User not found"));
    user.setActive(true);
    User savedUser = userRepository.save(user);

    emailService.sendAccountActivatedEmail(
            savedUser.getEmail(),
            savedUser.getFirstName(),
            savedUser.getLastName()
    );

    return savedUser;
  }

  @Override
  public AuthenticationResponse authenticatedUser(String email, String password) throws Exception {
    User user = userRepository.findByEmail(email);

    if (user == null) {
      throw new Exception("Invalid credentials");
    }

    if (!user.isActive()) {
      throw new Exception("Account not activated");
    }

    // Vérification du mot de passe avec BCrypt
    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new Exception("Invalid credentials");
    }

    if (passwordRotationService.isPasswordExpired(user)) {
      long daysRemaining = passwordRotationService.getDaysUntilExpiration(user);
      return new AuthenticationResponse(
              true,
              user.getSecurityQuestion() != null ? user.getSecurityQuestion().getQuestionText() : null,
              "Votre mot de passe a expiré. Vous devez le renouveler pour continuer.",
              user.getId()
      );
    }

    if (user.getSecurityQuestion() != null && user.getSecurityAnswerHash() != null) {
      return new AuthenticationResponse(
              true,
              user.getSecurityQuestion().getQuestionText(),
              "Question de sécurité requise",
              user.getId()
      );
    }

    return new AuthenticationResponse(
            false,
            null,
            "Connexion réussie",
            user.getId()
    );
  }

  @Override
  public boolean verifySecurityAnswer(Long userId, String securityAnswer) throws Exception {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new Exception("Utilisateur non trouvé"));

    if (user.getSecurityAnswerHash() == null) {
      throw new Exception("Aucune question de sécurité configurée");
    }

    return securityAnswerService.verifySecurityAnswer(securityAnswer, user.getSecurityAnswerHash());
  }

  @Override
  public User deleteUser(long id) throws Exception {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new Exception("User not found"));
    user.setActive(false);
    User savedUser = userRepository.save(user);

    emailService.sendUnsubscribeConfirmationEmail(
            savedUser.getEmail(),
            savedUser.getFirstName(),
            savedUser.getLastName()
    );

    return savedUser;
  }

  @Override
  public void sendActivationMail(String email) throws Exception {
    User user = userRepository.findByEmail(email);
    if (user == null) {
      throw new Exception("User not found");
    }

    emailService.sendActivationEmail(email, user.getId());
  }

  @Override
  public void updateUserProfile(long id, User updatedUser) throws Exception {
    User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new Exception("Utilisateur non trouvé"));

    if (updatedUser.getFirstName() != null && !updatedUser.getFirstName().isEmpty()) {
      existingUser.setFirstName(updatedUser.getFirstName());
    }

    if (updatedUser.getLastName() != null && !updatedUser.getLastName().isEmpty()) {
      existingUser.setLastName(updatedUser.getLastName());
    }

    if (updatedUser.getBirthdate() != null) {
      existingUser.setBirthdate(updatedUser.getBirthdate());
    }

    userRepository.save(existingUser);
  }

  @Override
  @Transactional
  public void updateUserPassword(long id, String oldPassword, String newPassword) throws Exception {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new Exception("Utilisateur non trouvé"));

    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
      throw new Exception("L'ancien mot de passe est incorrect");
    }

    if (passwordEncoder.matches(newPassword, user.getPassword())) {
      throw new Exception("Le nouveau mot de passe doit être différent de l'ancien");
    }

    if (passwordRotationService.isPasswordInHistory(user, newPassword)) {
      throw new Exception("Le nouveau mot de passe ne peut pas être l'un de vos 5 derniers mots de passe");
    }

    passwordRotationService.addPasswordToHistory(user, user.getPassword());

    String hashedNewPassword = passwordEncoder.encode(newPassword);
    user.setPassword(hashedNewPassword);
    user.setPasswordUpdatedAt(new Date());
    userRepository.save(user);

    emailService.sendPasswordChangedEmail(user.getEmail(), user.getFirstName(), user.getLastName());
  }

  @Override
  @Transactional
  public void renewPassword(String email, String oldPassword, String newPassword) throws Exception {
    User user = userRepository.findByEmail(email);
    if (user == null) {
      throw new Exception("Utilisateur non trouvé");
    }

    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
      throw new Exception("L'ancien mot de passe est incorrect");
    }

    if (passwordEncoder.matches(newPassword, user.getPassword())) {
      throw new Exception("Le nouveau mot de passe doit être différent de l'ancien");
    }

    if (passwordRotationService.isPasswordInHistory(user, newPassword)) {
      throw new Exception("Le nouveau mot de passe ne peut pas être l'un de vos 5 derniers mots de passe");
    }

    if (newPassword.length() < 6) {
      throw new Exception("Le nouveau mot de passe doit contenir au moins 6 caractères");
    }

    passwordRotationService.addPasswordToHistory(user, user.getPassword());

    String hashedNewPassword = passwordEncoder.encode(newPassword);
    user.setPassword(hashedNewPassword);
    user.setPasswordUpdatedAt(new Date());
    userRepository.save(user);

    emailService.sendPasswordChangedEmail(user.getEmail(), user.getFirstName(), user.getLastName());
  }

  @Override
  public boolean isPasswordExpired(String email) throws Exception {
    User user = userRepository.findByEmail(email);
    if (user == null) {
      throw new Exception("Utilisateur non trouvé");
    }

    return passwordRotationService.isPasswordExpired(user);
  }

  @Override
  public long getDaysUntilPasswordExpiration(String email) throws Exception {
    User user = userRepository.findByEmail(email);
    if (user == null) {
      throw new Exception("Utilisateur non trouvé");
    }

    return passwordRotationService.getDaysUntilExpiration(user);
  }
}