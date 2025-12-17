package fr.ensitech.biblio2.integration;

import fr.ensitech.biblio2.entity.Author;
import fr.ensitech.biblio2.repository.IAuthorRepository;
import fr.ensitech.biblio2.service.AuthorService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthorServiceIntegrationTest {

  @Autowired
  private IAuthorRepository authorRepository;

  @Autowired
  private AuthorService authorService;

  private Author author1, author2, author3;

  @BeforeEach
  void setUp() {
    // Nettoyer la base de données avant chaque test
    authorRepository.deleteAll();

    author1 = new Author();
    author1.setFirstName("Pascal");
    author1.setLastName("LAMBERT");

    author2 = new Author();
    author2.setFirstName("Benoit");
    author2.setLastName("DECOUX");

    author3 = new Author();
    author3.setFirstName("Pascal");
    author3.setLastName("DUPONT");
  }

  @Test
  @DisplayName("Récupération des auteurs par prénom exact")
  void shouldGetAuthorsByFirstName() {
    //GIVEN
    authorRepository.save(author1);
    authorRepository.save(author2);
    authorRepository.save(author3);

    //WHEN
    List<Author> authors = authorService.getAuthors("Pascal");

    //THEN
    assertThat(authors).isNotEmpty();
    assertThat(authors).hasSize(2);
    assertThat(authors).extracting(Author::getFirstName)
            .containsOnly("Pascal");
    assertThat(authors).extracting(Author::getLastName)
            .containsExactlyInAnyOrder("LAMBERT", "DUPONT");
  }

  @Test
  @DisplayName("Récupération des auteurs par prénom ignore la casse")
  void shouldGetAuthorsByFirstNameIgnoreCase() {
    //GIVEN
    authorRepository.save(author1);
    authorRepository.save(author2);

    //WHEN
    List<Author> authors = authorService.getAuthors("pascal");

    //THEN
    assertThat(authors).isNotEmpty();
    assertThat(authors).hasSize(1);
    assertThat(authors.get(0).getFirstName()).isEqualToIgnoringCase("pascal");
    assertThat(authors.get(0).getLastName()).isEqualTo("LAMBERT");
  }

  @Test
  @DisplayName("Récupération des auteurs par prénom inexistant retourne liste vide")
  void shouldReturnEmptyListWhenFirstNameNotFound() {
    //GIVEN
    authorRepository.save(author1);
    authorRepository.save(author2);

    //WHEN
    List<Author> authors = authorService.getAuthors("Jean");

    //THEN
    assertThat(authors).isEmpty();
  }

  @Test
  @DisplayName("Récupération des auteurs par prénom et nom")
  void shouldGetAuthorsByFirstNameAndLastName() {
    //GIVEN
    authorRepository.save(author1);
    authorRepository.save(author2);
    authorRepository.save(author3);

    //WHEN
    List<Author> authors = authorService.getAuthors("Pascal", "LAMBERT");

    //THEN
    assertThat(authors).isNotEmpty();
    assertThat(authors).hasSize(1);
    assertThat(authors.get(0).getFirstName()).isEqualTo("Pascal");
    assertThat(authors.get(0).getLastName()).isEqualTo("LAMBERT");
  }

  @Test
  @DisplayName("Récupération des auteurs par prénom et nom ignore la casse")
  void shouldGetAuthorsByFirstNameAndLastNameIgnoreCase() {
    //GIVEN
    authorRepository.save(author1);
    authorRepository.save(author2);

    //WHEN
    List<Author> authors = authorService.getAuthors("pascal", "lambert");

    //THEN
    assertThat(authors).isNotEmpty();
    assertThat(authors).hasSize(1);
    assertThat(authors.get(0).getFirstName()).isEqualToIgnoringCase("pascal");
    assertThat(authors.get(0).getLastName()).isEqualToIgnoringCase("lambert");
  }

  @Test
  @DisplayName("Récupération des auteurs par prénom et nom inexistants retourne liste vide")
  void shouldReturnEmptyListWhenFirstNameAndLastNameNotFound() {
    //GIVEN
    authorRepository.save(author1);
    authorRepository.save(author2);

    //WHEN
    List<Author> authors = authorService.getAuthors("Jean", "MARTIN");

    //THEN
    assertThat(authors).isEmpty();
  }

  @Test
  @DisplayName("Récupération des auteurs avec prénom existant mais nom inexistant")
  void shouldReturnEmptyListWhenFirstNameExistsButLastNameDoesNot() {
    //GIVEN
    authorRepository.save(author1);
    authorRepository.save(author2);
    authorRepository.save(author3);

    //WHEN
    List<Author> authors = authorService.getAuthors("Pascal", "MARTIN");

    //THEN
    assertThat(authors).isEmpty();
  }

  @Test
  @DisplayName("Récupération de tous les auteurs ayant le même prénom avec différents noms")
  void shouldGetAllAuthorsWithSameFirstNameButDifferentLastNames() {
    //GIVEN
    authorRepository.save(author1); // Pascal LAMBERT
    authorRepository.save(author3); // Pascal DUPONT

    //WHEN
    List<Author> authors = authorService.getAuthors("Pascal");

    //THEN
    assertThat(authors).isNotEmpty();
    assertThat(authors).hasSize(2);
    assertThat(authors).allMatch(author -> author.getFirstName().equals("Pascal"));
    assertThat(authors).extracting(Author::getLastName)
            .containsExactlyInAnyOrder("LAMBERT", "DUPONT");
  }

  @Test
  @DisplayName("Récupération avec base de données vide retourne liste vide")
  void shouldReturnEmptyListWhenDatabaseIsEmpty() {
    //WHEN
    List<Author> authorsByFirstName = authorService.getAuthors("Pascal");
    List<Author> authorsByFullName = authorService.getAuthors("Pascal", "LAMBERT");

    //THEN
    assertThat(authorsByFirstName).isEmpty();
    assertThat(authorsByFullName).isEmpty();
  }

  @Test
  @DisplayName("Récupération avec prénom null ou vide")
  void shouldHandleNullOrEmptyFirstName() {
    //GIVEN
    authorRepository.save(author1);
    authorRepository.save(author2);

    //WHEN
    List<Author> authorsWithNull = authorService.getAuthors((String) null);
    List<Author> authorsWithEmpty = authorService.getAuthors("");

    //THEN
    assertThat(authorsWithNull).isEmpty();
    assertThat(authorsWithEmpty).isEmpty();
  }

  @Test
  @DisplayName("Récupération avec prénom et nom null ou vide")
  void shouldHandleNullOrEmptyFirstNameAndLastName() {
    //GIVEN
    authorRepository.save(author1);
    authorRepository.save(author2);

    //WHEN
    List<Author> authorsWithNullFirstName = authorService.getAuthors(null, "LAMBERT");
    List<Author> authorsWithNullLastName = authorService.getAuthors("Pascal", null);
    List<Author> authorsWithEmptyFirstName = authorService.getAuthors("", "LAMBERT");
    List<Author> authorsWithEmptyLastName = authorService.getAuthors("Pascal", "");

    //THEN
    assertThat(authorsWithNullFirstName).isEmpty();
    assertThat(authorsWithNullLastName).isEmpty();
    assertThat(authorsWithEmptyFirstName).isEmpty();
    assertThat(authorsWithEmptyLastName).isEmpty();
  }

  @Test
  @DisplayName("Récupération avec des espaces dans les noms")
  void shouldHandleNamesWithSpaces() {
    //GIVEN
    Author authorWithSpaces = new Author();
    authorWithSpaces.setFirstName("Jean Pierre");
    authorWithSpaces.setLastName("MARTIN DURAND");
    authorRepository.save(authorWithSpaces);

    //WHEN
    List<Author> authors = authorService.getAuthors("Jean Pierre", "MARTIN DURAND");

    //THEN
    assertThat(authors).isNotEmpty();
    assertThat(authors).hasSize(1);
    assertThat(authors.get(0).getFirstName()).isEqualTo("Jean Pierre");
    assertThat(authors.get(0).getLastName()).isEqualTo("MARTIN DURAND");
  }

  @Test
  @DisplayName("Récupération avec caractères spéciaux et accents")
  void shouldHandleSpecialCharactersAndAccents() {
    //GIVEN
    Author authorWithAccents = new Author();
    authorWithAccents.setFirstName("François");
    authorWithAccents.setLastName("JOSÉ-MARÍA");
    authorRepository.save(authorWithAccents);

    //WHEN
    List<Author> authors = authorService.getAuthors("François", "JOSÉ-MARÍA");

    //THEN
    assertThat(authors).isNotEmpty();
    assertThat(authors).hasSize(1);
    assertThat(authors.get(0).getFirstName()).isEqualTo("François");
    assertThat(authors.get(0).getLastName()).isEqualTo("JOSÉ-MARÍA");
  }
}