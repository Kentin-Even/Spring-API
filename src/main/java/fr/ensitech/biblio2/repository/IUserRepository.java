package fr.ensitech.biblio2.repository;

import fr.ensitech.biblio2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {

  List<User> findByBirthdateBetween(Date startDate, Date endDate);
  User findByFirstName(String firstName);
  List<User> findByFirstNameAndLastName(String firstName, String lastName);
  List<User> findByBirthdate(Date birthdate);

}
