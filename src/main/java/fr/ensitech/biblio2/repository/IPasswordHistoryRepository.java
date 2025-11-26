package fr.ensitech.biblio2.repository;

import fr.ensitech.biblio2.entity.PasswordHistory;
import fr.ensitech.biblio2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {

  @Query("SELECT ph FROM PasswordHistory ph WHERE ph.user.id = ?1 ORDER BY ph.createdAt DESC")
  List<PasswordHistory> findTop5ByUserIdOrderByCreatedAtDesc(long userId);

  List<PasswordHistory> findByUserOrderByCreatedAtDesc(User user);

  long countByUser(User user);
}