package fr.ensitech.biblio2.service;

import fr.ensitech.biblio2.entity.Author;
import fr.ensitech.biblio2.entity.Book;
import fr.ensitech.biblio2.repository.IBookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class BookService implements IBookService {

  @Autowired
  private IBookRepository bookRepository;

  @Override
  public void addOrUpdateBook(Book book) throws Exception {
    if (book.getId() == null || book.getId() == 0) {
      bookRepository.save(book);
    } else {
      if (book.getId() < 0) {
        throw new Exception("Book id must be greater than 0");
      }

      Optional<Book> optionalBook = bookRepository.findById(book.getId());
      if (optionalBook.isEmpty()) {
        throw new Exception("Book with id " + book.getId() + " not found");
      }

      Book _book = optionalBook.get();
      _book.setIsbn(book.getIsbn());
      _book.setTitle(book.getTitle());
      _book.setDescription(book.getDescription());
      _book.setEditor(book.getEditor());
      _book.setPublicationDate(book.getPublicationDate());
      _book.setCategory(book.getCategory());
      _book.setLanguage(book.getLanguage());
      _book.setNbPage(book.getNbPage());
      _book.setPublished(book.isPublished());
      bookRepository.save(_book);
    }
  }

  @Override
  public void deleteBook(long id) throws Exception {
    Optional<Book> optionalBook = bookRepository.findById(id);
    if (optionalBook.isEmpty()) {
      throw new Exception("Book with id " + id + " not found");
    }
    bookRepository.deleteById(id);
  }

  @Override
  public List<Book> getBooks() throws Exception {
    return bookRepository.findAll();
  }

  @Override
  public Book getBook(long id) throws Exception {
    Optional<Book> optional = bookRepository.findById(id);
    return optional.orElse(null);
  }

  @Override
  public List<Book> getBooksByTitle(String title) throws Exception {
    return bookRepository.findByTitleContainingIgnoreCase(title);
  }

  @Override
  public List<Book> getBooksByAuthor(Author author) throws Exception {
    return null;
  }

  @Override
  public List<Book> getBooksBetweenYears(int startYear, int endYear) throws Exception {
    Calendar startCalendar = Calendar.getInstance();
    startCalendar.set(Calendar.YEAR, startYear);
    startCalendar.set(Calendar.MONTH, Calendar.JANUARY);
    startCalendar.set(Calendar.DAY_OF_MONTH, 1);
    Date startDate = startCalendar.getTime();

    Calendar endCalendar = Calendar.getInstance();
    endCalendar.set(Calendar.YEAR, endYear);
    endCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
    endCalendar.set(Calendar.DAY_OF_MONTH, 31);
    Date endDate = endCalendar.getTime();

    return bookRepository.findByPublicationDateBetween(startDate, endDate);
  }

  @Override
  public List<Book> getBooksByPublished(boolean published) {
    return bookRepository.findByPublished(published);
  }

  @Override
  public Book getBookByIsbn(String isbn) {
    return bookRepository.findByIsbnIgnoreCase(isbn);
  }

  @Override
  public List<Book> getBooksByTitleOrDescription(String title, String description) {
    return bookRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(title, description);
  }
}