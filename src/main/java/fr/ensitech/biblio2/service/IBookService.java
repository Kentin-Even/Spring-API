package fr.ensitech.biblio2.service;

import fr.ensitech.biblio2.entity.Author;
import fr.ensitech.biblio2.entity.Book;

import java.util.Date;
import java.util.List;

public interface IBookService {

  void addOrUpdateBook(Book book) throws Exception;
  void deleteBook(long id) throws Exception;  // Changé de Book à long
  List<Book> getBooks() throws Exception;
  Book getBook(long id) throws Exception;
  List<Book> getBooksByTitle(String title) throws Exception;
  List<Book> getBooksByAuthor(Author author) throws Exception;
  List<Book> getBooksBetweenYears(int startYear, int endYear) throws Exception;
  List<Book> getBooksByPublished(boolean published);
  Book getBookByIsbn(String isbn);
  List<Book> getBooksByTitleOrDescription(String title, String description);
}