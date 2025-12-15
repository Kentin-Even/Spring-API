package fr.ensitech.biblio2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", catalog = "biblio-database")
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name="firstName", nullable=false, length=48)
  private String firstName;

  @Column(name="lastName", nullable=false, length=48)
  private String lastName;

  @Column(name = "email", nullable=false, length=48, unique=true)
  private String email;

  @Column(name = "password", nullable=false, length=128)
  private String password;

  @Column(name = "role", nullable=false, length=1)
  private String role;

  @Column(name = "birthdate", nullable=true)
  @Temporal(TemporalType.DATE)
  private Date birthdate;

  @Column(name = "active", nullable=false)
  private boolean active;

  @Enumerated(EnumType.STRING)
  @Column(name = "security_question", nullable=true, length=50)
  private SecurityQuestion securityQuestion;

  @Column(name = "security_answer_hash", nullable=true, length=128)
  private String securityAnswerHash;

  @Column(name = "password_updated_at", nullable=true)
  @Temporal(TemporalType.TIMESTAMP)
  private Date passwordUpdatedAt;

  @Column(name = "pending_2fa", nullable=false)
  private boolean pending2fa = false;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Reservation> reservations = new HashSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<PasswordHistory> passwordHistories = new HashSet<>();
}