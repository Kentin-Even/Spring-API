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

  @Query("SELECT COUNT(r) FROM Reservation r WHERE r.user.id = ?1 AND r.status = 'ACTIVE'")
  long countActiveReservationsByUserId(long userId);

  @Query("SELECT COUNT(r) FROM Reservation r WHERE r.user.id = ?1 AND r.book.id = ?2 AND r.status = 'ACTIVE'")
  long countActiveReservationsByUserAndBook(long userId, long bookId);

  List<Reservation> findByUserAndStatus(User user, String status);

  List<Reservation> findByBookAndStatus(Book book, String status);
}