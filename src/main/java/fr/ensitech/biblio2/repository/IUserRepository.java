package fr.ensitech.biblio2.repository;

import fr.ensitech.biblio2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {

  List<User> findByBirthdateBetween(Date startDate, Date endDate);
  User findByFirstName(String firstName);
  List<User> findByFirstNameAndLastName(String firstName, String lastName);
  List<User> findByBirthdate(Date birthdate);
  User createUser(User user);
  User activeUser(long id);
  User authenticatedUser(String email, String password) throws Exception;
  User deleteUser(long id);

  User findByEmail(String email);
}
