package fr.ensitech.biblio2.controller;

import fr.ensitech.biblio2.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface IUserController {
  ResponseEntity<User> createUser(User user);
  ResponseEntity<User> activeUser(@PathVariable long id);
  ResponseEntity<User> authenticatedUser(@RequestParam String email,
                                         @RequestParam String password);
  ResponseEntity<User> deleteUser(User user);
  ResponseEntity<User> sendActivationMail(@RequestParam String email);
}
