package fr.ensitech.biblio2.service;

import fr.ensitech.biblio2.entity.Book;
import fr.ensitech.biblio2.entity.Reservation;
import fr.ensitech.biblio2.entity.User;
import fr.ensitech.biblio2.repository.IBookRepository;
import fr.ensitech.biblio2.repository.IReservationRepository;
import fr.ensitech.biblio2.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ReservationService implements IReservationService {

  @Autowired
  private IReservationRepository reservationRepository;

  @Autowired
  private IBookRepository bookRepository;

  @Autowired
  private IUserRepository userRepository;

  @Autowired
  private EmailService emailService;

  @Override
  @Transactional
  public Reservation reserveBook(long bookId, String email) throws Exception {
    User user = userRepository.findByEmail(email);
    if (user == null) {
      throw new Exception("Utilisateur non trouvé");
    }
    if (!user.isActive()) {
      throw new Exception("Compte utilisateur non activé");
    }

    Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new Exception("Livre non trouvé"));

    long existingReservation = reservationRepository.countActiveReservationsByUserAndBook(user.getId(), bookId);
    if (existingReservation > 0) {
      throw new Exception("Vous avez déjà réservé ce livre");
    }

    long userActiveReservations = reservationRepository.countActiveReservationsByUserId(user.getId());
    if (userActiveReservations >= 3) {
      throw new Exception("Vous avez atteint la limite de 3 réservations actives");
    }

    int availableStock = getAvailableStock(bookId);
    if (availableStock <= 0) {
      throw new Exception("Ce livre n'est plus disponible");
    }

    Reservation reservation = new Reservation();
    reservation.setUser(user);
    reservation.setBook(book);
    reservation.setReservationDate(new Date());
    reservation.setStatus("ACTIVE");

    Reservation savedReservation = reservationRepository.save(reservation);

    emailService.sendReservationConfirmationEmail(
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            book.getTitle()
    );

    return savedReservation;
  }

  @Override
  public List<Reservation> getUserReservations(String email) throws Exception {
    User user = userRepository.findByEmail(email);
    if (user == null) {
      throw new Exception("Utilisateur non trouvé");
    }
    return reservationRepository.findByUserAndStatus(user, "ACTIVE");
  }

  @Override
  @Transactional
  public void cancelReservation(long reservationId) throws Exception {
    Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new Exception("Réservation non trouvée"));

    reservation.setStatus("CANCELLED");
    reservationRepository.save(reservation);
  }

  @Override
  public int getAvailableStock(long bookId) throws Exception {
    Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new Exception("Livre non trouvé"));

    int totalStock = book.getStock();

    List<Reservation> activeReservations = reservationRepository.findByBookAndStatus(book, "ACTIVE");
    int reservedCount = activeReservations.size();

    return totalStock - reservedCount;
  }
}