package fr.ensitech.biblio2.controller;

import fr.ensitech.biblio2.entity.Book;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface IBookController {

  ResponseEntity<Book> createBook(Book book);
  ResponseEntity<Book> getBookById(long id);
  ResponseEntity<Book> updateBook(Book book);
  ResponseEntity<String> deleteBookById(long id);
  ResponseEntity<List<Book>> getAllBooks();
  ResponseEntity<String> reserveBook(@PathVariable long bookId, @PathVariable String email);
  ResponseEntity<Book> getBookByTitle(@RequestParam String title);
  ResponseEntity<List<Book>> getBooksByTitleContaining(@RequestParam String title);
  ResponseEntity<Book> getBookByIsbn(@RequestParam String isbn);
  ResponseEntity<List<Book>> getBooksByPublished(@RequestParam boolean published);
  ResponseEntity<List<Book>> searchBooksByTitleOrDescription(@RequestParam String keyword);
  ResponseEntity<List<Book>> getBooksBetweenYears(@RequestParam int startYear, @RequestParam int endYear);
}