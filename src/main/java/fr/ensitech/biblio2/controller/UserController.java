package fr.ensitech.biblio2.controller;

import fr.ensitech.biblio2.entity.User;
import fr.ensitech.biblio2.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/api/users")
public class UserController implements IUserController {

  @Autowired
  private IUserService userService;

  @PostMapping("/register")
  @Override
  public ResponseEntity<User> createUser(@RequestBody User user) {
    if (user == null
            || user.getFirstName() == null || user.getFirstName().isEmpty()
            || user.getLastName() == null || user.getLastName().isEmpty()
            || user.getEmail() == null || user.getEmail().isEmpty()
            || user.getPassword() == null || user.getPassword().isEmpty()) {

      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    try {
      userService.createUser(user);
      userService.sendActivationMail(user.getEmail());
      return new ResponseEntity<>(user, HttpStatus.CREATED);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/send-activation")
  @Override
  public ResponseEntity<User> sendActivationMail(@RequestParam String email) {
    try {
      userService.sendActivationMail(email);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/activate/{id}") // Changé de POST à GET
  @Override
  public ResponseEntity<String> activeUser(@PathVariable long id) {
    try {
      User activatedUser = userService.activeUser(id);
      return new ResponseEntity<>("Votre compte a été activé avec succès ! Vous pouvez maintenant vous connecter.", HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>("Erreur lors de l'activation du compte. Lien invalide ou compte déjà activé.", HttpStatus.NOT_FOUND);
    }
  }

  @PostMapping("/login")
  @Override
  public ResponseEntity<String> authenticatedUser(@RequestParam String email,
                                                  @RequestParam String password) {
    if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
      return new ResponseEntity<>("Email et mot de passe requis", HttpStatus.BAD_REQUEST);
    }
    try {
      User user = userService.authenticatedUser(email, password);
      return new ResponseEntity<>("Connexion réussie", HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>("Identifiants invalides ou compte non activé", HttpStatus.UNAUTHORIZED);
    }
  }

  @PutMapping("/unsubscribe")
  @Override
  public ResponseEntity<String> deleteUser(@RequestBody User user) {
    try {
      User deletedUser = userService.deleteUser(user.getId());
      return new ResponseEntity<>("Votre compte a été désactivé avec succès. Un email de confirmation vous a été envoyé.", HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>("Erreur lors de la désinscription. Veuillez réessayer.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}