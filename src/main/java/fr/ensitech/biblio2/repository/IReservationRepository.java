package fr.ensitech.biblio2.repository;

import fr.ensitech.biblio2.entity.Reservation;
import fr.ensitech.biblio2.entity.User;
import fr.ensitech.biblio2.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IReservationRepository extends JpaRepository<Reservation, Long> {

  // Compter les réservations actives d'un utilisateur
  @Query("SELECT COUNT(r) FROM Reservation r WHERE r.user.id = ?1 AND r.status = 'ACTIVE'")
  long countActiveReservationsByUserId(long userId);

  // Vérifier si un utilisateur a déjà réservé un livre spécifique
  @Query("SELECT COUNT(r) FROM Reservation r WHERE r.user.id = ?1 AND r.book.id = ?2 AND r.status = 'ACTIVE'")
  long countActiveReservationsByUserAndBook(long userId, long bookId);

  // Récupérer toutes les réservations actives d'un utilisateur
  List<Reservation> findByUserAndStatus(User user, String status);

  // Récupérer toutes les réservations actives d'un livre
  List<Reservation> findByBookAndStatus(Book book, String status);
}