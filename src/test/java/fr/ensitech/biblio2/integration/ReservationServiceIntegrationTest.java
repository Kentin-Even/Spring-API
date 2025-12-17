package fr.ensitech.biblio2.integration;

import fr.ensitech.biblio2.entity.Author;
import fr.ensitech.biblio2.entity.Book;
import fr.ensitech.biblio2.entity.Reservation;
import fr.ensitech.biblio2.entity.User;
import fr.ensitech.biblio2.repository.IBookRepository;
import fr.ensitech.biblio2.repository.IReservationRepository;
import fr.ensitech.biblio2.repository.IUserRepository;
import fr.ensitech.biblio2.service.EmailService;
import fr.ensitech.biblio2.service.ReservationService;
import fr.ensitech.biblio2.utils.Dates;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ReservationServiceIntegrationTest {

  @Autowired
  private ReservationService reservationService;

  @Autowired
  private IReservationRepository reservationRepository;

  @Autowired
  private IUserRepository userRepository;

  @Autowired
  private IBookRepository bookRepository;

  @MockitoBean
  private JavaMailSender mailSender;

  private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  private User testUser;
  private Book testBook;
  private Author author;

  @BeforeEach
  @SneakyThrows
  void setUp() {
    reservationRepository.deleteAll();
    bookRepository.deleteAll();
    userRepository.deleteAll();

    // Créer un utilisateur de test
    testUser = new User();
    testUser.setFirstName("Sophie");
    testUser.setLastName("MARTIN");
    testUser.setEmail("sophie.martin@example.com");
    testUser.setPassword(passwordEncoder.encode("Password123!"));
    testUser.setRole("U");
    testUser.setActive(true);
    testUser = userRepository.save(testUser);

    // Créer un auteur de test
    author = new Author();
    author.setFirstName("Victor");
    author.setLastName("HUGO");

    // Créer un livre de test
    testBook = new Book();
    testBook.setTitle("Les Misérables");
    testBook.setDescription("Un chef-d'œuvre de la littérature française");
    testBook.setIsbn("9781234567890");
    testBook.setEditor("Editions Gallimard");
    testBook.setCategory("Littérature");
    testBook.setNbPage((short) 1500);
    testBook.setLanguage("FR");
    testBook.setPublished(true);
    testBook.setPublicationDate(Dates.convertStringToDate("01/01/1862"));
    testBook.setStock(5);

    Set<Author> authors = new HashSet<>();
    authors.add(author);
    testBook.setAuthors(authors);

    testBook = bookRepository.save(testBook);

    // Reset le mock du mailSender
    reset(mailSender);
  }

  @Test
  @DisplayName("Réservation d'un livre avec succès")
  void shouldReserveBookSuccessfully() throws Exception {
    //WHEN
    Reservation reservation = reservationService.reserveBook(testBook.getId(), testUser.getEmail());

    //THEN
    assertThat(reservation).isNotNull();
    assertThat(reservation.getId()).isNotNull();
    assertThat(reservation.getUser().getId()).isEqualTo(testUser.getId());
    assertThat(reservation.getBook().getId()).isEqualTo(testBook.getId());
    assertThat(reservation.getStatus()).isEqualTo("ACTIVE");
    assertThat(reservation.getReservationDate()).isNotNull();

    // Vérifier que l'email a été envoyé
    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("Réservation enregistrée dans la base de données")
  void shouldSaveReservationInDatabase() throws Exception {
    //WHEN
    Reservation reservation = reservationService.reserveBook(testBook.getId(), testUser.getEmail());

    //THEN
    Reservation savedReservation = reservationRepository.findById(reservation.getId()).orElseThrow();
    assertThat(savedReservation.getUser().getEmail()).isEqualTo(testUser.getEmail());
    assertThat(savedReservation.getBook().getTitle()).isEqualTo(testBook.getTitle());
    assertThat(savedReservation.getStatus()).isEqualTo("ACTIVE");
  }

  @Test
  @DisplayName("Réservation avec utilisateur inexistant doit lever une exception")
  void shouldThrowExceptionWhenUserNotFound() {
    //WHEN & THEN
    assertThatThrownBy(() -> reservationService.reserveBook(testBook.getId(), "inexistant@example.com"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Utilisateur non trouvé");

    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("Réservation avec livre inexistant doit lever une exception")
  void shouldThrowExceptionWhenBookNotFound() {
    //WHEN & THEN
    assertThatThrownBy(() -> reservationService.reserveBook(9999L, testUser.getEmail()))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Livre non trouvé");

    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("Réservation avec utilisateur inactif doit lever une exception")
  void shouldThrowExceptionWhenUserNotActive() {
    //GIVEN
    testUser.setActive(false);
    userRepository.save(testUser);

    //WHEN & THEN
    assertThatThrownBy(() -> reservationService.reserveBook(testBook.getId(), testUser.getEmail()))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Compte utilisateur non activé");

    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("Réservation d'un livre déjà réservé par l'utilisateur doit lever une exception")
  void shouldThrowExceptionWhenBookAlreadyReservedByUser() throws Exception {
    //GIVEN
    reservationService.reserveBook(testBook.getId(), testUser.getEmail());
    reset(mailSender); // Reset pour ne compter que le deuxième appel

    //WHEN & THEN
    assertThatThrownBy(() -> reservationService.reserveBook(testBook.getId(), testUser.getEmail()))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Vous avez déjà réservé ce livre");

    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("Réservation avec limite de 3 réservations actives atteinte")
  void shouldThrowExceptionWhenUserReachedReservationLimit() throws Exception {
    //GIVEN
    // Créer 3 livres différents
    Book book1 = createBook("Livre 1", "9781111111111", 5);
    Book book2 = createBook("Livre 2", "9782222222222", 5);
    Book book3 = createBook("Livre 3", "9783333333333", 5);

    // Réserver 3 livres
    reservationService.reserveBook(book1.getId(), testUser.getEmail());
    reservationService.reserveBook(book2.getId(), testUser.getEmail());
    reservationService.reserveBook(book3.getId(), testUser.getEmail());

    reset(mailSender); // Reset pour ne compter que le quatrième appel

    //WHEN & THEN
    assertThatThrownBy(() -> reservationService.reserveBook(testBook.getId(), testUser.getEmail()))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Vous avez atteint la limite de 3 réservations actives");

    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("Réservation avec stock insuffisant doit lever une exception")
  void shouldThrowExceptionWhenBookOutOfStock() throws Exception {
    //GIVEN
    testBook.setStock(1);
    bookRepository.save(testBook);

    // Créer un autre utilisateur et réserver le dernier exemplaire
    User otherUser = createUser("Pierre", "DURAND", "pierre.durand@example.com");
    reservationService.reserveBook(testBook.getId(), otherUser.getEmail());

    reset(mailSender);

    //WHEN & THEN
    assertThatThrownBy(() -> reservationService.reserveBook(testBook.getId(), testUser.getEmail()))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Ce livre n'est plus disponible");

    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("Récupération des réservations d'un utilisateur")
  void shouldGetUserReservations() throws Exception {
    //GIVEN
    Book book1 = createBook("Livre A", "9784444444444", 5);
    Book book2 = createBook("Livre B", "9785555555555", 5);

    reservationService.reserveBook(book1.getId(), testUser.getEmail());
    reservationService.reserveBook(book2.getId(), testUser.getEmail());

    //WHEN
    List<Reservation> reservations = reservationService.getUserReservations(testUser.getEmail());

    //THEN
    assertThat(reservations).hasSize(2);
    assertThat(reservations).extracting(r -> r.getBook().getTitle())
            .containsExactlyInAnyOrder("Livre A", "Livre B");
    assertThat(reservations).allMatch(r -> r.getStatus().equals("ACTIVE"));
  }

  @Test
  @DisplayName("Récupération des réservations pour utilisateur sans réservation")
  void shouldReturnEmptyListWhenUserHasNoReservations() throws Exception {
    //WHEN
    List<Reservation> reservations = reservationService.getUserReservations(testUser.getEmail());

    //THEN
    assertThat(reservations).isEmpty();
  }

  @Test
  @DisplayName("Récupération des réservations pour utilisateur inexistant doit lever une exception")
  void shouldThrowExceptionWhenGettingReservationsForNonExistentUser() {
    //WHEN & THEN
    assertThatThrownBy(() -> reservationService.getUserReservations("inexistant@example.com"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Utilisateur non trouvé");
  }

  @Test
  @DisplayName("Annulation d'une réservation")
  void shouldCancelReservation() throws Exception {
    //GIVEN
    Reservation reservation = reservationService.reserveBook(testBook.getId(), testUser.getEmail());

    //WHEN
    reservationService.cancelReservation(reservation.getId());

    //THEN
    Reservation cancelledReservation = reservationRepository.findById(reservation.getId()).orElseThrow();
    assertThat(cancelledReservation.getStatus()).isEqualTo("CANCELLED");
  }

  @Test
  @DisplayName("Annulation d'une réservation inexistante doit lever une exception")
  void shouldThrowExceptionWhenCancellingNonExistentReservation() {
    //WHEN & THEN
    assertThatThrownBy(() -> reservationService.cancelReservation(9999L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Réservation non trouvée");
  }

  @Test
  @DisplayName("Réservation annulée n'apparaît plus dans les réservations actives")
  void shouldNotIncludeCancelledReservationInActiveReservations() throws Exception {
    //GIVEN
    Book book1 = createBook("Livre X", "9786666666666", 5);
    Book book2 = createBook("Livre Y", "9787777777777", 5);

    Reservation reservation1 = reservationService.reserveBook(book1.getId(), testUser.getEmail());
    reservationService.reserveBook(book2.getId(), testUser.getEmail());

    //WHEN
    reservationService.cancelReservation(reservation1.getId());
    List<Reservation> activeReservations = reservationService.getUserReservations(testUser.getEmail());

    //THEN
    assertThat(activeReservations).hasSize(1);
    assertThat(activeReservations.get(0).getBook().getTitle()).isEqualTo("Livre Y");
  }

  @Test
  @DisplayName("Calcul du stock disponible pour un livre sans réservation")
  void shouldCalculateAvailableStockWithoutReservations() throws Exception {
    //WHEN
    int availableStock = reservationService.getAvailableStock(testBook.getId());

    //THEN
    assertThat(availableStock).isEqualTo(5);
  }

  @Test
  @DisplayName("Calcul du stock disponible pour un livre avec réservations")
  void shouldCalculateAvailableStockWithReservations() throws Exception {
    //GIVEN
    User user2 = createUser("Marie", "DUBOIS", "marie.dubois@example.com");
    User user3 = createUser("Luc", "BERNARD", "luc.bernard@example.com");

    reservationService.reserveBook(testBook.getId(), testUser.getEmail());
    reservationService.reserveBook(testBook.getId(), user2.getEmail());
    reservationService.reserveBook(testBook.getId(), user3.getEmail());

    //WHEN
    int availableStock = reservationService.getAvailableStock(testBook.getId());

    //THEN
    assertThat(availableStock).isEqualTo(2); // 5 - 3 = 2
  }

  @Test
  @DisplayName("Calcul du stock disponible ne compte pas les réservations annulées")
  void shouldNotCountCancelledReservationsInAvailableStock() throws Exception {
    //GIVEN
    User user2 = createUser("Alice", "PETIT", "alice.petit@example.com");

    Reservation reservation1 = reservationService.reserveBook(testBook.getId(), testUser.getEmail());
    reservationService.reserveBook(testBook.getId(), user2.getEmail());

    reservationService.cancelReservation(reservation1.getId());

    //WHEN
    int availableStock = reservationService.getAvailableStock(testBook.getId());

    //THEN
    assertThat(availableStock).isEqualTo(4); // 5 - 1 = 4 (une seule réservation active)
  }

  @Test
  @DisplayName("Calcul du stock disponible pour un livre inexistant doit lever une exception")
  void shouldThrowExceptionWhenCalculatingStockForNonExistentBook() {
    //WHEN & THEN
    assertThatThrownBy(() -> reservationService.getAvailableStock(9999L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Livre non trouvé");
  }

  @Test
  @DisplayName("Stock disponible à zéro empêche les nouvelles réservations")
  void shouldPreventReservationWhenStockIsZero() throws Exception {
    //GIVEN
    testBook.setStock(2);
    bookRepository.save(testBook);

    User user2 = createUser("Thomas", "ROBERT", "thomas.robert@example.com");
    User user3 = createUser("Emma", "RICHARD", "emma.richard@example.com");

    reservationService.reserveBook(testBook.getId(), testUser.getEmail());
    reservationService.reserveBook(testBook.getId(), user2.getEmail());

    reset(mailSender);

    //WHEN & THEN
    assertThatThrownBy(() -> reservationService.reserveBook(testBook.getId(), user3.getEmail()))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Ce livre n'est plus disponible");

    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("Email de confirmation envoyé avec les bonnes informations")
  void shouldSendConfirmationEmailWithCorrectInformation() throws Exception {
    //GIVEN
    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    //WHEN
    reservationService.reserveBook(testBook.getId(), testUser.getEmail());

    //THEN
    verify(mailSender, times(1)).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertThat(sentMessage.getTo()).containsExactly(testUser.getEmail());
    assertThat(sentMessage.getSubject()).isEqualTo("Confirmation de réservation - Biblio");
    assertThat(sentMessage.getText()).contains("Sophie");
    assertThat(sentMessage.getText()).contains("MARTIN");
    assertThat(sentMessage.getText()).contains("Les Misérables");
  }

  @Test
  @DisplayName("Utilisateur peut réserver à nouveau après annulation")
  void shouldAllowReservationAfterCancellation() throws Exception {
    //GIVEN
    Reservation reservation = reservationService.reserveBook(testBook.getId(), testUser.getEmail());
    reservationService.cancelReservation(reservation.getId());

    reset(mailSender);

    //WHEN
    Reservation newReservation = reservationService.reserveBook(testBook.getId(), testUser.getEmail());

    //THEN
    assertThat(newReservation).isNotNull();
    assertThat(newReservation.getId()).isNotEqualTo(reservation.getId());
    assertThat(newReservation.getStatus()).isEqualTo("ACTIVE");

    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("Utilisateur peut réserver un quatrième livre après annulation d'une réservation")
  void shouldAllowFourthReservationAfterCancellation() throws Exception {
    //GIVEN
    Book book1 = createBook("Livre 1", "9788888888888", 5);
    Book book2 = createBook("Livre 2", "9789999999999", 5);
    Book book3 = createBook("Livre 3", "9780000000000", 5);

    Reservation res1 = reservationService.reserveBook(book1.getId(), testUser.getEmail());
    reservationService.reserveBook(book2.getId(), testUser.getEmail());
    reservationService.reserveBook(book3.getId(), testUser.getEmail());

    // Annuler une réservation
    reservationService.cancelReservation(res1.getId());

    reset(mailSender);

    //WHEN
    Reservation newReservation = reservationService.reserveBook(testBook.getId(), testUser.getEmail());

    //THEN
    assertThat(newReservation).isNotNull();
    assertThat(newReservation.getStatus()).isEqualTo("ACTIVE");

    List<Reservation> activeReservations = reservationService.getUserReservations(testUser.getEmail());
    assertThat(activeReservations).hasSize(3);

    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("Plusieurs utilisateurs peuvent réserver le même livre")
  void shouldAllowMultipleUsersToReserveSameBook() throws Exception {
    //GIVEN
    User user2 = createUser("Paul", "SIMON", "paul.simon@example.com");
    User user3 = createUser("Julie", "LAURENT", "julie.laurent@example.com");

    //WHEN
    Reservation res1 = reservationService.reserveBook(testBook.getId(), testUser.getEmail());
    Reservation res2 = reservationService.reserveBook(testBook.getId(), user2.getEmail());
    Reservation res3 = reservationService.reserveBook(testBook.getId(), user3.getEmail());

    //THEN
    assertThat(res1.getUser().getId()).isEqualTo(testUser.getId());
    assertThat(res2.getUser().getId()).isEqualTo(user2.getId());
    assertThat(res3.getUser().getId()).isEqualTo(user3.getId());

    assertThat(res1.getBook().getId()).isEqualTo(testBook.getId());
    assertThat(res2.getBook().getId()).isEqualTo(testBook.getId());
    assertThat(res3.getBook().getId()).isEqualTo(testBook.getId());

    int availableStock = reservationService.getAvailableStock(testBook.getId());
    assertThat(availableStock).isEqualTo(2); // 5 - 3 = 2
  }

  @Test
  @DisplayName("Date de réservation est définie automatiquement")
  void shouldSetReservationDateAutomatically() throws Exception {
    //GIVEN
    long beforeReservation = System.currentTimeMillis();

    //WHEN
    Reservation reservation = reservationService.reserveBook(testBook.getId(), testUser.getEmail());

    //THEN
    long afterReservation = System.currentTimeMillis();
    assertThat(reservation.getReservationDate()).isNotNull();
    assertThat(reservation.getReservationDate().getTime()).isBetween(beforeReservation, afterReservation);
  }

  @Test
  @DisplayName("Comptage correct des réservations actives par utilisateur")
  void shouldCountActiveReservationsCorrectly() throws Exception {
    //GIVEN
    Book book1 = createBook("Livre Alpha", "9781010101010", 5);
    Book book2 = createBook("Livre Beta", "9782020202020", 5);

    reservationService.reserveBook(book1.getId(), testUser.getEmail());
    Reservation res2 = reservationService.reserveBook(book2.getId(), testUser.getEmail());

    // Annuler une réservation
    reservationService.cancelReservation(res2.getId());

    //WHEN
    long activeCount = reservationRepository.countActiveReservationsByUserId(testUser.getId());

    //THEN
    assertThat(activeCount).isEqualTo(1);
  }

  @Test
  @DisplayName("Vérification que le stock n'est pas modifié par les réservations")
  void shouldNotModifyBookStockOnReservation() throws Exception {
    //GIVEN
    int initialStock = testBook.getStock();

    //WHEN
    reservationService.reserveBook(testBook.getId(), testUser.getEmail());

    //THEN
    Book bookAfterReservation = bookRepository.findById(testBook.getId()).orElseThrow();
    assertThat(bookAfterReservation.getStock()).isEqualTo(initialStock);
  }

  // Méthodes utilitaires

  private Book createBook(String title, String isbn, int stock) throws Exception {
    Book book = new Book();
    book.setTitle(title);
    book.setDescription("Description de " + title);
    book.setIsbn(isbn);
    book.setEditor("Editions Test");
    book.setCategory("Informatique");
    book.setNbPage((short) 300);
    book.setLanguage("FR");
    book.setPublished(true);
    book.setPublicationDate(Dates.convertStringToDate("01/01/2020"));
    book.setStock(stock);

    Set<Author> authors = new HashSet<>();
    authors.add(author);
    book.setAuthors(authors);

    return bookRepository.save(book);
  }

  private User createUser(String firstName, String lastName, String email) {
    User user = new User();
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode("Password123!"));
    user.setRole("U");
    user.setActive(true);
    return userRepository.save(user);
  }
}
