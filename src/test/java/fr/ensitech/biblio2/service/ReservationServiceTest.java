package fr.ensitech.biblio2.service;

import fr.ensitech.biblio2.entity.Book;
import fr.ensitech.biblio2.entity.Reservation;
import fr.ensitech.biblio2.entity.User;
import fr.ensitech.biblio2.repository.IBookRepository;
import fr.ensitech.biblio2.repository.IReservationRepository;
import fr.ensitech.biblio2.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ReservationService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - ReservationService")
class ReservationServiceTest {

  @Mock
  private IReservationRepository reservationRepository;

  @Mock
  private IBookRepository bookRepository;

  @Mock
  private IUserRepository userRepository;

  @Mock
  private EmailService emailService;

  @InjectMocks
  private ReservationService reservationService;

  private User testUser;
  private Book testBook;
  private Reservation testReservation;

  @BeforeEach
  void setUp() {
    // User de test
    testUser = new User();
    testUser.setId(1L);
    testUser.setFirstName("John");
    testUser.setLastName("Doe");
    testUser.setEmail("john.doe@test.com");
    testUser.setActive(true);

    // Book de test
    testBook = new Book();
    testBook.setId(1L);
    testBook.setIsbn("9781234567890");
    testBook.setTitle("Test Book");
    testBook.setDescription("Test description");
    testBook.setEditor("Test Editor");
    testBook.setPublicationDate(new Date());
    testBook.setCategory("Fiction");
    testBook.setLanguage("FR");
    testBook.setNbPage((short) 300);
    testBook.setStock(5);

    // Reservation de test
    testReservation = new Reservation();
    testReservation.setId(1L);
    testReservation.setUser(testUser);
    testReservation.setBook(testBook);
    testReservation.setReservationDate(new Date());
    testReservation.setStatus("ACTIVE");
  }

  @Test
  @DisplayName("Devrait réserver un livre avec succès")
  void shouldReserveBookSuccessfully() throws Exception {
    // Given
    when(userRepository.findByEmail("john.doe@test.com")).thenReturn(testUser);
    when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
    when(reservationRepository.countActiveReservationsByUserAndBook(1L, 1L)).thenReturn(0L);
    when(reservationRepository.countActiveReservationsByUserId(1L)).thenReturn(0L);
    when(reservationRepository.findByBookAndStatus(testBook, "ACTIVE")).thenReturn(Arrays.asList());
    when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);
    doNothing().when(emailService).sendReservationConfirmationEmail(
            anyString(), anyString(), anyString(), anyString());

    // When
    Reservation result = reservationService.reserveBook(1L, "john.doe@test.com");

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo("ACTIVE");
    verify(userRepository, times(1)).findByEmail("john.doe@test.com");
    // 1er appel dans reserveBook, 2ème dans getAvailableStock
    verify(bookRepository, times(2)).findById(1L);
    verify(reservationRepository, times(1)).save(any(Reservation.class));
    verify(emailService, times(1)).sendReservationConfirmationEmail(
            anyString(), anyString(), anyString(), anyString());
  }

  @Test
  @DisplayName("Devrait lever une exception si l'utilisateur n'existe pas")
  void shouldThrowExceptionWhenUserNotFound() {
    // Given
    when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(null);

    // When & Then
    assertThatThrownBy(() -> reservationService.reserveBook(1L, "nonexistent@test.com"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Utilisateur non trouvé");

    verify(userRepository, times(1)).findByEmail("nonexistent@test.com");
    verify(reservationRepository, never()).save(any(Reservation.class));
  }

  @Test
  @DisplayName("Devrait lever une exception si le compte n'est pas activé")
  void shouldThrowExceptionWhenAccountNotActivated() {
    // Given
    testUser.setActive(false);
    when(userRepository.findByEmail("john.doe@test.com")).thenReturn(testUser);

    // When & Then
    assertThatThrownBy(() -> reservationService.reserveBook(1L, "john.doe@test.com"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("non activé");

    verify(userRepository, times(1)).findByEmail("john.doe@test.com");
    verify(bookRepository, never()).findById(anyLong());
  }

  @Test
  @DisplayName("Devrait lever une exception si le livre n'existe pas")
  void shouldThrowExceptionWhenBookNotFound() {
    // Given
    when(userRepository.findByEmail("john.doe@test.com")).thenReturn(testUser);
    when(bookRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> reservationService.reserveBook(999L, "john.doe@test.com"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Livre non trouvé");

    verify(userRepository, times(1)).findByEmail("john.doe@test.com");
    verify(bookRepository, times(1)).findById(999L);
    verify(reservationRepository, never()).save(any(Reservation.class));
  }

  @Test
  @DisplayName("Devrait lever une exception si le livre est déjà réservé par l'utilisateur")
  void shouldThrowExceptionWhenBookAlreadyReserved() {
    // Given
    when(userRepository.findByEmail("john.doe@test.com")).thenReturn(testUser);
    when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
    when(reservationRepository.countActiveReservationsByUserAndBook(1L, 1L)).thenReturn(1L);

    // When & Then
    assertThatThrownBy(() -> reservationService.reserveBook(1L, "john.doe@test.com"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("déjà réservé");

    verify(reservationRepository, times(1))
            .countActiveReservationsByUserAndBook(1L, 1L);
    verify(reservationRepository, never()).save(any(Reservation.class));
  }

  @Test
  @DisplayName("Devrait lever une exception si l'utilisateur a atteint la limite de réservations")
  void shouldThrowExceptionWhenReservationLimitReached() {
    // Given
    when(userRepository.findByEmail("john.doe@test.com")).thenReturn(testUser);
    when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
    when(reservationRepository.countActiveReservationsByUserAndBook(1L, 1L)).thenReturn(0L);
    when(reservationRepository.countActiveReservationsByUserId(1L)).thenReturn(3L);

    // When & Then
    assertThatThrownBy(() -> reservationService.reserveBook(1L, "john.doe@test.com"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("limite de 3 réservations");

    verify(reservationRepository, times(1)).countActiveReservationsByUserId(1L);
    verify(reservationRepository, never()).save(any(Reservation.class));
  }

  @Test
  @DisplayName("Devrait lever une exception si le stock est insuffisant")
  void shouldThrowExceptionWhenInsufficientStock() throws Exception {
    // Given
    testBook.setStock(2);
    when(userRepository.findByEmail("john.doe@test.com")).thenReturn(testUser);
    when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
    when(reservationRepository.countActiveReservationsByUserAndBook(1L, 1L)).thenReturn(0L);
    when(reservationRepository.countActiveReservationsByUserId(1L)).thenReturn(0L);

    // Simuler 2 réservations actives (stock = 2, donc plus disponible)
    Reservation res1 = new Reservation();
    res1.setBook(testBook);
    res1.setStatus("ACTIVE");
    Reservation res2 = new Reservation();
    res2.setBook(testBook);
    res2.setStatus("ACTIVE");

    when(reservationRepository.findByBookAndStatus(testBook, "ACTIVE"))
            .thenReturn(Arrays.asList(res1, res2));

    // When & Then
    assertThatThrownBy(() -> reservationService.reserveBook(1L, "john.doe@test.com"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("plus disponible");

    verify(reservationRepository, never()).save(any(Reservation.class));
  }

  @Test
  @DisplayName("Devrait récupérer les réservations d'un utilisateur")
  void shouldGetUserReservations() throws Exception {
    // Given
    List<Reservation> expectedReservations = Arrays.asList(testReservation);
    when(userRepository.findByEmail("john.doe@test.com")).thenReturn(testUser);
    when(reservationRepository.findByUserAndStatus(testUser, "ACTIVE"))
            .thenReturn(expectedReservations);

    // When
    List<Reservation> result = reservationService.getUserReservations("john.doe@test.com");

    // Then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getStatus()).isEqualTo("ACTIVE");
    verify(userRepository, times(1)).findByEmail("john.doe@test.com");
    verify(reservationRepository, times(1)).findByUserAndStatus(testUser, "ACTIVE");
  }

  @Test
  @DisplayName("Devrait annuler une réservation")
  void shouldCancelReservation() throws Exception {
    // Given
    when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
    when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

    // When
    reservationService.cancelReservation(1L);

    // Then
    verify(reservationRepository, times(1)).findById(1L);
    verify(reservationRepository, times(1)).save(argThat(reservation ->
            "CANCELLED".equals(reservation.getStatus())
    ));
  }

  @Test
  @DisplayName("Devrait lever une exception lors de l'annulation d'une réservation inexistante")
  void shouldThrowExceptionWhenCancellingNonExistentReservation() {
    // Given
    when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> reservationService.cancelReservation(999L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Réservation non trouvée");

    verify(reservationRepository, times(1)).findById(999L);
    verify(reservationRepository, never()).save(any(Reservation.class));
  }

  @Test
  @DisplayName("Devrait calculer le stock disponible correctement")
  void shouldCalculateAvailableStockCorrectly() throws Exception {
    // Given
    testBook.setStock(10);
    Reservation activeRes1 = new Reservation();
    activeRes1.setBook(testBook);
    activeRes1.setStatus("ACTIVE");
    Reservation activeRes2 = new Reservation();
    activeRes2.setBook(testBook);
    activeRes2.setStatus("ACTIVE");

    when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
    when(reservationRepository.findByBookAndStatus(testBook, "ACTIVE"))
            .thenReturn(Arrays.asList(activeRes1, activeRes2));

    // When
    int availableStock = reservationService.getAvailableStock(1L);

    // Then
    assertThat(availableStock).isEqualTo(8); // 10 - 2 réservations actives
    verify(bookRepository, times(1)).findById(1L);
    verify(reservationRepository, times(1)).findByBookAndStatus(testBook, "ACTIVE");
  }

  @Test
  @DisplayName("Devrait retourner 0 si tout le stock est réservé")
  void shouldReturnZeroWhenAllStockReserved() throws Exception {
    // Given
    testBook.setStock(2);
    Reservation res1 = new Reservation();
    res1.setBook(testBook);
    res1.setStatus("ACTIVE");
    Reservation res2 = new Reservation();
    res2.setBook(testBook);
    res2.setStatus("ACTIVE");

    when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
    when(reservationRepository.findByBookAndStatus(testBook, "ACTIVE"))
            .thenReturn(Arrays.asList(res1, res2));

    // When
    int availableStock = reservationService.getAvailableStock(1L);

    // Then
    assertThat(availableStock).isEqualTo(0);
  }
}