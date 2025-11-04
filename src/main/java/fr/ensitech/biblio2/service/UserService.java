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

  @Override
  public void createUser(User user) throws Exception {
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
}
