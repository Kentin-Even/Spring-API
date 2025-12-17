package fr.ensitech.biblio2.integration;

import fr.ensitech.biblio2.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class EmailServiceIntegrationTest {

  @Autowired
  private EmailService emailService;

  @MockitoBean
  private JavaMailSender mailSender;

  private ArgumentCaptor<SimpleMailMessage> messageCaptor;

  @BeforeEach
  void setUp() {
    messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
    reset(mailSender);
  }

  @Test
  @DisplayName("Envoi d'un email d'activation de compte")
  void shouldSendActivationEmail() {
    //GIVEN
    String toEmail = "user@example.com";
    long userId = 123L;

    //WHEN
    emailService.sendActivationEmail(toEmail, userId);

    //THEN
    verify(mailSender, times(1)).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertThat(sentMessage.getFrom()).isEqualTo("dev.kentin@gmail.com");
    assertThat(sentMessage.getTo()).containsExactly(toEmail);
    assertThat(sentMessage.getSubject()).isEqualTo("Activation de votre compte - Biblio");
    assertThat(sentMessage.getText()).contains("Pour activer votre compte");
    assertThat(sentMessage.getText()).contains("http://localhost:8080/api/users/activate/" + userId);
    assertThat(sentMessage.getText()).contains("L'équipe Biblio");
  }

  @Test
  @DisplayName("Email d'activation contient le bon userId dans le lien")
  void shouldIncludeCorrectUserIdInActivationLink() {
    //GIVEN
    String toEmail = "newuser@example.com";
    long userId = 456L;

    //WHEN
    emailService.sendActivationEmail(toEmail, userId);

    //THEN
    verify(mailSender, times(1)).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertThat(sentMessage.getText()).contains("http://localhost:8080/api/users/activate/456");
  }

  @Test
  @DisplayName("Envoi d'un email de confirmation d'activation")
  void shouldSendAccountActivatedEmail() {
    //GIVEN
    String toEmail = "user@example.com";
    String firstName = "Jean";
    String lastName = "DUPONT";

    //WHEN
    emailService.sendAccountActivatedEmail(toEmail, firstName, lastName);

    //THEN
    verify(mailSender, times(1)).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertThat(sentMessage.getFrom()).isEqualTo("dev.kentin@gmail.com");
    assertThat(sentMessage.getTo()).containsExactly(toEmail);
    assertThat(sentMessage.getSubject()).isEqualTo("Votre compte est maintenant actif - Biblio");
    assertThat(sentMessage.getText()).contains("Bonjour " + firstName + " " + lastName);
    assertThat(sentMessage.getText()).contains("Votre compte a été activé avec succès");
    assertThat(sentMessage.getText()).contains("http://localhost:8080/api/users/login");
  }

  @Test
  @DisplayName("Email de confirmation d'activation personnalisé avec le nom complet")
  void shouldPersonalizeAccountActivatedEmailWithFullName() {
    //GIVEN
    String toEmail = "marie@example.com";
    String firstName = "Marie";
    String lastName = "MARTIN";

    //WHEN
    emailService.sendAccountActivatedEmail(toEmail, firstName, lastName);

    //THEN
    verify(mailSender, times(1)).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertThat(sentMessage.getText()).startsWith("Bonjour Marie MARTIN");
  }

  @Test
  @DisplayName("Envoi d'un email de confirmation de désinscription")
  void shouldSendUnsubscribeConfirmationEmail() {
    //GIVEN
    String toEmail = "user@example.com";
    String firstName = "Pascal";
    String lastName = "LAMBERT";

    //WHEN
    emailService.sendUnsubscribeConfirmationEmail(toEmail, firstName, lastName);

    //THEN
    verify(mailSender, times(1)).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertThat(sentMessage.getFrom()).isEqualTo("dev.kentin@gmail.com");
    assertThat(sentMessage.getTo()).containsExactly(toEmail);
    assertThat(sentMessage.getSubject()).isEqualTo("Confirmation de désinscription - Biblio");
    assertThat(sentMessage.getText()).contains("Bonjour " + firstName + " " + lastName);
    assertThat(sentMessage.getText()).contains("demande de désinscription");
    assertThat(sentMessage.getText()).contains("Votre compte a été désactivé");
  }

  @Test
  @DisplayName("Email de désinscription mentionne la conservation des données")
  void shouldMentionDataRetentionInUnsubscribeEmail() {
    //GIVEN
    String toEmail = "user@example.com";
    String firstName = "Benoit";
    String lastName = "DECOUX";

    //WHEN
    emailService.sendUnsubscribeConfirmationEmail(toEmail, firstName, lastName);

    //THEN
    verify(mailSender, times(1)).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertThat(sentMessage.getText()).contains("Vos données sont conservées");
    assertThat(sentMessage.getText()).contains("réactiver votre compte");
  }

  @Test
  @DisplayName("Envoi d'un email de changement de mot de passe")
  void shouldSendPasswordChangedEmail() {
    //GIVEN
    String toEmail = "user@example.com";
    String firstName = "Sophie";
    String lastName = "DURAND";

    //WHEN
    emailService.sendPasswordChangedEmail(toEmail, firstName, lastName);

    //THEN
    verify(mailSender, times(1)).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertThat(sentMessage.getFrom()).isEqualTo("dev.kentin@gmail.com");
    assertThat(sentMessage.getTo()).containsExactly(toEmail);
    assertThat(sentMessage.getSubject()).isEqualTo("Modification de votre mot de passe - Biblio");
    assertThat(sentMessage.getText()).contains("Bonjour " + firstName + " " + lastName);
    assertThat(sentMessage.getText()).contains("Votre mot de passe a été modifié avec succès");
  }

  @Test
  @DisplayName("Email de changement de mot de passe contient un avertissement de sécurité")
  void shouldIncludeSecurityWarningInPasswordChangedEmail() {
    //GIVEN
    String toEmail = "user@example.com";
    String firstName = "Lucas";
    String lastName = "BERNARD";

    //WHEN
    emailService.sendPasswordChangedEmail(toEmail, firstName, lastName);

    //THEN
    verify(mailSender, times(1)).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertThat(sentMessage.getText()).contains("Si vous n'êtes pas à l'origine de cette modification");
    assertThat(sentMessage.getText()).contains("contacter immédiatement notre support");
  }

  @Test
  @DisplayName("Envoi d'un email de confirmation de réservation")
  void shouldSendReservationConfirmationEmail() {
    //GIVEN
    String toEmail = "user@example.com";
    String firstName = "Emma";
    String lastName = "PETIT";
    String bookTitle = "Livre de Java";

    //WHEN
    emailService.sendReservationConfirmationEmail(toEmail, firstName, lastName, bookTitle);

    //THEN
    verify(mailSender, times(1)).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertThat(sentMessage.getFrom()).isEqualTo("dev.kentin@gmail.com");
    assertThat(sentMessage.getTo()).containsExactly(toEmail);
    assertThat(sentMessage.getSubject()).isEqualTo("Confirmation de réservation - Biblio");
    assertThat(sentMessage.getText()).contains("Bonjour " + firstName + " " + lastName);
    assertThat(sentMessage.getText()).contains("Votre réservation a été effectuée avec succès");
    assertThat(sentMessage.getText()).contains("Livre : " + bookTitle);
  }

  @Test
  @DisplayName("Email de réservation contient le titre du livre réservé")
  void shouldIncludeBookTitleInReservationEmail() {
    //GIVEN
    String toEmail = "reader@example.com";
    String firstName = "Thomas";
    String lastName = "MOREAU";
    String bookTitle = "Java Avancé - Techniques et Patterns";

    //WHEN
    emailService.sendReservationConfirmationEmail(toEmail, firstName, lastName, bookTitle);

    //THEN
    verify(mailSender, times(1)).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertThat(sentMessage.getText()).contains("Livre : Java Avancé - Techniques et Patterns");
  }

  @Test
  @DisplayName("Email de réservation mentionne l'espace personnel")
  void shouldMentionPersonalSpaceInReservationEmail() {
    //GIVEN
    String toEmail = "user@example.com";
    String firstName = "Alice";
    String lastName = "ROBERT";
    String bookTitle = "Spring Boot en Action";

    //WHEN
    emailService.sendReservationConfirmationEmail(toEmail, firstName, lastName, bookTitle);

    //THEN
    verify(mailSender, times(1)).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertThat(sentMessage.getText()).contains("espace personnel");
    assertThat(sentMessage.getText()).contains("réservations actives");
  }

  @Test
  @DisplayName("Tous les emails ont le même expéditeur")
  void shouldUseSameSenderForAllEmails() {
    //GIVEN
    String expectedSender = "dev.kentin@gmail.com";

    //WHEN
    emailService.sendActivationEmail("user1@example.com", 1L);
    emailService.sendAccountActivatedEmail("user2@example.com", "John", "DOE");
    emailService.sendUnsubscribeConfirmationEmail("user3@example.com", "Jane", "DOE");
    emailService.sendPasswordChangedEmail("user4@example.com", "Bob", "SMITH");
    emailService.sendReservationConfirmationEmail("user5@example.com", "Alice", "JONES", "Test Book");

    //THEN
    verify(mailSender, times(5)).send(messageCaptor.capture());

    for (SimpleMailMessage message : messageCaptor.getAllValues()) {
      assertThat(message.getFrom()).isEqualTo(expectedSender);
    }
  }

  @Test
  @DisplayName("Tous les emails contiennent la signature de l'équipe")
  void shouldIncludeTeamSignatureInAllEmails() {
    //WHEN
    emailService.sendActivationEmail("user1@example.com", 1L);
    emailService.sendAccountActivatedEmail("user2@example.com", "John", "DOE");
    emailService.sendUnsubscribeConfirmationEmail("user3@example.com", "Jane", "DOE");
    emailService.sendPasswordChangedEmail("user4@example.com", "Bob", "SMITH");
    emailService.sendReservationConfirmationEmail("user5@example.com", "Alice", "JONES", "Test Book");

    //THEN
    verify(mailSender, times(5)).send(messageCaptor.capture());

    for (SimpleMailMessage message : messageCaptor.getAllValues()) {
      assertThat(message.getText()).contains("L'équipe Biblio");
    }
  }

  @Test
  @DisplayName("Gestion d'un email avec caractères spéciaux dans le nom")
  void shouldHandleSpecialCharactersInNames() {
    //GIVEN
    String toEmail = "user@example.com";
    String firstName = "François";
    String lastName = "D'AMBOISE";

    //WHEN
    emailService.sendAccountActivatedEmail(toEmail, firstName, lastName);

    //THEN
    verify(mailSender, times(1)).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertThat(sentMessage.getText()).contains("Bonjour François D'AMBOISE");
  }

  @Test
  @DisplayName("Gestion d'un titre de livre avec caractères spéciaux")
  void shouldHandleSpecialCharactersInBookTitle() {
    //GIVEN
    String toEmail = "user@example.com";
    String firstName = "Pierre";
    String lastName = "MARTIN";
    String bookTitle = "L'Art de la Programmation: Guide & Techniques";

    //WHEN
    emailService.sendReservationConfirmationEmail(toEmail, firstName, lastName, bookTitle);

    //THEN
    verify(mailSender, times(1)).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertThat(sentMessage.getText()).contains("Livre : L'Art de la Programmation: Guide & Techniques");
  }

  @Test
  @DisplayName("Vérification de l'envoi effectif au JavaMailSender")
  void shouldActuallySendEmailThroughMailSender() {
    //GIVEN
    String toEmail = "test@example.com";
    long userId = 999L;

    //WHEN
    emailService.sendActivationEmail(toEmail, userId);

    //THEN
    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    verifyNoMoreInteractions(mailSender);
  }

  @Test
  @DisplayName("Envoi de plusieurs emails successifs")
  void shouldSendMultipleEmailsSuccessively() {
    //GIVEN
    String email1 = "user1@example.com";
    String email2 = "user2@example.com";

    //WHEN
    emailService.sendActivationEmail(email1, 1L);
    emailService.sendActivationEmail(email2, 2L);

    //THEN
    verify(mailSender, times(2)).send(messageCaptor.capture());

    assertThat(messageCaptor.getAllValues()).hasSize(2);
    assertThat(messageCaptor.getAllValues().get(0).getTo()).containsExactly(email1);
    assertThat(messageCaptor.getAllValues().get(1).getTo()).containsExactly(email2);
  }
}