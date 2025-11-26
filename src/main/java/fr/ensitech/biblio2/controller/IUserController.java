package fr.ensitech.biblio2.controller;

import fr.ensitech.biblio2.dto.PasswordRenewalRequest;
import fr.ensitech.biblio2.dto.SecurityAnswerVerificationRequest;
import fr.ensitech.biblio2.dto.UserRegistrationRequest;
import fr.ensitech.biblio2.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface IUserController {
  ResponseEntity<?> createUser(@RequestBody UserRegistrationRequest request);
  ResponseEntity<String> activeUser(@PathVariable long id);
  ResponseEntity<?> authenticatedUser(@RequestParam String email,
                                      @RequestParam String password);
  ResponseEntity<String> verifySecurityAnswer(@RequestBody SecurityAnswerVerificationRequest request);
  ResponseEntity<String> deleteUser(@RequestBody User user);
  ResponseEntity<String> sendActivationMail(@RequestParam String email);
  ResponseEntity<String> updateUserProfile(@PathVariable long id, @RequestBody User user);
  ResponseEntity<String> updateUserPassword(@PathVariable long id,
                                            @PathVariable String oldPwd,
                                            @PathVariable String newPwd);
  ResponseEntity<String> renewPassword(@PathVariable String email,
                                       @RequestBody PasswordRenewalRequest request);
  ResponseEntity<String> checkPasswordStatus(@PathVariable String email);
}