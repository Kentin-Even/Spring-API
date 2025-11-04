package fr.ensitech.biblio2.controller;

import fr.ensitech.biblio2.entity.Book;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/api/books")
public class BookController implements IBookController {

  @PostMapping("/create")
  @Override
  public ResponseEntity<Book> createBook(Book book) {
    return null;
  }

  @GetMapping("/{id}")
  @Override
  public ResponseEntity<Book> getBookById(@PathVariable("id") long id) {
    return null;
  }

  @Override
  public ResponseEntity<Book> updateBook(Book book) {
    return null;
  }

  @Override
  public ResponseEntity<Book> deleteBookById(long id) {
    return null;
  }

  @Override
  public ResponseEntity<List<Book>> getAllBooks() {
    return null;
  }
}
