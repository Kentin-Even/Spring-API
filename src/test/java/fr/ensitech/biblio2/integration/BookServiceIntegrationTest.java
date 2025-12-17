package fr.ensitech.biblio2.integration;

import fr.ensitech.biblio2.entity.Author;
import fr.ensitech.biblio2.entity.Book;
import fr.ensitech.biblio2.repository.IBookRepository;
import fr.ensitech.biblio2.service.BookService;
import fr.ensitech.biblio2.utils.Dates;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BookServiceIntegrationTest {

  @Autowired
  private IBookRepository bookRepository;

  @Autowired
  private BookService bookService;

  private Book validBook;
  private Author author1, author2;

  @BeforeEach
  @SneakyThrows
  void setUp() {
    bookRepository.deleteAll();

    author1 = new Author();
    author1.setFirstName("Pascal");
    author1.setLastName("LAMBERT");

    author2 = new Author();
    author2.setFirstName("Benoit");
    author2.setLastName("DECOUX");

    validBook = new Book();
    validBook.setTitle("Livre de Java");
    validBook.setDescription("Cours et Exercices en Java");
    validBook.setIsbn("9781234567890");
    validBook.setEditor("Editions Eyrolles");
    validBook.setCategory("Informatique");
    validBook.setNbPage((short) 155);
    validBook.setLanguage("FR");
    validBook.setPublished(true);
    validBook.setPublicationDate(Dates.convertStringToDate("15/03/2000"));
    validBook.setStock(10);

    Set<Author> authors = new HashSet<>();
    authors.add(author1);
    authors.add(author2);
    validBook.setAuthors(authors);
  }

  @Test
  @DisplayName("Ajout d'un livre en BDD")
  void shouldAddBookInDatabase() throws Exception {
    //WHEN
    bookService.addOrUpdateBook(validBook);

    //THEN
    assertThat(validBook.getId()).isNotNull();
    assertThat(validBook.getId()).isGreaterThan(0);
    assertThat(bookRepository.findById(validBook.getId())).isPresent();
  }

  @Test
  @DisplayName("Mise à jour d'un livre dans la BDD")
  void shouldUpdateBookInDatabase() throws Exception {
    //GIVEN
    bookService.addOrUpdateBook(validBook);
    Long bookId = validBook.getId();
    validBook.setNbPage((short) 234);
    validBook.setLanguage("PT");

    //WHEN
    bookService.addOrUpdateBook(validBook);

    //THEN
    Book updatedBook = bookRepository.findById(bookId).orElseThrow();
    assertThat(updatedBook.getNbPage()).isEqualTo((short) 234);
    assertThat(updatedBook.getLanguage()).isEqualTo("PT");
  }

  @Test
  @DisplayName("Mise à jour d'un livre inexistant doit lever une exception")
  void shouldThrowExceptionWhenUpdatingNonExistentBook() {
    //GIVEN
    validBook.setId(9999L);

    //WHEN & THEN
    assertThatThrownBy(() -> bookService.addOrUpdateBook(validBook))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Book with id 9999 not found");
  }

  @Test
  @DisplayName("Mise à jour d'un livre avec ID négatif doit lever une exception")
  void shouldThrowExceptionWhenUpdatingBookWithNegativeId() {
    //GIVEN
    validBook.setId(-1L);

    //WHEN & THEN
    assertThatThrownBy(() -> bookService.addOrUpdateBook(validBook))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Book id must be greater than 0");
  }

  @Test
  @DisplayName("Suppression d'un livre de la BDD")
  void shouldDeleteBookFromDatabase() throws Exception {
    //GIVEN
    bookService.addOrUpdateBook(validBook);
    Long bookId = validBook.getId();

    //WHEN
    bookService.deleteBook(bookId);

    //THEN
    assertThat(bookRepository.findById(bookId)).isEmpty();
  }

  @Test
  @DisplayName("Suppression d'un livre inexistant doit lever une exception")
  void shouldThrowExceptionWhenDeletingNonExistentBook() {
    //WHEN & THEN
    assertThatThrownBy(() -> bookService.deleteBook(9999L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Book with id 9999 not found");
  }

  @Test
  @DisplayName("Récupération de tous les livres")
  void shouldGetAllBooks() throws Exception {
    //GIVEN
    Book book2 = new Book();
    book2.setTitle("Livre de Python");
    book2.setDescription("Guide Python");
    book2.setIsbn("9780987654321");
    book2.setEditor("Editions O'Reilly");
    book2.setCategory("Informatique");
    book2.setNbPage((short) 200);
    book2.setLanguage("EN");
    book2.setPublished(true);
    book2.setPublicationDate(Dates.convertStringToDate("10/05/2015"));
    book2.setStock(5);

    bookService.addOrUpdateBook(validBook);
    bookService.addOrUpdateBook(book2);

    //WHEN
    List<Book> books = bookService.getBooks();

    //THEN
    assertThat(books).isNotEmpty();
    assertThat(books.size()).isGreaterThanOrEqualTo(2);
  }

  @Test
  @DisplayName("Récupération d'un livre par ID")
  void shouldGetBookById() throws Exception {
    //GIVEN
    bookService.addOrUpdateBook(validBook);
    Long bookId = validBook.getId();

    //WHEN
    Book foundBook = bookService.getBook(bookId);

    //THEN
    assertThat(foundBook).isNotNull();
    assertThat(foundBook.getId()).isEqualTo(bookId);
    assertThat(foundBook.getTitle()).isEqualTo("Livre de Java");
  }

  @Test
  @DisplayName("Récupération d'un livre inexistant par ID retourne null")
  void shouldReturnNullWhenGettingNonExistentBook() throws Exception {
    //WHEN
    Book foundBook = bookService.getBook(9999L);

    //THEN
    assertThat(foundBook).isNull();
  }

  @Test
  @DisplayName("Récupération d'un livre par titre exact")
  void shouldGetBookByExactTitle() throws Exception {
    //GIVEN
    bookService.addOrUpdateBook(validBook);

    //WHEN
    Book foundBook = bookService.getBookByTitle("Livre de Java");

    //THEN
    assertThat(foundBook).isNotNull();
    assertThat(foundBook.getTitle()).isEqualTo("Livre de Java");
  }

  @Test
  @DisplayName("Récupération d'un livre par titre ignore la casse")
  void shouldGetBookByTitleIgnoreCase() throws Exception {
    //GIVEN
    bookService.addOrUpdateBook(validBook);

    //WHEN
    Book foundBook = bookService.getBookByTitle("livre de java");

    //THEN
    assertThat(foundBook).isNotNull();
    assertThat(foundBook.getTitle()).isEqualTo("Livre de Java");
  }

  @Test
  @DisplayName("Récupération d'un livre par titre inexistant retourne null")
  void shouldReturnNullWhenGettingBookByNonExistentTitle() throws Exception {
    //WHEN
    Book foundBook = bookService.getBookByTitle("Titre Inexistant");

    //THEN
    assertThat(foundBook).isNull();
  }

  @Test
  @DisplayName("Récupération des livres contenant un mot dans le titre")
  void shouldGetBooksByTitleContaining() throws Exception {
    //GIVEN
    Book book2 = new Book();
    book2.setTitle("Java Avancé");
    book2.setDescription("Techniques avancées");
    book2.setIsbn("9780111111111");
    book2.setEditor("Editions Tech");
    book2.setCategory("Informatique");
    book2.setNbPage((short) 300);
    book2.setLanguage("FR");
    book2.setPublished(true);
    book2.setPublicationDate(Dates.convertStringToDate("20/06/2010"));
    book2.setStock(8);

    bookService.addOrUpdateBook(validBook);
    bookService.addOrUpdateBook(book2);

    //WHEN
    List<Book> books = bookService.getBooksByTitleContaining("Java");

    //THEN
    assertThat(books).isNotEmpty();
    assertThat(books).hasSize(2);
    assertThat(books).extracting(Book::getTitle)
            .contains("Livre de Java", "Java Avancé");
  }

  @Test
  @DisplayName("Récupération des livres par ISBN")
  void shouldGetBookByIsbn() throws Exception {
    //GIVEN
    bookService.addOrUpdateBook(validBook);

    //WHEN
    Book foundBook = bookService.getBookByIsbn("9781234567890");

    //THEN
    assertThat(foundBook).isNotNull();
    assertThat(foundBook.getIsbn()).isEqualTo("9781234567890");
    assertThat(foundBook.getTitle()).isEqualTo("Livre de Java");
  }

  @Test
  @DisplayName("Récupération des livres par ISBN ignore la casse")
  void shouldGetBookByIsbnIgnoreCase() throws Exception {
    //GIVEN
    Book bookWithLowerIsbn = new Book();
    bookWithLowerIsbn.setTitle("Livre Unique ISBN");
    bookWithLowerIsbn.setDescription("Test ISBN case");
    bookWithLowerIsbn.setIsbn("9780987654321"); // ISBN valide et plus court
    bookWithLowerIsbn.setEditor("Editions Test");
    bookWithLowerIsbn.setCategory("Test");
    bookWithLowerIsbn.setNbPage((short) 100);
    bookWithLowerIsbn.setLanguage("FR");
    bookWithLowerIsbn.setPublished(true);
    bookWithLowerIsbn.setPublicationDate(Dates.convertStringToDate("01/01/2020"));
    bookWithLowerIsbn.setStock(5);

    bookService.addOrUpdateBook(bookWithLowerIsbn);

    //WHEN
    Book foundBook = bookService.getBookByIsbn("9780987654321"); // Même ISBN

    //THEN
    assertThat(foundBook).isNotNull();
    assertThat(foundBook.getIsbn()).isEqualTo("9780987654321");
    assertThat(foundBook.getTitle()).isEqualTo("Livre Unique ISBN");
  }

  @Test
  @DisplayName("Récupération des livres publiés")
  void shouldGetPublishedBooks() throws Exception {
    //GIVEN
    Book unpublishedBook = new Book();
    unpublishedBook.setTitle("Livre Non Publié");
    unpublishedBook.setDescription("En cours de révision");
    unpublishedBook.setIsbn("9782222222222");
    unpublishedBook.setEditor("Editions Test");
    unpublishedBook.setCategory("Informatique");
    unpublishedBook.setNbPage((short) 100);
    unpublishedBook.setLanguage("FR");
    unpublishedBook.setPublished(false);
    unpublishedBook.setPublicationDate(Dates.convertStringToDate("01/01/2020"));
    unpublishedBook.setStock(0);

    bookService.addOrUpdateBook(validBook);
    bookService.addOrUpdateBook(unpublishedBook);

    //WHEN
    List<Book> publishedBooks = bookService.getBooksByPublished(true);

    //THEN
    assertThat(publishedBooks).isNotEmpty();
    assertThat(publishedBooks).allMatch(Book::isPublished);
  }

  @Test
  @DisplayName("Récupération des livres non publiés")
  void shouldGetUnpublishedBooks() throws Exception {
    //GIVEN
    Book unpublishedBook = new Book();
    unpublishedBook.setTitle("Livre Non Publié");
    unpublishedBook.setDescription("En cours de révision");
    unpublishedBook.setIsbn("9782222222222");
    unpublishedBook.setEditor("Editions Test");
    unpublishedBook.setCategory("Informatique");
    unpublishedBook.setNbPage((short) 100);
    unpublishedBook.setLanguage("FR");
    unpublishedBook.setPublished(false);
    unpublishedBook.setPublicationDate(Dates.convertStringToDate("01/01/2020"));
    unpublishedBook.setStock(0);

    bookService.addOrUpdateBook(validBook);
    bookService.addOrUpdateBook(unpublishedBook);

    //WHEN
    List<Book> unpublishedBooks = bookService.getBooksByPublished(false);

    //THEN
    assertThat(unpublishedBooks).isNotEmpty();
    assertThat(unpublishedBooks).allMatch(book -> !book.isPublished());
  }

  @Test
  @DisplayName("Récupération des livres entre deux années")
  void shouldGetBooksBetweenYears() throws Exception {
    //GIVEN
    Book book2015 = new Book();
    book2015.setTitle("Livre 2015");
    book2015.setDescription("Publié en 2015");
    book2015.setIsbn("9783333333333");
    book2015.setEditor("Editions 2015");
    book2015.setCategory("Informatique");
    book2015.setNbPage((short) 250);
    book2015.setLanguage("FR");
    book2015.setPublished(true);
    book2015.setPublicationDate(Dates.convertStringToDate("15/08/2015"));
    book2015.setStock(3);

    bookService.addOrUpdateBook(validBook); // 2000
    bookService.addOrUpdateBook(book2015); // 2015

    //WHEN
    List<Book> books = bookService.getBooksBetweenYears(2010, 2020);

    //THEN
    assertThat(books).isNotEmpty();
    assertThat(books).hasSize(1);
    assertThat(books.get(0).getTitle()).isEqualTo("Livre 2015");
  }

  @Test
  @DisplayName("Récupération des livres par titre ou description")
  void shouldGetBooksByTitleOrDescription() throws Exception {
    //GIVEN
    Book book2 = new Book();
    book2.setTitle("Livre de Python");
    book2.setDescription("Cours et Exercices en Java");
    book2.setIsbn("9784444444444");
    book2.setEditor("Editions Python");
    book2.setCategory("Informatique");
    book2.setNbPage((short) 180);
    book2.setLanguage("FR");
    book2.setPublished(true);
    book2.setPublicationDate(Dates.convertStringToDate("10/10/2018"));
    book2.setStock(7);

    bookService.addOrUpdateBook(validBook);
    bookService.addOrUpdateBook(book2);

    //WHEN
    List<Book> books = bookService.getBooksByTitleOrDescription("Java", "Java");

    //THEN
    assertThat(books).isNotEmpty();
    assertThat(books).hasSize(2);
    assertThat(books).extracting(Book::getTitle)
            .contains("Livre de Java", "Livre de Python");
  }

  @Test
  @DisplayName("Recherche par titre ou description ignore la casse")
  void shouldGetBooksByTitleOrDescriptionIgnoreCase() throws Exception {
    //GIVEN
    bookService.addOrUpdateBook(validBook);

    //WHEN
    List<Book> books = bookService.getBooksByTitleOrDescription("java", "exercices");

    //THEN
    assertThat(books).isNotEmpty();
    assertThat(books.get(0).getTitle()).isEqualTo("Livre de Java");
  }
}