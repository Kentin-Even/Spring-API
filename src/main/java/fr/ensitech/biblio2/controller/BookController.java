package fr.ensitech.biblio2.controller;

import fr.ensitech.biblio2.entity.Book;
import fr.ensitech.biblio2.entity.Reservation;
import fr.ensitech.biblio2.service.IBookService;
import fr.ensitech.biblio2.service.IReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/api/books")
public class BookController implements IBookController {

  @Autowired
  private IBookService bookService;

  @Autowired
  private IReservationService reservationService;

  @PostMapping("/create")
  @Override
  public ResponseEntity<Book> createBook(@RequestBody Book book) {
    if(book == null
            || book.getIsbn() == null || book.getIsbn().isEmpty()
            || book.getTitle() == null || book.getTitle().isEmpty()
            || book.getDescription() == null || book.getDescription().isEmpty()
            || book.getEditor() == null || book.getEditor().isEmpty()
            || book.getPublicationDate() == null
            || book.getCategory() == null || book.getCategory().isEmpty()
            || book.getLanguage() == null || book.getLanguage().isEmpty()
            || book.getNbPage() < 0) {

      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    try {
      bookService.addOrUpdateBook(book);
      return new ResponseEntity<>(book, HttpStatus.CREATED);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la création du livre");
    }
  }

  @GetMapping("/{id}")
  @Override
  public ResponseEntity<Book> getBookById(@PathVariable("id") long id) {

    if (id <= 0) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    try {
      Book book = bookService.getBook(id);
      if (book == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      return new ResponseEntity<>(book, HttpStatus.OK);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la récupération du livre");
    }
  }

  @Override
  @PutMapping("/update")
  public ResponseEntity<Book> updateBook(@RequestBody Book book) {
    if(book == null || book.getId() == null) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    try {
      Book existingBook = bookService.getBook(book.getId());
      if (existingBook == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }

      if(book.getIsbn() == null || book.getIsbn().isEmpty()
              || book.getTitle() == null || book.getTitle().isEmpty()
              || book.getDescription() == null || book.getDescription().isEmpty()
              || book.getEditor() == null || book.getEditor().isEmpty()
              || book.getPublicationDate() == null
              || book.getCategory() == null || book.getCategory().isEmpty()
              || book.getLanguage() == null || book.getLanguage().isEmpty()
              || book.getNbPage() < 0) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }

      bookService.addOrUpdateBook(book);
      return new ResponseEntity<>(book, HttpStatus.OK);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la mise à jour du livre");
    }
  }

  @Override
  @DeleteMapping("/delete/{id}")
  public ResponseEntity<String> deleteBookById(@PathVariable("id") long id) {
    try {
      Book book = bookService.getBook(id);
      if (book == null) {
        return new ResponseEntity<>("book id must be greater than 0",HttpStatus.NOT_FOUND);
      }

      bookService.deleteBook(id);
      String message = "Book id ".concat(String.valueOf(id)).concat(" deleted");
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      String error = "Erreur interne" + e.getMessage();
      return new ResponseEntity<String>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/all")
  @Override
  public ResponseEntity<List<Book>> getAllBooks() {
    try {
      List<Book> books = bookService.getBooks();
      if (books.isEmpty()) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }
      return new ResponseEntity<>(books, HttpStatus.OK);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la récupération des livres");
    }
  }

  @PutMapping("/reserver/{bookId}/{email}")
  @Override
  public ResponseEntity<String> reserveBook(@PathVariable long bookId, @PathVariable String email) {
    if (email == null || email.isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body("{\"message\": \"L'email est requis\"}");
    }

    try {
      Reservation reservation = reservationService.reserveBook(bookId, email);
      return ResponseEntity.ok("{\"message\": \"Livre réservé avec succès\", \"reservationId\": " + reservation.getId() + "}");
    } catch (Exception e) {
      e.printStackTrace();

      if (e.getMessage().contains("non trouvé")) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("{\"message\": \"" + e.getMessage() + "\"}");
      } else if (e.getMessage().contains("déjà réservé") ||
              e.getMessage().contains("limite") ||
              e.getMessage().contains("disponible")) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"message\": \"" + e.getMessage() + "\"}");
      } else {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\": \"Erreur lors de la réservation: " + e.getMessage() + "\"}");
      }
    }
  }

  @GetMapping("/search/by-title")
  @Override
  public ResponseEntity<Book> getBookByTitle(@RequestParam String title) {
    if (title == null || title.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    try {
      Book book = bookService.getBookByTitle(title);
      if (book == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      return new ResponseEntity<>(book, HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              "Erreur lors de la recherche du livre par titre");
    }
  }

  @GetMapping("/search/by-title-containing")
  @Override
  public ResponseEntity<List<Book>> getBooksByTitleContaining(@RequestParam String title) {
    if (title == null || title.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    try {
      List<Book> books = bookService.getBooksByTitleContaining(title);
      if (books.isEmpty()) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }
      return new ResponseEntity<>(books, HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              "Erreur lors de la recherche des livres par titre contenant");
    }
  }

  @GetMapping("/search/by-isbn")
  @Override
  public ResponseEntity<Book> getBookByIsbn(@RequestParam String isbn) {
    if (isbn == null || isbn.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    try {
      Book book = bookService.getBookByIsbn(isbn);
      if (book == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      return new ResponseEntity<>(book, HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              "Erreur lors de la recherche du livre par ISBN");
    }
  }

  @GetMapping("/search/by-published")
  @Override
  public ResponseEntity<List<Book>> getBooksByPublished(@RequestParam boolean published) {
    try {
      List<Book> books = bookService.getBooksByPublished(published);
      if (books.isEmpty()) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }
      return new ResponseEntity<>(books, HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              "Erreur lors de la recherche des livres par statut de publication");
    }
  }

  @GetMapping("/search/by-keyword")
  @Override
  public ResponseEntity<List<Book>> searchBooksByTitleOrDescription(@RequestParam String keyword) {
    if (keyword == null || keyword.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    try {
      List<Book> books = bookService.getBooksByTitleOrDescription(keyword, keyword);
      if (books.isEmpty()) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }
      return new ResponseEntity<>(books, HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              "Erreur lors de la recherche des livres par mot-clé");
    }
  }

  @GetMapping("/search/by-years")
  @Override
  public ResponseEntity<List<Book>> getBooksBetweenYears(
          @RequestParam int startYear,
          @RequestParam int endYear) {

    if (startYear < 0 || endYear < 0 || startYear > endYear) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    try {
      List<Book> books = bookService.getBooksBetweenYears(startYear, endYear);
      if (books.isEmpty()) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }
      return new ResponseEntity<>(books, HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              "Erreur lors de la recherche des livres entre deux années");
    }
  }
}