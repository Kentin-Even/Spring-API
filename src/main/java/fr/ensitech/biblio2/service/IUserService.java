package fr.ensitech.biblio2.service;

import fr.ensitech.biblio2.entity.User;

import java.util.Date;
import java.util.List;

public interface IUserService {

  void createUser(User user) throws Exception;
  User getUserById(long id) throws Exception;
  List<User> getUsersByBirthdate(Date dateInf, Date dateSup) throws Exception;
  User activeUser(long id) throws Exception;
  User authenticatedUser(String email, String password) throws Exception;
  User deleteUser(long id) throws Exception;
  void sendActivationMail(String email) throws Exception; // Changé en void
}