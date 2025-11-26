package fr.ensitech.biblio2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SecurityAnswerVerificationRequest {
  private Long userId;
  private String securityAnswer;
}