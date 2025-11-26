package fr.ensitech.biblio2.controller;

import fr.ensitech.biblio2.entity.Book;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface IBookController {

  ResponseEntity<Book> createBook(Book book);
  ResponseEntity<Book> getBookById(long id);
  ResponseEntity<Book> updateBook(Book book);
  ResponseEntity<String> deleteBookById(long id);
  ResponseEntity<List<Book>> getAllBooks();
  ResponseEntity<String> reserveBook(@PathVariable long bookId, @PathVariable String email);
}