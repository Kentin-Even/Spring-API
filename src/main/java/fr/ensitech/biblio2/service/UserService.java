package fr.ensitech.biblio2.service;

import fr.ensitech.biblio2.entity.User;
import fr.ensitech.biblio2.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements IUserService {

  @Autowired
  private IUserRepository userRepository;

  @Autowired
  private EmailService emailService;

  private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Override
  public void createUser(User user) throws Exception {
    if (userRepository.findByEmail(user.getEmail()) != null) {
      throw new Exception("User already exists");
    }

    // Hachage du mot de passe avec BCrypt
    String hashedPassword = passwordEncoder.encode(user.getPassword());
    user.setPassword(hashedPassword);

    user.setActive(false);
    userRepository.save(user);
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
  public User authenticatedUser(String email, String password) throws Exception {
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

    return user;
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
  public void updateUserPassword(long id, String oldPassword, String newPassword) throws Exception {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new Exception("Utilisateur non trouvé"));

    // Vérification de l'ancien mot de passe avec BCrypt
    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
      throw new Exception("L'ancien mot de passe est incorrect");
    }

    // Vérification que le nouveau mot de passe est différent de l'ancien
    if (passwordEncoder.matches(newPassword, user.getPassword())) {
      throw new Exception("Le nouveau mot de passe doit être différent de l'ancien");
    }

    // Hachage et mise à jour du nouveau mot de passe
    String hashedNewPassword = passwordEncoder.encode(newPassword);
    user.setPassword(hashedNewPassword);
    userRepository.save(user);

    emailService.sendPasswordChangedEmail(user.getEmail(), user.getFirstName(), user.getLastName());
  }
}