package fr.ensitech.biblio2.service;

import fr.ensitech.biblio2.dto.AuthenticationResponse;
import fr.ensitech.biblio2.entity.User;

import java.util.Date;
import java.util.List;

public interface IUserService {

  void createUser(User user) throws Exception;
  User getUserById(long id) throws Exception;
  List<User> getUsersByBirthdate(Date dateInf, Date dateSup) throws Exception;
  User activeUser(long id) throws Exception;
  AuthenticationResponse authenticatedUser(String email, String password) throws Exception;
  boolean verifySecurityAnswer(Long userId, String securityAnswer) throws Exception;
  User deleteUser(long id) throws Exception;
  void sendActivationMail(String email) throws Exception;
  void updateUserProfile(long id, User user) throws Exception;
  void updateUserPassword(long id, String oldPassword, String newPassword) throws Exception;
  void renewPassword(String email, String oldPassword, String newPassword) throws Exception;
  boolean isPasswordExpired(String email) throws Exception;
  long getDaysUntilPasswordExpiration(String email) throws Exception;
}