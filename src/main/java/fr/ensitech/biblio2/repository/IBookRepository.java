package fr.ensitech.biblio2.repository;

import fr.ensitech.biblio2.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface IBookRepository extends JpaRepository<Book, Long> {

  List<Book> findByPublished(boolean published);
  List<Book> findByTitleIgnoreCase(String title);
  List<Book> findByTitleContainingIgnoreCase(String title);
  Book findByIsbnIgnoreCase(String isbn);
  List<Book> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
  List<Book> findByPublicationDateBetween(Date start, Date end);

}