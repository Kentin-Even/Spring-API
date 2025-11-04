package fr.ensitech.biblio2.controller;

import fr.ensitech.biblio2.entity.Book;
import fr.ensitech.biblio2.service.IBookService;
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

  @PostMapping("/create")
  @Override
  public ResponseEntity<Book> createBook(Book book) {
    if(book == null
            || book.getIsbn() == null || book.getIsbn().isEmpty()
            || book.getTitle() == null || book.getTitle().isEmpty()
            || book.getDescription() == null || book.getDescription().isEmpty()
            || book.getEditor() == null || book.getEditor().isEmpty()
            || book.getPublicationDate() == null
            || book.getCategory() == null || book.getCategory().isEmpty()
            || book.getLanguage() == null || book.getLanguage().isEmpty()
            || book.getNbPage() < 0) {

      return new ResponseEntity<Book>(HttpStatus.BAD_REQUEST);

    }
    try {
      bookService.addOrUpdateBook(book);

      return new ResponseEntity<>(book, HttpStatus.CREATED);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/{id}")
  @Override
  public ResponseEntity<Book> getBookById(@PathVariable("id") @RequestParam(required = true) long id) {
    return null;
  }

  @Override
  @PutMapping("/update")
  public ResponseEntity<Book> updateBook(@RequestBody Book book) {
    return null;
  }

  @Override
  @DeleteMapping("/delete/{id}")
  public ResponseEntity<Book> deleteBookById(@PathVariable("id") @RequestParam(required = true) long id) {
    return null;
  }

  @GetMapping("/all")
  @Override
  public ResponseEntity<List<Book>> getAllBooks() {
    return null;
  }
}
