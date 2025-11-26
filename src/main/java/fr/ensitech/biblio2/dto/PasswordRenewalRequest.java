package fr.ensitech.biblio2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PasswordRenewalRequest {
  private String oldPassword;
  private String newPassword;
}