package fr.ensitech.biblio2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  @Autowired
  private JavaMailSender mailSender;

  public void sendActivationEmail(String toEmail, long userId) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("dev.kentin@gmail.com");
    message.setTo(toEmail);
    message.setSubject("Activation de votre compte - Biblio");
    message.setText("Bonjour,\n\n" +
            "Merci de vous être inscrit sur notre plateforme.\n\n" +
            "Pour activer votre compte, cliquez sur le lien suivant :\n" +
            "http://localhost:8080/api/users/activate/" + userId + "\n\n" +
            "Cordialement,\n" +
            "L'équipe Biblio");

    mailSender.send(message);
  }

  public void sendAccountActivatedEmail(String toEmail, String firstName, String lastName) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("dev.kentin@gmail.com");
    message.setTo(toEmail);
    message.setSubject("Votre compte est maintenant actif - Biblio");
    message.setText("Bonjour " + firstName + " " + lastName + ",\n\n" +
            "Bonne nouvelle ! Votre compte a été activé avec succès.\n\n" +
            "Vous pouvez désormais vous connecter et profiter de tous nos services :\n" +
            "http://localhost:8080/api/users/login\n\n" +
            "Nous sommes ravis de vous compter parmi nous !\n\n" +
            "Cordialement,\n" +
            "L'équipe Biblio");

    mailSender.send(message);
  }

  public void sendUnsubscribeConfirmationEmail(String toEmail, String firstName, String lastName) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("dev.kentin@gmail.com");
    message.setTo(toEmail);
    message.setSubject("Confirmation de désinscription - Biblio");
    message.setText("Bonjour " + firstName + " " + lastName + ",\n\n" +
            "Nous avons bien pris en compte votre demande de désinscription.\n\n" +
            "Votre compte a été désactivé. Vos données sont conservées et vous pouvez réactiver " +
            "votre compte à tout moment en nous contactant.\n\n" +
            "Nous sommes désolés de vous voir partir et espérons vous revoir bientôt !\n\n" +
            "Merci d'avoir utilisé nos services.\n\n" +
            "Cordialement,\n" +
            "L'équipe Biblio");

    mailSender.send(message);
  }
}