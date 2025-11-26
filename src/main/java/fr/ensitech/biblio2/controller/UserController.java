package fr.ensitech.biblio2.controller;

import fr.ensitech.biblio2.dto.AuthenticationResponse;
import fr.ensitech.biblio2.dto.PasswordRenewalRequest;
import fr.ensitech.biblio2.dto.SecurityAnswerVerificationRequest;
import fr.ensitech.biblio2.dto.UserRegistrationRequest;
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
  public ResponseEntity<?> createUser(@RequestBody UserRegistrationRequest request) {
    if (request == null
            || request.getFirstName() == null || request.getFirstName().isEmpty()
            || request.getLastName() == null || request.getLastName().isEmpty()
            || request.getEmail() == null || request.getEmail().isEmpty()
            || request.getPassword() == null || request.getPassword().isEmpty()
            || request.getSecurityQuestion() == null
            || request.getSecurityAnswer() == null || request.getSecurityAnswer().isEmpty()) {

      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body("{\"message\": \"Tous les champs sont obligatoires, y compris la question et réponse de sécurité\"}");
    }

    if (request.getSecurityAnswer().length() > 32) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body("{\"message\": \"La réponse de sécurité ne peut pas dépasser 32 caractères\"}");
    }

    try {
      User user = new User();
      user.setFirstName(request.getFirstName());
      user.setLastName(request.getLastName());
      user.setEmail(request.getEmail());
      user.setPassword(request.getPassword());
      user.setBirthdate(request.getBirthdate());
      user.setSecurityQuestion(request.getSecurityQuestion());
      user.setSecurityAnswerHash(request.getSecurityAnswer());
      user.setRole("U");

      userService.createUser(user);
      userService.sendActivationMail(user.getEmail());

      return ResponseEntity.status(HttpStatus.CREATED)
              .body("{\"message\": \"Utilisateur créé avec succès. Un email d'activation a été envoyé.\"}");
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body("{\"message\": \"" + e.getMessage() + "\"}");
    }
  }

  @PostMapping("/send-activation")
  @Override
  public ResponseEntity<String> sendActivationMail(@RequestParam String email) {
    try {
      userService.sendActivationMail(email);
      return ResponseEntity.ok("{\"message\": \"Email d'activation envoyé\"}");
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body("{\"message\": \"Erreur lors de l'envoi de l'email\"}");
    }
  }

  @GetMapping("/activate/{id}")
  @Override
  public ResponseEntity<String> activeUser(@PathVariable long id) {
    try {
      User activatedUser = userService.activeUser(id);
      return ResponseEntity.ok("Votre compte a été activé avec succès ! Vous pouvez maintenant vous connecter.");
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body("Erreur lors de l'activation du compte. Lien invalide ou compte déjà activé.");
    }
  }

  @PostMapping("/login")
  @Override
  public ResponseEntity<?> authenticatedUser(@RequestParam String email,
                                             @RequestParam String password) {
    if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body("{\"message\": \"Email et mot de passe requis\"}");
    }

    try {
      AuthenticationResponse response = userService.authenticatedUser(email, password);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body("{\"message\": \"Identifiants invalides ou compte non activé\"}");
    }
  }

  @PostMapping("/verify-security-answer")
  @Override
  public ResponseEntity<String> verifySecurityAnswer(@RequestBody SecurityAnswerVerificationRequest request) {
    if (request == null || request.getUserId() == null ||
            request.getSecurityAnswer() == null || request.getSecurityAnswer().isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body("{\"message\": \"ID utilisateur et réponse de sécurité requis\"}");
    }

    try {
      boolean isValid = userService.verifySecurityAnswer(request.getUserId(), request.getSecurityAnswer());

      if (isValid) {
        return ResponseEntity.ok("{\"message\": \"Authentification réussie\", \"authenticated\": true}");
      } else {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"message\": \"Réponse de sécurité incorrecte\", \"authenticated\": false}");
      }
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body("{\"message\": \"Erreur lors de la vérification: " + e.getMessage() + "\"}");
    }
  }

  @PutMapping("/unsubscribe")
  @Override
  public ResponseEntity<String> deleteUser(@RequestBody User user) {
    try {
      User deletedUser = userService.deleteUser(user.getId());
      return ResponseEntity.ok("{\"message\": \"Votre compte a été désactivé avec succès. Un email de confirmation vous a été envoyé.\"}");
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body("{\"message\": \"Erreur lors de la désinscription. Veuillez réessayer.\"}");
    }
  }

  @PutMapping("/{id}/profile")
  @Override
  public ResponseEntity<String> updateUserProfile(@PathVariable long id, @RequestBody User user) {
    if (user == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body("{\"message\": \"Les données de l'utilisateur sont requises\"}");
    }

    try {
      userService.updateUserProfile(id, user);
      return ResponseEntity.ok("{\"message\": \"Profil mis à jour avec succès\"}");
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body("{\"message\": \"Erreur lors de la mise à jour du profil: " + e.getMessage() + "\"}");
    }
  }

  @PutMapping("/{id}/{oldPwd}/{newPwd}")
  @Override
  public ResponseEntity<String> updateUserPassword(@PathVariable long id,
                                                   @PathVariable String oldPwd,
                                                   @PathVariable String newPwd) {
    if (oldPwd == null || oldPwd.isEmpty() || newPwd == null || newPwd.isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body("{\"message\": \"L'ancien et le nouveau mot de passe sont requis\"}");
    }

    if (newPwd.length() < 6) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body("{\"message\": \"Le nouveau mot de passe doit contenir au moins 6 caractères\"}");
    }

    try {
      userService.updateUserPassword(id, oldPwd, newPwd);
      return ResponseEntity.ok("{\"message\": \"Mot de passe mis à jour avec succès\"}");
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body("{\"message\": \"" + e.getMessage() + "\"}");
    }
  }

  @PutMapping("/{email}/password/renew")
  @Override
  public ResponseEntity<String> renewPassword(@PathVariable String email,
                                              @RequestBody PasswordRenewalRequest request) {
    if (request == null ||
            request.getOldPassword() == null || request.getOldPassword().isEmpty() ||
            request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body("{\"message\": \"L'ancien et le nouveau mot de passe sont requis\"}");
    }

    if (request.getNewPassword().length() < 6) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body("{\"message\": \"Le nouveau mot de passe doit contenir au moins 6 caractères\"}");
    }

    try {
      userService.renewPassword(email, request.getOldPassword(), request.getNewPassword());
      return ResponseEntity.ok("{\"message\": \"Mot de passe renouvelé avec succès\"}");
    } catch (Exception e) {
      e.printStackTrace();

      if (e.getMessage().contains("5 derniers")) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"message\": \"" + e.getMessage() + "\"}");
      } else if (e.getMessage().contains("incorrect")) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"message\": \"" + e.getMessage() + "\"}");
      } else {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\": \"Erreur lors du renouvellement: " + e.getMessage() + "\"}");
      }
    }
  }

  @GetMapping("/{email}/password/status")
  @Override
  public ResponseEntity<String> checkPasswordStatus(@PathVariable String email) {
    try {
      boolean isExpired = userService.isPasswordExpired(email);
      long daysRemaining = userService.getDaysUntilPasswordExpiration(email);

      if (isExpired) {
        return ResponseEntity.ok("{\"expired\": true, \"daysRemaining\": 0, \"message\": \"Votre mot de passe a expiré. Veuillez le renouveler.\"}");
      } else {
        return ResponseEntity.ok("{\"expired\": false, \"daysRemaining\": " + daysRemaining + ", \"message\": \"Votre mot de passe est valide.\"}");
      }
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body("{\"message\": \"Erreur lors de la vérification: " + e.getMessage() + "\"}");
    }
  }
}