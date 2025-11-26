package fr.ensitech.biblio2.dto;

import fr.ensitech.biblio2.entity.SecurityQuestion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserRegistrationRequest {
  private String firstName;
  private String lastName;
  private String email;
  private String password;
  private Date birthdate;
  private SecurityQuestion securityQuestion;
  private String securityAnswer;
}