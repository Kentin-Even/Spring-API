package fr.ensitech.biblio2.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class SecurityAnswerService {

  /**
   * Hash une réponse de sécurité avec SHA-256
   * @param answer la réponse en clair
   * @return la réponse hashée en Base64
   */
  public String hashSecurityAnswer(String answer) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(answer.toLowerCase().trim().getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Erreur lors du hashage de la réponse de sécurité", e);
    }
  }

  /**
   * Vérifie si une réponse correspond au hash stocké
   * @param plainAnswer la réponse fournie par l'utilisateur
   * @param hashedAnswer le hash stocké en base
   * @return true si la réponse correspond
   */
  public boolean verifySecurityAnswer(String plainAnswer, String hashedAnswer) {
    String hashedInput = hashSecurityAnswer(plainAnswer);
    return hashedInput.equals(hashedAnswer);
  }
}