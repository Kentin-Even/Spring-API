package fr.ensitech.biblio2.service;

import fr.ensitech.biblio2.entity.Author;
import fr.ensitech.biblio2.entity.Book;
import fr.ensitech.biblio2.repository.IBookRepository;
import fr.ensitech.biblio2.utils.Dates;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitaires pour BookService
 * Coverage : 100%
 * Style : AssertJ + @SneakyThrows
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires BookService - 100% Coverage")
public class BookServiceTest {

  @Mock
  private IBookRepository bookRepository;

  @InjectMocks
  private BookService bookService;

  private Book validBook;
  private Author author1;
  private Author author2;

  @SneakyThrows
  @BeforeEach
  void setUp() {
    author1 = new Author();
    author1.setId(1L);
    author1.setFirstName("Pascal");
    author1.setLastName("LAMBERT");

    author2 = new Author();
    author2.setId(2L);
    author2.setFirstName("Benoit");
    author2.setLastName("DECOUX");

    validBook = new Book();
    validBook.setId(1L);
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

  @AfterEach
  void tearDown() {
    // Cleanup si nécessaire
  }

  // ========================================
  // TESTS AJOUT DE LIVRE - CAS NOMINAUX
  // ========================================

  @Nested
  @DisplayName("Ajout de livre - Cas nominaux")
  class AjoutLivreCasNominaux {

    @SneakyThrows
    @Test
    @DisplayName("Devrait ajouter un livre valide avec ID null")
    void shouldAddBookWithNullId() {
      // GIVEN
      Book newBook = new Book();
      newBook.setId(null); // ID null
      newBook.setIsbn("9780987654321");
      newBook.setTitle("Nouveau Livre");
      newBook.setDescription("Description du nouveau livre");
      newBook.setEditor("Editeur Test");
      newBook.setPublicationDate(new Date());
      newBook.setCategory("Test");
      newBook.setLanguage("FR");
      newBook.setNbPage((short) 200);

      when(bookRepository.save(newBook)).thenReturn(newBook);

      // WHEN
      bookService.addOrUpdateBook(newBook);

      // THEN
      verify(bookRepository).save(newBook);
      verify(bookRepository, never()).findById(anyLong());
    }

    @SneakyThrows
    @Test
    @DisplayName("Devrait ajouter un livre valide avec ID = 0")
    void shouldAddBookWithZeroId() {
      // GIVEN
      Book newBook = new Book();
      newBook.setId(0L); // ID = 0
      newBook.setIsbn("9780987654321");
      newBook.setTitle("Nouveau Livre");
      newBook.setDescription("Description du nouveau livre");
      newBook.setEditor("Editeur Test");
      newBook.setPublicationDate(new Date());
      newBook.setCategory("Test");
      newBook.setLanguage("FR");
      newBook.setNbPage((short) 200);

      when(bookRepository.save(newBook)).thenReturn(newBook);

      // WHEN
      bookService.addOrUpdateBook(newBook);

      // THEN
      verify(bookRepository).save(newBook);
      verify(bookRepository, never()).findById(anyLong());
    }

    @SneakyThrows
    @Test
    @DisplayName("Devrait ajouter un livre avec plusieurs auteurs")
    void shouldAddBookWithMultipleAuthors() {
      // GIVEN
      validBook.setId(null); // Pour simuler un ajout
      when(bookRepository.save(validBook)).thenReturn(validBook);

      // WHEN
      bookService.addOrUpdateBook(validBook);

      // THEN
      assertThat(validBook.getAuthors())
              .isNotNull()
              .hasSize(2);

      verify(bookRepository).save(validBook);
    }
  }

  // ========================================
  // TESTS MISE À JOUR DE LIVRE - CAS NOMINAUX
  // ========================================

  @Nested
  @DisplayName("Mise à jour de livre - Cas nominaux")
  class MiseAJourLivreCasNominaux {

    @SneakyThrows
    @Test
    @DisplayName("Devrait mettre à jour un livre existant")
    void shouldUpdateExistingBook() {
      // GIVEN
      Book existingBook = new Book();
      existingBook.setId(1L);
      existingBook.setIsbn("9781234567890");
      existingBook.setTitle("Ancien Titre");
      existingBook.setDescription("Ancienne Description");
      existingBook.setEditor("Ancien Editeur");
      existingBook.setPublicationDate(new Date());
      existingBook.setCategory("Ancienne Categorie");
      existingBook.setLanguage("FR");
      existingBook.setNbPage((short) 100);
      existingBook.setPublished(false);

      Book updatedBook = new Book();
      updatedBook.setId(1L);
      updatedBook.setIsbn("9780000000000");
      updatedBook.setTitle("Nouveau Titre");
      updatedBook.setDescription("Nouvelle Description");
      updatedBook.setEditor("Nouvel Editeur");
      updatedBook.setPublicationDate(new Date());
      updatedBook.setCategory("Nouvelle Categorie");
      updatedBook.setLanguage("EN");
      updatedBook.setNbPage((short) 500);
      updatedBook.setPublished(true);

      when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
      when(bookRepository.save(any(Book.class))).thenReturn(existingBook);

      // WHEN
      bookService.addOrUpdateBook(updatedBook);

      // THEN
      verify(bookRepository).findById(1L);
      verify(bookRepository).save(any(Book.class));

      assertThat(existingBook)
              .extracting(Book::getIsbn, Book::getTitle, Book::getDescription, Book::getEditor)
              .containsExactly("9780000000000", "Nouveau Titre", "Nouvelle Description", "Nouvel Editeur");

      assertThat(existingBook.getCategory()).isEqualTo("Nouvelle Categorie");
      assertThat(existingBook.getLanguage()).isEqualTo("EN");
      assertThat(existingBook.getNbPage()).isEqualTo((short) 500);
      assertThat(existingBook.isPublished()).isTrue();
    }

    @SneakyThrows
    @Test
    @DisplayName("Devrait mettre à jour toutes les propriétés modifiables")
    void shouldUpdateAllModifiableProperties() {
      // GIVEN
      Book existingBook = new Book();
      existingBook.setId(1L);
      existingBook.setIsbn("OLD_ISBN");
      existingBook.setTitle("Old Title");
      existingBook.setDescription("Old Description");
      existingBook.setEditor("Old Editor");
      existingBook.setPublicationDate(Dates.convertStringToDate("01/01/2000"));
      existingBook.setCategory("Old Category");
      existingBook.setLanguage("FR");
      existingBook.setNbPage((short) 100);
      existingBook.setPublished(false);

      Book updateData = new Book();
      updateData.setId(1L);
      updateData.setIsbn("NEW_ISBN");
      updateData.setTitle("New Title");
      updateData.setDescription("New Description");
      updateData.setEditor("New Editor");
      updateData.setPublicationDate(Dates.convertStringToDate("31/12/2024"));
      updateData.setCategory("New Category");
      updateData.setLanguage("EN");
      updateData.setNbPage((short) 999);
      updateData.setPublished(true);

      when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
      when(bookRepository.save(existingBook)).thenReturn(existingBook);

      // WHEN
      bookService.addOrUpdateBook(updateData);

      // THEN
      verify(bookRepository).findById(1L);
      verify(bookRepository).save(existingBook);

      assertThat(existingBook.getIsbn()).isEqualTo("NEW_ISBN");
      assertThat(existingBook.getTitle()).isEqualTo("New Title");
      assertThat(existingBook.getDescription()).isEqualTo("New Description");
      assertThat(existingBook.getEditor()).isEqualTo("New Editor");
      assertThat(existingBook.getCategory()).isEqualTo("New Category");
      assertThat(existingBook.getLanguage()).isEqualTo("EN");
      assertThat(existingBook.getNbPage()).isEqualTo((short) 999);
      assertThat(existingBook.isPublished()).isTrue();
    }
  }

  // ========================================
  // TESTS MISE À JOUR - CAS D'ERREUR
  // ========================================

  @Nested
  @DisplayName("Mise à jour de livre - Cas d'erreur")
  class MiseAJourLivreCasErreur {

    @Test
    @SneakyThrows
    @DisplayName("Devrait lever une exception avec un ID négatif")
    void shouldThrowExceptionWithNegativeId() {
      // GIVEN
      Book bookWithNegativeId = new Book();
      bookWithNegativeId.setId(-1L);
      bookWithNegativeId.setIsbn("9781234567890");
      bookWithNegativeId.setTitle("Book");
      bookWithNegativeId.setDescription("Description");
      bookWithNegativeId.setEditor("Editor");
      bookWithNegativeId.setPublicationDate(new Date());
      bookWithNegativeId.setCategory("Category");
      bookWithNegativeId.setLanguage("FR");
      bookWithNegativeId.setNbPage((short) 100);

      // WHEN & THEN
      assertThatThrownBy(() -> bookService.addOrUpdateBook(bookWithNegativeId))
              .isInstanceOf(Exception.class)
              .hasMessageContaining("greater than 0");

      verify(bookRepository, never()).findById(anyLong());
      verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @SneakyThrows
    @DisplayName("Devrait lever une exception lors de la mise à jour d'un livre inexistant")
    void shouldThrowExceptionWhenUpdatingNonExistentBook() {
      // GIVEN
      Book nonExistentBook = new Book();
      nonExistentBook.setId(999L);
      nonExistentBook.setIsbn("9781234567890");
      nonExistentBook.setTitle("Non Existent");
      nonExistentBook.setDescription("Description");
      nonExistentBook.setEditor("Editor");
      nonExistentBook.setPublicationDate(new Date());
      nonExistentBook.setCategory("Category");
      nonExistentBook.setLanguage("FR");
      nonExistentBook.setNbPage((short) 100);

      when(bookRepository.findById(999L)).thenReturn(Optional.empty());

      // WHEN & THEN
      assertThatThrownBy(() -> bookService.addOrUpdateBook(nonExistentBook))
              .isInstanceOf(Exception.class)
              .hasMessageContaining("not found");

      verify(bookRepository).findById(999L);
      verify(bookRepository, never()).save(any(Book.class));
    }
  }

  // ========================================
  // TESTS SUPPRESSION - CAS NOMINAUX
  // ========================================

  @Nested
  @DisplayName("Suppression de livre - Cas nominaux")
  class SuppressionLivreCasNominaux {

    @Test
    @SneakyThrows
    @DisplayName("Devrait supprimer un livre existant")
    void shouldDeleteExistingBook() {
      // GIVEN
      when(bookRepository.findById(1L)).thenReturn(Optional.of(validBook));
      doNothing().when(bookRepository).deleteById(1L);

      // WHEN
      bookService.deleteBook(1L);

      // THEN
      verify(bookRepository).findById(1L);
      verify(bookRepository).deleteById(1L);
    }
  }

  // ========================================
  // TESTS SUPPRESSION - CAS D'ERREUR
  // ========================================

  @Nested
  @DisplayName("Suppression de livre - Cas d'erreur")
  class SuppressionLivreCasErreur {

    @Test
    @SneakyThrows
    @DisplayName("Devrait lever une exception lors de la suppression d'un livre inexistant")
    void shouldThrowExceptionWhenDeletingNonExistentBook() {
      // GIVEN
      when(bookRepository.findById(999L)).thenReturn(Optional.empty());

      // WHEN & THEN
      assertThatThrownBy(() -> bookService.deleteBook(999L))
              .isInstanceOf(Exception.class)
              .hasMessageContaining("not found");

      verify(bookRepository).findById(999L);
      verify(bookRepository, never()).deleteById(anyLong());
    }
  }

  // ========================================
  // TESTS RECHERCHE - CAS NOMINAUX
  // ========================================

  @Nested
  @DisplayName("Recherche de livres - Cas nominaux")
  class RechercheLivres {

    @Test
    @SneakyThrows
    @DisplayName("Devrait obtenir tous les livres")
    void shouldGetAllBooks() {
      // GIVEN
      List<Book> expectedBooks = Arrays.asList(validBook);
      when(bookRepository.findAll()).thenReturn(expectedBooks);

      // WHEN
      List<Book> result = bookService.getBooks();

      // THEN
      assertThat(result)
              .isNotNull()
              .hasSize(1)
              .containsExactly(validBook);

      verify(bookRepository).findAll();
    }

    @Test
    @SneakyThrows
    @DisplayName("Devrait obtenir un livre par ID")
    void shouldGetBookById() {
      // GIVEN
      when(bookRepository.findById(1L)).thenReturn(Optional.of(validBook));

      // WHEN
      Book result = bookService.getBook(1L);

      // THEN
      assertThat(result)
              .isNotNull()
              .extracting(Book::getId, Book::getTitle)
              .containsExactly(1L, "Livre de Java");

      verify(bookRepository).findById(1L);
    }

    @Test
    @SneakyThrows
    @DisplayName("Devrait retourner null pour un livre inexistant")
    void shouldReturnNullForNonExistentBook() {
      // GIVEN
      when(bookRepository.findById(999L)).thenReturn(Optional.empty());

      // WHEN
      Book result = bookService.getBook(999L);

      // THEN
      assertThat(result).isNull();

      verify(bookRepository).findById(999L);
    }

    @Test
    @SneakyThrows
    @DisplayName("Devrait rechercher par titre")
    void shouldSearchByTitle() {
      // GIVEN
      List<Book> expectedBooks = Arrays.asList(validBook);
      when(bookRepository.findByTitleIgnoreCase("Livre de Java")).thenReturn(expectedBooks);

      // WHEN
      Book result = bookService.getBookByTitle("Livre de Java");

      // THEN
      assertThat(result)
              .isNotNull()
              .extracting(Book::getTitle)
              .isEqualTo("Livre de Java");

      verify(bookRepository).findByTitleIgnoreCase("Livre de Java");
    }

    @Test
    @SneakyThrows
    @DisplayName("Devrait retourner null si aucun livre trouvé par titre")
    void shouldReturnNullIfNoBookFoundByTitle() {
      // GIVEN
      when(bookRepository.findByTitleIgnoreCase("Titre Inexistant")).thenReturn(Collections.emptyList());

      // WHEN
      Book result = bookService.getBookByTitle("Titre Inexistant");

      // THEN
      assertThat(result).isNull();

      verify(bookRepository).findByTitleIgnoreCase("Titre Inexistant");
    }

    @Test
    @SneakyThrows
    @DisplayName("Devrait rechercher par titre contenant")
    void shouldSearchByTitleContaining() {
      // GIVEN
      List<Book> expectedBooks = Arrays.asList(validBook);
      when(bookRepository.findByTitleContainingIgnoreCase("Java")).thenReturn(expectedBooks);

      // WHEN
      List<Book> result = bookService.getBooksByTitleContaining("Java");

      // THEN
      assertThat(result)
              .isNotNull()
              .hasSize(1)
              .extracting(Book::getTitle)
              .containsExactly("Livre de Java");

      verify(bookRepository).findByTitleContainingIgnoreCase("Java");
    }

    @Test
    @SneakyThrows
    @DisplayName("Devrait rechercher par ISBN")
    void shouldSearchByIsbn() {
      // GIVEN
      when(bookRepository.findByIsbnIgnoreCase("9781234567890")).thenReturn(validBook);

      // WHEN
      Book result = bookService.getBookByIsbn("9781234567890");

      // THEN
      assertThat(result)
              .isNotNull()
              .extracting(Book::getIsbn)
              .isEqualTo("9781234567890");

      verify(bookRepository).findByIsbnIgnoreCase("9781234567890");
    }

    @Test
    @SneakyThrows
    @DisplayName("Devrait rechercher les livres publiés")
    void shouldSearchPublishedBooks() {
      // GIVEN
      List<Book> publishedBooks = Arrays.asList(validBook);
      when(bookRepository.findByPublished(true)).thenReturn(publishedBooks);

      // WHEN
      List<Book> results = bookService.getBooksByPublished(true);

      // THEN
      assertThat(results)
              .isNotNull()
              .hasSize(1)
              .allMatch(Book::isPublished);

      verify(bookRepository).findByPublished(true);
    }

    @Test
    @SneakyThrows
    @DisplayName("Devrait rechercher les livres non publiés")
    void shouldSearchUnpublishedBooks() {
      // GIVEN
      Book unpublishedBook = new Book();
      unpublishedBook.setPublished(false);
      List<Book> unpublishedBooks = Arrays.asList(unpublishedBook);
      when(bookRepository.findByPublished(false)).thenReturn(unpublishedBooks);

      // WHEN
      List<Book> results = bookService.getBooksByPublished(false);

      // THEN
      assertThat(results)
              .isNotNull()
              .hasSize(1)
              .allMatch(book -> !book.isPublished());

      verify(bookRepository).findByPublished(false);
    }

    @Test
    @SneakyThrows
    @DisplayName("Devrait rechercher par mot-clé dans titre et description")
    void shouldSearchByKeyword() {
      // GIVEN
      List<Book> expectedBooks = Arrays.asList(validBook);
      when(bookRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("Java", "Java"))
              .thenReturn(expectedBooks);

      // WHEN
      List<Book> results = bookService.getBooksByTitleOrDescription("Java", "Java");

      // THEN
      assertThat(results)
              .isNotNull()
              .hasSize(1);

      verify(bookRepository).findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("Java", "Java");
    }

    @Test
    @SneakyThrows
    @DisplayName("Devrait rechercher entre deux années")
    void shouldSearchBetweenYears() {
      // GIVEN
      List<Book> expectedBooks = Arrays.asList(validBook);
      when(bookRepository.findByPublicationDateBetween(any(Date.class), any(Date.class)))
              .thenReturn(expectedBooks);

      // WHEN
      List<Book> results = bookService.getBooksBetweenYears(1999, 2001);

      // THEN
      assertThat(results)
              .isNotNull()
              .hasSize(1);

      verify(bookRepository).findByPublicationDateBetween(any(Date.class), any(Date.class));
    }

    @Test
    @SneakyThrows
    @DisplayName("Devrait retourner null pour getBooksByAuthor (non implémenté)")
    void shouldReturnNullForGetBooksByAuthor() {
      // GIVEN
      Author author = new Author();
      author.setFirstName("John");
      author.setLastName("Doe");

      // WHEN
      List<Book> result = bookService.getBooksByAuthor(author);

      // THEN
      assertThat(result).isNull();
    }
  }

  // ========================================
  // TESTS VÉRIFICATION DES INTERACTIONS
  // ========================================

  @Nested
  @DisplayName("Vérification des interactions avec le repository")
  class VerificationInteractions {

    @Test
    @SneakyThrows
    @DisplayName("Devrait appeler le repository exactement une fois lors de l'ajout")
    void shouldCallRepositoryOnceOnAdd() {
      // GIVEN
      Book newBook = new Book();
      newBook.setIsbn("9781234567890");
      newBook.setTitle("New Book");
      newBook.setDescription("Description");
      newBook.setEditor("Editor");
      newBook.setPublicationDate(new Date());
      newBook.setCategory("Category");
      newBook.setLanguage("FR");
      newBook.setNbPage((short) 100);

      when(bookRepository.save(any(Book.class))).thenReturn(newBook);

      // WHEN
      bookService.addOrUpdateBook(newBook);

      // THEN
      verify(bookRepository, times(1)).save(newBook);
      verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @SneakyThrows
    @DisplayName("Ne devrait jamais sauvegarder si la recherche échoue")
    void shouldNeverSaveIfFindFails() {
      // GIVEN
      Book book = new Book();
      book.setId(999L);
      book.setIsbn("9781234567890");
      book.setTitle("Book");
      book.setDescription("Description");
      book.setEditor("Editor");
      book.setPublicationDate(new Date());
      book.setCategory("Category");
      book.setLanguage("FR");
      book.setNbPage((short) 100);

      when(bookRepository.findById(999L)).thenReturn(Optional.empty());

      // WHEN & THEN
      assertThatThrownBy(() -> bookService.addOrUpdateBook(book))
              .isInstanceOf(Exception.class);

      verify(bookRepository).findById(999L);
      verify(bookRepository, never()).save(any(Book.class));
    }
  }
}