package fr.ensitech.biblio2.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class SecurityAnswerService {

  public String hashSecurityAnswer(String answer) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(answer.toLowerCase().trim().getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Erreur lors du hashage de la réponse de sécurité", e);
    }
  }

  public boolean verifySecurityAnswer(String plainAnswer, String hashedAnswer) {
    String hashedInput = hashSecurityAnswer(plainAnswer);
    return hashedInput.equals(hashedAnswer);
  }
}