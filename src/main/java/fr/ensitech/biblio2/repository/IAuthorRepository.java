package fr.ensitech.biblio2.repository;

import fr.ensitech.biblio2.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IAuthorRepository extends JpaRepository<Author, Long> {

  List<Author> findByFirstNameIgnoreCase(String firstName);

  @Query("select a from Author a where a.firstName = ?1 and a.lastName = ?2")
  List<Author> findAuthors(String firstName, String lastName);
}
