package fr.ensitech.biblio2.controller;

import fr.ensitech.biblio2.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

public interface IUserController {
  ResponseEntity<User> createUser(User user);
  ResponseEntity<String> activeUser(@PathVariable long id);
  ResponseEntity<String> authenticatedUser(@RequestParam String email,
                                           @RequestParam String password);
  ResponseEntity<String> deleteUser(User user); // Changé en String
  ResponseEntity<User> sendActivationMail(@RequestParam String email);
}