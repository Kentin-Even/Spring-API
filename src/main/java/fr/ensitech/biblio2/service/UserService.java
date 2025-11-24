package fr.ensitech.biblio2.service;

import fr.ensitech.biblio2.entity.User;
import fr.ensitech.biblio2.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Override
  public void createUser(User user) throws Exception {
    if (userRepository.findByEmail(user.getEmail()) != null) {
      throw new Exception("User already exists");
    }
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
    if (user != null && user.isActive() && password.equals(user.getPassword())) {
      return user;
    }
    throw new Exception("Invalid credentials or account not activated");
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

    // Envoi de l'email d'activation
    emailService.sendActivationEmail(email, user.getId());
  }
}